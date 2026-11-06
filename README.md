# SENG 350 Lab 03: Observer (Publish/Subscribe) with ZeroC Ice callbacks

Group 18

## Overview
A server (the Subject) receives images from three clients (buses). Each bus
subscribes with an Ice callback proxy. Every image is stored under
`team1/<busId>/`. If an image is tagged GENERALINTEREST, the server calls
`notifyCallback("GENERALINTEREST from <busId>")` on every subscribed bus.

## Prerequisites
Java 17+ and Gradle (the Ice library is pulled in by the build).

## Build
    cd callback-adapt-send
    gradle build

## Run (server and clients on the same machine, two terminals)
    ./run-server.sh                           # terminal 1
    ./run-clients.sh <maxImages> <periodMs> <pctGI>     # terminal 2
Example: `./run-clients.sh 300 200 0`

Client arguments: image directory (the busId: ../1, ../2, ../3), maximum
number of images (0 = all), period in ms, percentage tagged GENERALINTEREST.
Test images go in folders `1`, `2`, `3` next to `callback-adapt-send`.

Ports: server 18000; bus callbacks 18001-18003.

## Output
- Stored images: `callback-adapt-send/team1/<busId>/`
- Throughput: the server writes a per-second CSV (`throughput-*.csv`).

## Results
Raw data, averages, and plots: `callback-adapt-send/results/`
(`throughput-data.xlsx`, `averages.csv`, `all-runs-raw.csv`, `summary.txt`).