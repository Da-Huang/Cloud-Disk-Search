#!/bin/bash
#coding=utf8

echo Running the TCP Server.
java -Xmx1g -Xms1g -Dfile.encoding=utf8 -jar Main.jar -serve
echo TCP Server Abort.
