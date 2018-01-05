# Download

## Linux
##### Requires:
- wget | curl | openssl

##### SH:
```
VERSION="`curl http://ci_deploy.frk.wf/Reddconomy_latest.txt`"
PASSWORD="N2lMuG106fRM4yJRcQyozzUnzF13tJid"

#Download spigot plugin
curl https://ci_deploy.frk.wf/Reddconomy-spigot.jar-$VERSION.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy-spigot.jar -k $PASSWORD

#Download service
curl https://ci_deploy.frk.wf/Reddconomy.jar-$VERSION.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy.jar -k $PASSWORD

```

## Windows
##### Requires:
- [Chocolatey Package Manager](https://chocolatey.org/install)

##### Things to do in Admin's CMD:
```
choco install wget curl openssl
```

##### Batch:
```
wget http://ci_deploy.frk.wf/Reddconomy_latest.txt
set /p VERSION=<Reddconomy_latest.txt
set PASSWORD="N2lMuG106fRM4yJRcQyozzUnzF13tJid"

REM Download spigot plugin
curl https://ci_deploy.frk.wf/Reddconomy-spigot.jar-%VERSION%.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy-spigot.jar -k %PASSWORD%

REM Download service
curl https://ci_deploy.frk.wf/Reddconomy.jar-%VERSION%.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy.jar -k %PASSWORD%
```
