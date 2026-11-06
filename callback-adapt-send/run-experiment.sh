#!/bin/bash
# usage: ./run-experiment.sh <runName> <maxImages> <periodMs> <pctGI>
cd "$(dirname "$0")"
NAME=$1; shift
MAX=$1
mkdir -p results
pkill -f server.jar 2>/dev/null
rm -rf team1
rm -f throughput-*.csv

java -jar server/build/libs/server.jar > results/$NAME-server.log 2>&1 &
SERVER_PID=$!
sleep 3

./run-clients.sh "$@" > results/$NAME-clients.log 2>&1
sleep 2
kill $SERVER_PID
sleep 1

mv throughput-*.csv results/$NAME.csv
COUNT=$(find team1 -name '*.jpg' | wc -l)
echo "$NAME stored_files=$COUNT expected=$((3*MAX))" | tee -a results/summary.txt
