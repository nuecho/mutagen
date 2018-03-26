
load test_helper

@test "core invoking version" {
  run $MUTAGEN --version
  [ "$status" -eq 0 ]
  [ "$output" = "mutagen version ${VERSION}" ]
}

@test "core invoking help" {
  run $MUTAGEN --help
  [ "$status" -eq 0 ]
  expect=$(file "./core/$BATS_TEST_NAME.txt")
  diff <(echo "$expect" ) <(echo "$output")
}
