test("[mutagen config --help] should print config usage", () => {
  assertMutagenResult(`config --help`, "config usage", 0);
});

test(`[mutagen config export] should not allow connecting tls if ca certificate is missing`, () => {
  const { code, output } = mutagen(`--env configserver-tls config export`);

  expect(output.stderr).toMatchSnapshot("error while connecting");
  expect(code).toBe(1);
});

test(`[mutagen config export --trust-insecure-certificate] should allow connecting tls even without ca certificate`, () => {
  const { code, output } = mutagen(`--env configserver-tls config export --trust-insecure-certificate`);

  expect(output.stderr).toMatchSnapshot("export without exception");
  expect(code).toBe(0);
});