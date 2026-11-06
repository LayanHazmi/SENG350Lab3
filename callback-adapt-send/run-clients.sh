#!/bin/bash
for id in 1 2 3; do
  java -jar client/build/libs/client.jar ../$id $1 $2 $3 &
done
wait