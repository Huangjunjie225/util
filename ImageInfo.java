package com.holden;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author dw_huangjunjie2
 * @date 2019/6/17 10:00
 */
public class ImageInfo {

    static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws IOException {

        if(null == args || args.length < 1) {
            return;
        }
        String filePath = args[0];
        if(null == filePath || "".equals(filePath.trim())) {
            return;
        }
        File originFile = new File(filePath);
        FileWriter writerSuccess = new FileWriter("E:\\success_result.txt");
        BufferedWriter bwSuccess = new BufferedWriter(writerSuccess);
        FileWriter writerError = new FileWriter("E:\\error_result.txt");
        BufferedWriter bwError = new BufferedWriter(writerError);
        StringBuffer resultSuccess = new StringBuffer();
        StringBuffer resultError = new StringBuffer();
        List<Data> records = new ArrayList<>();
        List<Data> errorRecords = new ArrayList<>();
        if(originFile.exists()) {
            try {
                FileReader fileReader = new FileReader(originFile);
                BufferedReader br = new BufferedReader(fileReader);
                String record = null;
                while((record = br.readLine()) != null) {
                    // record format [tableName id index pic_url size]
                    String[] items = record.split("\\s+");
                    Data data = new Data();
                    data.setRecord(record);
                    data.setTableName(items[0]);
                    data.setId(items[1]);
                    data.setIndex(items[2]);
                    data.setPicUrl(items[3]);
                    if(items.length == 4) {
                        errorRecords.add(data);
                        continue;
                    }
                    data.setSize(items[4]);
                    records.add(data);
                }
                br.close();
                fileReader.close();
                int i = 0;
                for(Data data : records) {
                    i++;
                    if(i % 500 == 0) {
                        Thread.sleep(20000);
                        System.out.println("submit !!!");
                    }
                    fixedThreadPool.submit(() -> {
                        URL url = null;
                        if(data.getPicUrl().endsWith("zip") || data.getPicUrl().endsWith("svga")) {
                            return;
                        }
                        System.out.println("record: " + data.getRecord());
                        try {
                            url = new URL(data.getPicUrl());
                            URLConnection connection = url.openConnection();
                            connection.setDoOutput(true);
                            BufferedImage image = ImageIO.read(connection.getInputStream());
                            int srcWidth = image .getWidth();
                            int srcHeight = image .getHeight();
                            data.setWidth(srcWidth);
                            data.setHeight(srcHeight);
                            data.setAvg((((double) Long.parseLong(data.getSize()))/ (data.getHeight()*data.getWidth())));
                            System.out.println("record: " + data.getRecord() + " finish width: " + srcWidth + " height: " + srcHeight);
                        } catch (Exception e) {
                            errorRecords.add(data);
                            System.out.println("error!!!, record: " + data.getRecord() + "   " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            fixedThreadPool.shutdown();
            while(true) {
                if(fixedThreadPool.isTerminated()) {
                    System.out.println("thread pool is terminated!!!");
                    records = records.stream().filter((d) -> d.getAvg() != null).sorted(Comparator.comparing(Data::getAvg,Comparator.nullsLast(Double::compareTo))).collect(Collectors.toList());
                    for(Data data : records) {
                        resultSuccess.append(data.getTableName()).append(" ").append(data.getId()).append(" ").append(data.getIndex()).append(" ").append(data.getPicUrl()).append(" ").append(data.getSize()).append(" ").append(data.getWidth()).append("x").append(data.getHeight()).append(" ").append(data.getAvg()).append("\n");
                        System.out.println(data.getTableName() + " " + data.getId() + " " + data.getIndex() + " " + data.getPicUrl() + " " + data.getSize() + " " + data.getWidth() + " " + data.getHeight());
                    }
                    errorRecords.forEach((data) -> {
                        resultError.append(data.getRecord()).append("\n");
                    });
                    break;
                }
            }
            bwSuccess.write(resultSuccess.toString());
            bwError.write(resultError.toString());
            bwSuccess.close();
            writerSuccess.close();
            bwError.close();
            writerError.close();
        }
    }

    static class Data {

        private String tableName;
        private String id;
        private String index;
        private String picUrl;
        private String size;
        private Integer width;
        private Integer height;
        private String record;
        private Double avg;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public String getRecord() {
            return record;
        }

        public void setRecord(String record) {
            this.record = record;
        }

        public Double getAvg() {
            return avg;
        }

        public void setAvg(Double avg) {
            this.avg = avg;
        }
    }

}
