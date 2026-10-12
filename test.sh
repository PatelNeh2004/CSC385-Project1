#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh build.sh
mkdir -p build/test-classes
find src/test/java -name '*.java' -print > build/test-sources.txt
javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes @build/test-sources.txt
java -cp build/classes:build/test-classes csc241.project1.GameTests
