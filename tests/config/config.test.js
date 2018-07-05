test("[mutagen config --help] should print config usage", () => {
  assertMutagenResult(`config --help`, "config usage", 0);
});
