#!/bin/sh
set -eu
cd "$(dirname "$0")"
./build.sh
java -cp 'build:src:lib/*' Main
