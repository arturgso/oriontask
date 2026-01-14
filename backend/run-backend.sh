#!/bin/bash
set -a
if [ -f ../.env ]; then
    source ../.env
fi
set +a

./gradlew bootRun
