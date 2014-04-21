#!/bin/bash
#coding=utf8

CLASSPATH=bin
for file in lib/*.jar; do CLASSPATH=$file:$CLASSPATH; done

echo Please input according to hints.
read -p "Enter the query:" query
echo The result is:
java -cp $CLASSPATH -Xmx1g -Xms1g -Dfile.encoding=utf8 \
ui.Main -search $query
