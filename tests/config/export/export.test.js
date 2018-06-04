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

test(`[mutagen config export] should export the config in raw format`, () => {
     const { code, output } = mutagen(`config export`);
     const exportedConfig = JSON.parse(output.stdout);

     expect(exportedConfig.CFGEnumerator).toBeDefined;
     expect(exportedConfig.CFGPerson).toBeDefined;
     expect(exportedConfig.CFGTenant).toBeDefined;
     expect(code).toBe(0);
   });

test(`[mutagen config export --format JSON] should export the config in json format`, () => {
     const { code, output } = mutagen(`config export --format JSON`);
     const exportedConfig = JSON.parse(output.stdout);

     expect(exportedConfig.enumerators).toBeDefined;
     expect(exportedConfig.persons).toBeDefined;
     expect(exportedConfig.tenants).toBeDefined;
     expect(code).toBe(0);
   });