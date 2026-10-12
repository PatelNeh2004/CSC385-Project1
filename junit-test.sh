#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh build.sh
JUNIT_JAR=build/lib/junit-platform-console-standalone-1.11.4.jar
if [ ! -f "$JUNIT_JAR" ]; then
    mkdir -p build/lib
    curl --fail --location --output "$JUNIT_JAR" https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar
fi
mkdir -p build/junit-classes build/junit-results
find src/test/java src/junit/java -name '*.java' -print > build/junit-sources.txt
javac --release 17 -encoding UTF-8 -cp "build/classes:$JUNIT_JAR" -d build/junit-classes @build/junit-sources.txt
java -jar "$JUNIT_JAR" execute --class-path build/classes:build/junit-classes --scan-class-path --include-classname '.*SpecificationTest' --reports-dir build/junit-results --details summary --disable-ansi-colors
