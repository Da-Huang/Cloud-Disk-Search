#!/usr/bin/env bash
#coding: utf8

echo Please input according to hints.
read -p "Enter the query:" query
echo The result is:
java -cp bin:lib/*: -Xmx4g -Xms4g -Dfile.encoding=utf8 \
ui.Main -search $query
