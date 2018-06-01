#!/bin/bash

echo The container will run: npm test -- "$@"

npm install && wait-for-it.sh "$CONFIG_SERVER_HOST:$CONFIG_SERVER_PORT" -s -t "$WAIT_TIMEOUT" -- npm test -- "$@"