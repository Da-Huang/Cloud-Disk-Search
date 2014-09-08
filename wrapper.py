#!/usr/bin/env python
#coding: utf8

import json
import socket
import cherrypy


class CloudDiskSearcher(object):
  @cherrypy.expose
  def search(self, keywords, fileType='all', start=0, limit=100):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect(('162.105.205.253', 7779));
    data = json.dumps({
      'type' : 'search',
      'fileType' : fileType,
      'query' : keywords,
      'start' : start,
      'limit' : limit,
    })
    sock.sendall(data);
    sock.shutdown(socket.SHUT_WR);
    BUFFER_SIZE = 1024

    data = sock.recv(BUFFER_SIZE)
    res = data
    while data:
      data = sock.recv(BUFFER_SIZE)
      res += data

    cherrypy.response.headers['Content-Type'] = 'application/json;charset=utf8'
    cherrypy.response.headers['Content-Encoding'] = 'gzip'
    return res

  @cherrypy.expose
  def hot(self):
    return "Hello World"


app = cherrypy.tree.mount(CloudDiskSearcher(), '/', config={
  '/' : {
    'tools.sessions.on' : True,
    'tools.CORS.on' : True,
  }
})
cherrypy.config.update({
  'server.socket_host' : '162.105.205.253',
  'server.socket_port' : 7777,
})

def CORS():
  cherrypy.response.headers['Access-Control-Allow-Origin'] = '*'
cherrypy.tools.CORS = cherrypy.Tool('before_finalize', CORS)


if __name__ == '__main__':
  cherrypy.quickstart(app)

