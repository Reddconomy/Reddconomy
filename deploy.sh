 #!/bin/bash

 set -e

if [ "$BINTRAY_USER" == "" ];
then
    echo "BINTRAY_USER is not set"
    exit 1
fi

if [ "$BINTRAY_API_KEY" == "" ];
then
    echo "BINTRAY_API_KEY is not set"
    exit 1
fi

if [ "$TRAVIS_PULL_REQUEST" != "false" ];
then
    exit 0
fi

VERSION=$TRAVIS_COMMIT
IS_RELEASE=0

if [ "$TRAVIS_TAG" != "" ]; 
then 
    VERSION=$TRAVIS_TAG
    IS_RELEASE=1
fi

mv build/libs/Reddconomy-fat.jar Reddconomy-$VERSION.jar
mv build/libs/Reddconomy-sponge-fat.jar Reddconomy-sponge-$VERSION.jar

if [ "$IS_RELEASE" == "1" ]; 
then
      echo $VERSION > Reddconomy-release_latest.txt
      curl -X PUT  -T  Reddconomy-release_latest.txt -u$BINTRAY_USER:$BINTRAY_API_KEY\
        "https://api.bintray.com/content/reddconomy/Reddconomy/main/latest/Reddconomy-release_latest.txt?publish=1&override=1"

else 
    OLD_VERSION="`curl -u$BINTRAY_USER:$BINTRAY_API_KEY --silent \
    https://api.bintray.com/content/reddconomy/Reddconomy/main/latest/Reddconomy_latest.txt`"
    if [ "$OLD_VERSION" != "" ];
    then
        echo "Delete old version"
        curl -X DELETE   -u$BINTRAY_USER:$BINTRAY_API_KEY\
        "https://api.bintray.com/content/reddconomy/Reddconomy/backend/$OLD_VERSION/Reddconomy-$OLD_VERSION.jar?publish=1&override=1"
        curl -X DELETE   -u$BINTRAY_USER:$BINTRAY_API_KEY\
        "https://api.bintray.com/content/reddconomy/Reddconomy/sponge/$OLD_VERSION/Reddconomy-sponge-$OLD_VERSION.jar?publish=1&override=1"
    fi
    echo $VERSION > Reddconomy_latest.txt
    curl -X PUT   -T  Reddconomy_latest.txt -u$BINTRAY_USER:$BINTRAY_API_KEY\
        "https://api.bintray.com/content/reddconomy/Reddconomy/main/latest/Reddconomy_latest.txt?publish=1&override=1"
fi 
curl -X PUT    -T  Reddconomy-$VERSION.jar -u$BINTRAY_USER:$BINTRAY_API_KEY\
        "https://api.bintray.com/content/reddconomy/Reddconomy/backend/$VERSION/Reddconomy-$VERSION.jar?publish=1&override=1"
curl -X PUT    -T  Reddconomy-sponge-$VERSION.jar -u$BINTRAY_USER:$BINTRAY_API_KEY\
        "https://api.bintray.com/content/reddconomy/Reddconomy/sponge/$VERSION/Reddconomy-sponge-$VERSION.jar?publish=1&override=1"
 