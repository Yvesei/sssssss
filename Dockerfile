FROM jenkins/jenkins:latest

USER root

RUN mkdir -p /var/log/jenkins && chmod 777 /var/log/jenkins

RUN cat > /usr/local/bin/jenkins-with-logging.sh << 'EOF'
#!/bin/bash
set -e

mkdir -p /var/log/jenkins
chmod 777 /var/log/jenkins

# Export Jenkins args to enable Winstone access logging
export JENKINS_ARGS="--accessLoggerClassName=winstone.accesslog.SimpleAccessLogger --simpleAccessLogger.format=combined --simpleAccessLogger.file=/var/log/jenkins/access.log"

exec /usr/local/bin/jenkins.sh
EOF

RUN chmod +x /usr/local/bin/jenkins-with-logging.sh

USER jenkins

ENTRYPOINT ["/usr/local/bin/jenkins-with-logging.sh"]
