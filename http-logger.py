#!/usr/bin/env python3
import http.server
import socketserver
import socket
import requests
from datetime import datetime
import json
import threading
import sys

LOG_FILE = '/var/log/jenkins/http-requests.log'
JENKINS_URL_INTERNAL = 'http://localhost:8080'
PROXY_PORT = 8090

class HTTPLogger(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        self.log_request()
        self.proxy_request()
    
    def do_POST(self):
        self.log_request()
        self.proxy_request()
    
    def do_PUT(self):
        self.log_request()
        self.proxy_request()
    
    def do_DELETE(self):
        self.log_request()
        self.proxy_request()
    
    def do_HEAD(self):
        self.log_request()
        self.proxy_request()
    
    def log_request(self):
        """Log HTTP request in NCSA format"""
        timestamp = datetime.now().strftime('%d/%b/%Y:%H:%M:%S %z')
        client_ip = self.client_address[0]
        method = self.command
        path = self.path
        http_version = self.request_version.split('/')[-1] if '/' in self.request_version else '1.1'
        
        # Get user-agent and other headers
        user_agent = self.headers.get('User-Agent', '-')
        referer = self.headers.get('Referer', '-')
        
        # NCSA format: IP - USER [TIMESTAMP] "METHOD PATH HTTP/VERSION" STATUS_CODE SIZE "REFERER" "USER_AGENT"
        log_entry = f'{client_ip} - - [{timestamp}] "{method} {path} HTTP/{http_version}" 200 0 "{referer}" "{user_agent}"\n'
        
        try:
            with open(LOG_FILE, 'a') as f:
                f.write(log_entry)
            sys.stderr.write(f"Logged: {method} {path}\n")
            sys.stderr.flush()
        except Exception as e:
            sys.stderr.write(f"Error logging request: {e}\n")
            sys.stderr.flush()
    
    def proxy_request(self):
        """Forward request to Jenkins"""
        try:
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length) if content_length > 0 else b''
            
            # Prepare headers
            headers = {k: v for k, v in self.headers.items() if k.lower() not in ['host', 'connection']}
            headers['Connection'] = 'close'
            
            # Make request to Jenkins
            url = f'{JENKINS_URL_INTERNAL}{self.path}'
            response = requests.request(
                method=self.command,
                url=url,
                headers=headers,
                data=body if body else None,
                allow_redirects=False,
                timeout=10
            )
            
            # Send response back
            self.send_response(response.status_code)
            for header, value in response.headers.items():
                if header.lower() not in ['content-encoding', 'transfer-encoding']:
                    self.send_header(header, value)
            self.end_headers()
            
            self.wfile.write(response.content)
        except Exception as e:
            self.send_response(502)
            self.send_header('Content-Type', 'text/plain')
            self.end_headers()
            self.wfile.write(f'Gateway Error: {str(e)}'.encode())
            sys.stderr.write(f"Proxy error: {e}\n")
            sys.stderr.flush()
    
    def log_message(self, format, *args):
        """Suppress default logging"""
        pass

def start_proxy():
    """Start HTTP proxy server"""
    Port = PROXY_PORT
    Handler = HTTPLogger
    
    try:
        with socketserver.TCPServer(("0.0.0.0", Port), Handler) as httpd:
            sys.stderr.write(f"HTTP Logger proxy listening on port {Port}\n")
            sys.stderr.write(f"Forwarding to {JENKINS_URL_INTERNAL}\n")
            sys.stderr.write(f"Logging to {LOG_FILE}\n")
            sys.stderr.flush()
            httpd.serve_forever()
    except Exception as e:
        sys.stderr.write(f"Failed to start proxy: {e}\n")
        sys.stderr.flush()

if __name__ == '__main__':
    start_proxy()
