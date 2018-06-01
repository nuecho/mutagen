test(`[mutagen config export --help] should print import usage`, () => {
  const { code, output } = mutagen(`config export --help`);

  expect(output).toMatchSnapshot("export usage");
  expect(code).toBe(0);
});

test(`[mutagen config export] should warn the user when an invalid encoding is specified`, () => {
  const { code, output } = mutagen(`-e invalidEncoding config export`);

  expect(output.stderr).toMatchSnapshot("export invalid encoding warning");
  expect(code).toBe(0);
});

test(`[mutagen config export] should not warn the user when no encoding is specified`, () => {
  const { code, output } = mutagen(`config export`);

  expect(output.stderr).toMatchSnapshot("export without warning");
  expect(code).toBe(0);
});
