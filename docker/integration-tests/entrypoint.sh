#!/bin/bash

wait-for-it.sh "$CONFIG_SERVER_HOST:$CONFIG_SERVER_PORT" -s -t "$WAIT_TIMEOUT" -- jest \
  --setupFiles=/usr/src/app/tests/setupTests.js \
  --rootDir=/usr/src/app/ \
  "$@"