FROM jenkins/jenkins:latest

# Switch to root to create directory and set ownership
USER root

# Create log directory and set ownership to Jenkins user
RUN mkdir -p /var/log/jenkins \
    && chown -R jenkins:jenkins /var/log/jenkins

# Create the wrapper script that passes args directly to Jenkins
RUN cat > /usr/local/bin/jenkins-with-logging.sh << 'EOF'
#!/bin/bash
set -e

# Ensure the log directory exists with correct permissions
mkdir -p /var/log/jenkins
chmod 777 /var/log/jenkins

# Get the Jenkins war file location and run with access logging args
cd /usr/share/jenkins

# Run Java directly with access logging enabled
java -Dcom.sun.akuma.Daemon=false \
     -Djava.awt.headless=true \
     -jar jenkins.war \
     --httpPort=8080 \
     --accessLoggerClassName=winstone.accesslog.SimpleAccessLogger \
     --simpleAccessLogger.format=combined \
     --simpleAccessLogger.file=/var/log/jenkins/access.log
EOF

RUN chmod +x /usr/local/bin/jenkins-with-logging.sh

# Switch back to Jenkins user for runtime
USER jenkins

# Use the wrapper script as entrypoint
ENTRYPOINT ["/usr/local/bin/jenkins-with-logging.sh"]