
load test_helper

@test "config invoking help" {
  run $MUTAGEN config --help
  [ "$status" -eq 0 ]
  expect=$(file "./config/$BATS_TEST_NAME.txt")
  diff <(echo "$expect" ) <(echo "$output")
}

@test "config invoking import" {
  run $MUTAGEN config import "$BATS_TEST_DIRNAME/config/configurations.json"
  [ "$status" -eq 0 ]
  expect=$(file "./config/$BATS_TEST_NAME.txt")
  diff <(echo "$expect" ) <(echo "$output")
}

@test "config invoking import existing object" {
  run $MUTAGEN config import "$BATS_TEST_DIRNAME/config/configurations.json"
  [ "$status" -eq 0 ]
  expect=$(file "./config/$BATS_TEST_NAME.txt")
  diff <(echo "$expect" ) <(echo "$output")
}

@test "config invoking import on file not found" {
  run $MUTAGEN config import foo.json
  [ "$status" -eq 1 ]
}

@test "config invoking export" {
  run eval "$MUTAGEN config export | jq \".CFGAccessGroup[]\""
  [ "$status" -eq 0 ]
  expect=$(file "./config/$BATS_TEST_NAME.txt")
  diff <(echo "$expect" ) <(echo "$output")
}
