#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh build.sh
exec java -jar build/project1.jar "$@"
