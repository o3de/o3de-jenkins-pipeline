FROM jenkins/jenkins:2.414.3-lts-jdk11

# Using JENKINS_HOME and REF set on the base image
ARG uid=1000
ARG gid=1000
ARG user=jenkins
ARG JENKINS_LOCAL=/var/jenkins_local

ENV JENKINS_LOCAL $JENKINS_LOCAL

USER root

RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
  && unzip awscliv2.zip \
  && ./aws/install

# Prevent Jenkins server from pulling LFS objects
RUN git lfs uninstall --system

RUN mkdir -p $JENKINS_LOCAL \
  && chown ${uid}:${gid} $JENKINS_LOCAL

USER $user

# Create custom directories and logs. 
# Jenkins cannot create these on its own and will fail to start if they do not exist.
RUN mkdir ${JENKINS_LOCAL}/war \
  && mkdir ${REF}/logs \ 
  && touch ${REF}/logs/gc.log

COPY plugins.txt ${REF}/plugins.txt
RUN jenkins-plugin-cli --plugin-file ${REF}/plugins.txt

ENV CASC_JENKINS_CONFIG=${JENKINS_LOCAL}/configs
COPY configs/ ${JENKINS_LOCAL}/configs/
COPY jobdsl/ ${JENKINS_LOCAL}/jobdsl/

ENV JENKINS_OPTS="--webroot=${JENKINS_LOCAL}/war"
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false \
  -Dorg.jenkinsci.plugins.durabletask.BourneShellScript.HEARTBEAT_CHECK_INTERVAL=86400 \
  -Djenkins.model.Jenkins.logStartupPerformance=true \
  -Xms8g \
  -Xmx8g \
  -XX:+AlwaysPreTouch \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${JENKINS_HOME}/heapdump \
  -XX:+UseG1GC \
  -XX:+UseStringDeduplication \
  -XX:+ParallelRefProcEnabled \
  -XX:+DisableExplicitGC \
  -XX:+UnlockDiagnosticVMOptions \
  -Xlog:gc:${JENKINS_HOME}/logs/gc.log \
  -Xlog:gc*=warning \
  -XX:ErrorFile=${JENKINS_HOME}/logs/hs_err_%p.log \
  -XX:LogFile=${JENKINS_HOME}/logs/vm.log"

# Start jenkins using a custom entrypoint. See start-jenkins.sh for details.
COPY start-jenkins.sh /usr/local/bin/start-jenkins.sh
ENTRYPOINT ["/usr/bin/tini", "--", "/usr/local/bin/start-jenkins.sh"]
