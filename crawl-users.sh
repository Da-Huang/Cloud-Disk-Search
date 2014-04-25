#!/bin/bash
#coding=utf8

CLASSPATH=bin
for file in lib/*.jar; do CLASSPATH=$file:$CLASSPATH; done

echo Preparing to crawling ...
java -cp $CLASSPATH -Xmx1g -Xms1g -Dfile.encoding=utf8 \
ui.Main -crawl-users
echo Finishing crawling.

#java -jar $MAIL -F project0002@yeah.net -P sewm1220 -T dhuang.cn@gmail.com \
#	-S "Cloud-Disk-Search indexing finish."
