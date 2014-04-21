#!/bin/bash
#coding=utf8

echo Preparing to index ...
java -Xmx1g -Xms1g -Dfile.encoding=utf8 -jar Main.jar -index
echo Finishing index.

java -jar $MAIL -F project0002@yeah.net -P sewm1220 -T dhuang.cn@gmail.com \
	-S "Cloud-Disk-Search indexing finish."
