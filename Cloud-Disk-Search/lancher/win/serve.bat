@echo off
color 0E
echo 开启服务
java -Xms1g -Xmx1g -jar Main.jar -serve
echo 服务终止
pause