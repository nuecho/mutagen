test("[mutagen] should print usage with banner", () => {
  const {code, output} = mutagen();

  expect(output).toMatchSnapshot("usage with banner");
  expect(code).toBe(0);
});

test("[mutagen --help] should print usage", () => {
  const {code, output} = mutagen(`--help`);

  expect(output).toMatchSnapshot("usage");
  expect(code).toBe(0);
});
