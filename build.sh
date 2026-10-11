#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
javac -encoding UTF-8 -cp 'lib/*' -sourcepath src -d build src/Main.java
echo 'Amar Dokan compiled successfully.'
