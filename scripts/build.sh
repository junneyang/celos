#!/usr/bin/env bash
set -x
set -e
CELOS_VERSION=test ./gradlew clean test celos-server:fatJar celos-ui:jar celos-ci:fatJar
