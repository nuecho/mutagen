test("[mutagen audio --help] should print audio usage", () => {
  assertMutagenResult(`audio --help`, "audio usage", 0);
});
