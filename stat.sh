#!/usr/bin/env bash
#coding=utf8

echo Preparing to stat ...
java -cp bin:lib/*: -Xmx1g -Xms1g -Dfile.encoding=utf8 \
ui.Main -stat
echo Finishing stat.
