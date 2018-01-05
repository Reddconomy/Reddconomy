# Download

```
VERSION="`curl http://ci_deploy.frk.wf/Reddconomy_latest.txt`"
PASSWORD="N2lMuG106fRM4yJRcQyozzUnzF13tJid"

#Download spigot plugin
curl https://ci_deploy.frk.wf/Reddconomy-spigot.jar-$VERSION.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy-spigot.jar -k $PASSWORD

#Download service
curl https://ci_deploy.frk.wf/Reddconomy.jar-$VERSION.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy.jar -k $PASSWORD

```