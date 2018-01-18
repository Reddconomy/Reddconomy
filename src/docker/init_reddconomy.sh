#!/bin/bash
cd /opt/reddconomy

VERSION="`curl https://ci_deploy.frk.wf/Reddconomy_latest.txt`" 
PASSWORD="N2lMuG106fRM4yJRcQyozzUnzF13tJid" 
curl https://ci_deploy.frk.wf/Reddconomy.jar-$VERSION.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy.jar -k $PASSWORD 

echo '{"bind_ip":"0.0.0.0"}' > /data/reddconomy.json
/opt/java/bin/java -jar Reddconomy.jar /data/reddconomy.json
