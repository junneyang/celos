#!/usr/bin/env bash
set -x
set -e
./gradlew clean test celos-server:jar celos-ui:jar celos-ci:fatJar