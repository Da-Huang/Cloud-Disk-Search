#!/usr/bin/env bash
#coding: utf8

echo Preparing to crawling ...
java -cp bin:lib/*: -Xmx4g -Xms4g -Dfile.encoding=utf8 \
ui.Main -crawl-files
echo Finishing crawling.
