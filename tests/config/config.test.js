test("[mutagen config --help] should print config usage", () => {
  const {code, output} = mutagen(`config --help`);

  expect(output).toMatchSnapshot("config usage");
  expect(code).toBe(0);
});
