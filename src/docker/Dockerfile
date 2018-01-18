FROM riccardoblb/dogecoin:amd64
#FROM riccardoblb/riccardoblb/reddcoind-docker:amd64

ADD init_testnet.sh /init_testnet.sh
ADD init_reddconomy.sh /init_reddconomy.sh
ADD updater.sh /updater.sh

ADD supervisord.conf /etc/supervisor/conf.d/supervisord.conf

RUN \
    chmod +x /init_testnet.sh &&\
    chmod +x /init_reddconomy.sh &&\
    chmod +x /updater.sh &&\
    apt-get update -y &&\
    apt-get -y install supervisor curl&&\
    JDK_VERSION="`curl -s https://ci_public_deploy.frk.wf/jdk8_latest.txt`" && \
    echo "Download java $JDK_VERSION" && \
    curl "https://ci_deploy.frk.wf/jdk8_lin64-$JDK_VERSION.tar.gz" -o /tmp/jdk8.tar.gz && \
    mkdir -p /opt/java &&\
    tar -xzf /tmp/jdk8.tar.gz -C /opt/java &&\
    ls /opt/java &&\
    mkdir /opt/reddconomy    &&\
    mkdir -p /data/

ENTRYPOINT [ "/usr/bin/supervisord" ]
