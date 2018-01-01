

function paygate {
    ./gradle paygate
    mkdir -p dist/
    mv build/libs/*paygate*-fat.jar dist/reddconomy_paygate.jar
}