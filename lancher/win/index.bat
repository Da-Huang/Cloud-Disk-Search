@echo off
color 0E
echo 准备建立索引
java -Xms1g -Xmx1g -jar Main.jar -index
echo 索引建立完成
pause