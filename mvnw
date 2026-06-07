#!/usr/bin/env bash
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$DIR/.mvn/wrapper/maven-wrapper.jar" "$@"
