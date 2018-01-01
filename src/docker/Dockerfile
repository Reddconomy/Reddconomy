FROM riccardoblb/reddcoind-docker:amd64


ADD init_testnet.sh /init_testnet.sh


RUN \
    chmod +x /init_testnet.sh &&\
    apt-get -y install software-properties-common &&\
    add-apt-repository -y ppa:webupd8team/java &&\
        apt-get update -y &&\
    echo 'oracle-java8-installer shared/accepted-oracle-license-v1-1 boolean true' | debconf-set-selections  &&\
    DEBIAN_FRONTEND=noninteractive apt-get -y install oracle-java8-installer

ENTRYPOINT [ "/init_testnet.sh" ]

