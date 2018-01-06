#!/bin/bash
PREVIOUS_VERSION="`curl https://ci_deploy.frk.wf/Reddconomy_latest.txt`"
while true;
do
    NEW_VERSION="`curl https://ci_deploy.frk.wf/Reddconomy_latest.txt`"
    if [ "$PREVIOUS_VERSION" != "$NEW_VERSION" ];
    then
        echo "Restart & update"
        PREVIOUS_VERSION=$NEW_VERSION
        supervisorctl restart reddconomy
    fi
    sleep 60
done