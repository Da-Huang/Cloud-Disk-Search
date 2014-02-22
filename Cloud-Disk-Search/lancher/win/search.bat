@echo off
color 0E
echo 请按照提示输入
:RecQuery
set /p query=请输入查询关键词（回车键结束）：
if "%query%"=="" (
	echo 查询不能为空
	goto RecQuery
)
echo 查询结果为：
java -Xms1g -Xmx1g -jar Main.jar -search "%query%"
pause