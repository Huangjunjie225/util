#!/bash/bin
# find image from database

echo 'start find...'
cd ~
mysqlBin='/usr/bin/mysql'
mysqlHost='localhost'
mysqlUser='root'
mysqlPassword='123456'
mysqlDatabase='******'
mysqlCommand="${mysqlBin} -h${mysqlHost} -u${mysqlUser} -p${mysqlPassword} -D${mysqlDatabase}"
mysqlCommandWithoutName="${mysqlBin} -h${mysqlHost} -u${mysqlUser} -p${mysqlPassword} -D${mysqlDatabase} -N "

# init text
rm -rf ~/result.txt

existPictureTables=''

# get all table from database, write in ~/tables.txt
echo "show tables" | ${mysqlCommand} > ~/tables.txt
sed -i '1d' ~/tables.txt

# get sql of all including pic column
while read line
do
	example=`echo "select * from $line limit 1" | ${mysqlCommand}`
	echo "$example" > ~/temp.txt
	isHasImage=`cat ~/temp.txt | gawk -F "[\t]" 'BEGIN{flag=0;count=0;columns="";}{for(i=1;i<=NF;i++) {if(NR == 1) {column[i]=$i;} if($i ~/jpg$|png$|gif$/) {flag=1;indexs[count++]=i;}}} END{if(flag == 1) {
	for(x in indexs) {columns=(columns","column[indexs[x]]);} print columns;} else {print "";}}'`
	echo ${line}" isHasImage: "${isHasImage}
    if [ "$isHasImage"x != "x" ]; then
        existPictureTables=${existPictureTables}"\\nselect '"${line}"' as table_name,id"${isHasImage}" from "${line}
    fi
done < ~/tables.txt
echo -e "$existPictureTables" > ~/table_column.txt
sed -i "1d" ~/table_column.txt

all_table_url=""

# get all image url
while read line
do
    #echo "$line"
	table=`echo $line | awk 'END{print $NF;}'`
	#echo "table: $table"
	all_url=`echo $line | ${mysqlCommandWithoutName} `
    all_table_url=$all_table_url"\\n"$all_url
done < ~/table_column.txt

echo -e "$all_table_url" > ~/all_table_url.txt

result_with_size=''

# get all url with size(b)
while read line
do
	echo "$line"
	array=($line)
	index=2
	num=${#array[*]}
	table=${array[0]}
	id=${array[1]}
	otherInfo=""
	for((i=2;i<$num;i++)); do
		url=${array[i]}
		if [ $url == 'NULL' ]; then
			continue
		fi
		size=`curl -sI $url | grep Content-Length | awk '{print $2}'`
		urlsize="${url} ${size}"
		echo "$table $id ${i-1}  $urlsize" >> ~/result.txt
	done
done < ~/all_table_url.txt

# delete unuseful text
rm -rf ~/tables.txt
rm -rf ~/temp.txt
rm -rf ~/existPictureTable.txt
rm -rf ~/table_column.txt
rm -rf ~/all_table_url.txt
echo 'end find...'
