#!/usr/bin/env bash
function file() {
  local content=$( cat $BATS_TEST_DIRNAME/$1 )
  echo "$content"
}
