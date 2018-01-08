# Download

## Linux
##### Requires:
-  curl | openssl

##### SH:
```
VERSION="`curl https://ci_deploy.frk.wf/Reddconomy_latest.txt`"
PASSWORD="N2lMuG106fRM4yJRcQyozzUnzF13tJid"

#Download sponge plugin
curl https://ci_deploy.frk.wf/Reddconomy-sponge.jar-$VERSION.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy-sponge.jar -k $PASSWORD

#Download service
curl https://ci_deploy.frk.wf/Reddconomy.jar-$VERSION.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy.jar -k $PASSWORD

```

## Windows
##### Requires:
- [Chocolatey Package Manager](https://chocolatey.org/install)
- [OpenSSL](https://sourceforge.net/projects/openssl/files/latest/download?source=typ_redirect)

##### Things to do in Admin's CMD:
```
choco install curl
```

##### Batch:
```
curl https://ci_deploy.frk.wf/Reddconomy_latest.txt -o Reddconomy_latest.txt
set /p VERSION=<Reddconomy_latest.txt
rm Reddconomy_latest.txt
set PASSWORD="N2lMuG106fRM4yJRcQyozzUnzF13tJid"

REM Download spigot plugin
curl https://ci_deploy.frk.wf/Reddconomy-spigot.jar-%VERSION%.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy-spigot.jar -k %PASSWORD%

REM Download service
curl https://ci_deploy.frk.wf/Reddconomy.jar-%VERSION%.aes256 | openssl aes-256-cbc  -md sha256 -d -out Reddconomy.jar -k %PASSWORD%
```




# Docker

### Build

```
cd src/docker
docker build  --compress=true -t reddconomy_test:amd64 --label amd64 --rm --force-rm=true .
```

### Run

```
docker run --name=reddconomy_test -d -p 8099:8099 -p 45443:45443 -v /srv/reddconomy_testnet:/data --restart=always reddconomy_test:amd64
```

### Debug

#### Logs

```
docker logs reddconomy_test --tail 50
```

#### Wallet info

```
docker exec -it reddconomy_test  bash -c '/opt/dogecoin/bin/dogecoin-cli   -conf="${DATA_DIR}/${CONFIG_FILE}" -datadir="${DATA_DIR}" getwalletinfo'
```

#### Wallet available balance
```
docker exec -it reddconomy_test  bash -c '/opt/dogecoin/bin/dogecoin-cli  -conf="${DATA_DIR}/${CONFIG_FILE}" -datadir="${DATA_DIR}" getbalance'
```