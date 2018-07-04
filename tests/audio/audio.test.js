test("[mutagen audio --help] should print audio usage", () => {
  const {code, output} = mutagen(`audio --help`);

  expect(output).toMatchSnapshot("audio usage");
  expect(code).toBe(0);
});
