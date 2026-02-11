FROM jenkins/jenkins:latest

# Switch to root to create directory and set ownership
USER root

# Create log directory and set ownership to Jenkins user
RUN mkdir -p /var/log/jenkins \
    && chown -R jenkins:jenkins /var/log/jenkins

# Create the wrapper script
RUN cat > /usr/local/bin/jenkins-with-logging.sh << 'EOF'
#!/bin/bash
set -e

# Ensure the log directory exists (ownership already correct)
mkdir -p /var/log/jenkins

# Export Jenkins args to enable Winstone access logging
export JENKINS_ARGS="--accessLoggerClassName=winstone.accesslog.SimpleAccessLogger --simpleAccessLogger.format=combined --simpleAccessLogger.file=/var/log/jenkins/access.log"

# Start Jenkins
exec /usr/local/bin/jenkins.sh
EOF

RUN chmod +x /usr/local/bin/jenkins-with-logging.sh

# Switch back to Jenkins user for runtime
USER jenkins

# Use the wrapper script as entrypoint
ENTRYPOINT ["/usr/local/bin/jenkins-with-logging.sh"]