test("[mutagen] should print usage with banner", () => {
  assertMutagenResult("", "usage with banner", 0);
});

test("[mutagen --help] should print usage", () => {
  assertMutagenResult("--help", "usage", 0);
});
