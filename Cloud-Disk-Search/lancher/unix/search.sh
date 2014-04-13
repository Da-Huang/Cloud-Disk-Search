#!/bin/bash
#coding=utf8

echo Please input according to hints.
read -p "Enter the query:" query
echo The result is:
java -Xmx1g -Xms1g -Dfile.encoding=utf8 -jar Main.jar -search query
