#!/usr/bin/env python
#coding: utf8

import SimpleHTTPServer
import SocketServer
import urllib
import json
import cgi
import socket


class SearchHTTPServer(SimpleHTTPServer.SimpleHTTPRequestHandler):

  def do_POST(self):
    contentLength = long(self.headers.getheader('Content-Length'))
    content = self.rfile.read(contentLength)
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect(('162.105.205.253', 7779));
    sock.send(content);
    sock.shutdown(socket.SHUT_WR);
    res = sock.recv(10240)
    self.send_response(200)

    self.send_header('Access-Control-Allow-Origin', '*')
    self.send_header('Access-Control-Allow-Methods', '*')
    self.send_header('Access-Control-Allow-Headers', '*')
    self.send_header('Content-Encoding', 'gzip')
    self.send_header('Content-Type', 'application/json;charset=utf8')
    self.end_headers()
    self.wfile.write(res)


if __name__ == '__main__':
  PORT  = 7777
  httpd = SocketServer.ThreadingTCPServer(('', PORT), SearchHTTPServer)
  httpd.serve_forever()

