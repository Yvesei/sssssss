FROM jenkins/jenkins:latest

USER root

RUN mkdir -p /var/log/jenkins && chmod 777 /var/log/jenkins

RUN apt-get update && apt-get install -y python3 && rm -rf /var/lib/apt/lists/*

COPY http-logger.py /usr/local/bin/
RUN chmod +x /usr/local/bin/http-logger.py

RUN cat > /usr/local/bin/jenkins-with-logging.sh << 'EOF'
#!/bin/bash
set -e

mkdir -p /var/log/jenkins
chmod 777 /var/log/jenkins

# Start HTTP logger proxy in background
python3 /usr/local/bin/http-logger.py &

# Give it time to start
sleep 2

exec /usr/local/bin/jenkins.sh
EOF

RUN chmod +x /usr/local/bin/jenkins-with-logging.sh

USER jenkins

ENTRYPOINT ["/usr/local/bin/jenkins-with-logging.sh"]
