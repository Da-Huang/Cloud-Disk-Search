#!/bin/bash
#coding: utf8

echo Running the TCP Server.
java -cp bin:lib/*: -Xmx4g -Xms4g -Dfile.encoding=utf8 \
ui.Main -serve
echo Finishing index.
