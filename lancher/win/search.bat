@echo off
color 0E
echo �밴����ʾ����
:RecQuery
set /p query=�������ѯ�ؼ��ʣ��س�����������
if "%query%"=="" (
	echo ��ѯ����Ϊ��
	goto RecQuery
)
echo ��ѯ���Ϊ��
java -Xms1g -Xmx1g -jar Main.jar -search "%query%"
pause