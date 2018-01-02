#!/bin/bash
echo "server=1
rpcuser=test
rpcpassword=test123
rpcport=45443
rpcbind=0.0.0.0:45443
keypool=1500
testnet=1
rpcallowip=0.0.0.0/0
gen=0" > "${DATA_DIR}/reddcoin.conf"
echo "1" > "${DATA_DIR}/bootstrapped"
/init.sh $@