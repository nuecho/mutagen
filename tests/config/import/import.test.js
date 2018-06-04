const INVALID_TENANT_CONFIGURATION_PATH = getResourcePath("import/switch-with-invalid-tenant-config.json");
const MULTIPLE_OBJECTS_CONFIGURATION_PATH = getResourcePath("import/multiple-objects-config.json");
const REPEATED_KEYS_CONFIGURATION_PATH = getResourcePath("import/same-key-switches-config.json");
const SWITCH_DEPENDENCIES_CONFIGURATION_PATH = getResourcePath(
  "config-objects/switch-dependencies-config.json"
);

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

test(`[mutagen config import file] should not import the object if it has an invalid tenant`, () => {
  mutagen(`config import ${SWITCH_DEPENDENCIES_CONFIGURATION_PATH}`);
  const { code, output } = mutagen(`config import ${INVALID_TENANT_CONFIGURATION_PATH}`);

  expect(output).toMatchSnapshot("no object has been imported");
  expect(code).toBe(1);
});

test(`[mutagen config import file] should import multiple objects of different types`, () => {
  const { code, output } = mutagen(`config import ${MULTIPLE_OBJECTS_CONFIGURATION_PATH}`);

  expect(output).toMatchSnapshot("every object has been imported");
  expect(code).toBe(0);
});

test(`[mutagen config import file] should only import objects with a unique key`, () => {
  const { code, output } = mutagen(`config import ${REPEATED_KEYS_CONFIGURATION_PATH}`);

  expect(output).toMatchSnapshot("only one object with the key has been imported");
  expect(code).toBe(0);
});
