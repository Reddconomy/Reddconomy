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
    apt-get -y install software-properties-common supervisor curl&&\
    add-apt-repository -y ppa:webupd8team/java &&\
    apt-get update -y &&\
    echo 'oracle-java8-installer shared/accepted-oracle-license-v1-1 boolean true' | debconf-set-selections  &&\
    DEBIAN_FRONTEND=noninteractive apt-get -y install oracle-java8-installer &&\
    mkdir /opt/reddconomy    &&\
    mkdir -p /data/

ENTRYPOINT [ "/usr/bin/supervisord" ]
