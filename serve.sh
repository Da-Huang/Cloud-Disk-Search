#!/bin/bash
#coding=utf8

CLASSPATH=bin
for file in lib/*.jar; do CLASSPATH=$file:$CLASSPATH; done

echo Running the TCP Server.
java -cp $CLASSPATH -Xmx1g -Xms1g -Dfile.encoding=utf8 \
ui.Main -serve
echo Finishing index.
