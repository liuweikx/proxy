#! /bin/bash
name=$(date "+%Y-%m-%d_%H:%M:%S")
flag=$(java -jar  /home/hepan/jar/ParseAgents2DB.jar count http)
http="http";
if [ "$flag" == "$http" ];then
wget -O /home/hepan/agentsdir/agents "http://ip.baizhongsou.com/?id=609567295279698&sl=1000"
cat /home/hepan/agentsdir/agents | sed "s#<br>#\n#g" > /home/hepan/agentsdir/agents_$name
cat /home/hepan/agentsdir/agents_$name | java -jar /home/hepan/jar/ParseAgents2DB.jar get http
echo $name
else
echo $name ": no need to get new agents,there is enough in DB"
fi


