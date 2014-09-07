#!/bin/bash
#coding: utf8

echo Preparing to index ...
java -cp bin:lib/*: -Xms4g -Xmx4g -Dfile.encoding=utf8 \
ui.Main -index
echo Finishing index.

