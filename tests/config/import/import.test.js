test(`[mutagen config import --help] should print import usage`, () => {
  const { code, output } = mutagen(`config import --help`);

  expect(output).toMatchSnapshot("import usage");
  expect(code).toBe(0);
});

test(`[mutagen config import] should fail when file to import does not exist`, () => {
  const { code, output } = mutagen(`config import /tmp/foo`);

  expect(output).toMatchSnapshot("no such file or directory");
  expect(code).not.toBe(0);
});
