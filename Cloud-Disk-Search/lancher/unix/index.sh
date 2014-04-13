#!/bin/bash
#coding=utf8

echo Preparing to index ...
java -Xmx1g -Xms1g -Dfile.encoding=utf8 -jar Main.jar -index
echo Finishing index.
