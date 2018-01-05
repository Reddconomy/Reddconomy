#!/bin/bash
echo "server=1
rpcuser=test
rpcpassword=test123
rpcport=45443
rpcallowip=0.0.0.0/0
keypool=1500

###########DOGE
testnet=1
addnode=testnets.chain.so
addnode=suchdig.com
addnode=testdoge.lionservers.de
addnode=senatorwhiskers.com
gen=1
genproclimit=1
################
" > "${DATA_DIR}/${CONFIG_FILE}"
echo "1" > "${DATA_DIR}/bootstrapped"
/init.sh $@