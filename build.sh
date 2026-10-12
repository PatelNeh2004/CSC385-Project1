#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
find src/main/java -name '*.java' -print > build/main-sources.txt
javac --release 17 -encoding UTF-8 -d build/classes @build/main-sources.txt
jar --create --file build/project1.jar --main-class csc241.project1.Main -C build/classes .
