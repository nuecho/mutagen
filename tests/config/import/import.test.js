const INVALID_TENANT_CONFIGURATION_PATH = getResourcePath("import/switch-with-invalid-tenant-config.json");
const MULTIPLE_OBJECTS_CONFIGURATION_PATH = getResourcePath("import/multiple-objects-config.json");
const REPEATED_KEYS_CONFIGURATION_PATH = getResourcePath("import/same-key-switches-config.json");
const CYCLE_CONFIGURATION_PATH = getResourcePath("import/cycle-config.json");
const MISSING_DEPENDENCY_CONFIGURATION_PATH = getResourcePath("import/missing-dependency-config.json");
const ACCEPT_CHANGES_CONFIGURATION_PATH = getResourcePath("import/accept-changes-config.json");
const REJECT_CHANGES_CONFIGURATION_PATH = getResourcePath("import/reject-changes-config.json");
const SWITCH_DEPENDENCIES_CONFIGURATION_PATH = getResourcePath(
  "config-objects/switch-dependencies-config.json"
);

test(`[mutagen config import --help] should print import usage`, () => {
  const { code, output } = mutagen(`config import --help`);

  expect(output).toMatchSnapshot("import usage");
  expect(code).toBe(0);
});

test(`[mutagen config import] should fail when file to import does not exist`, () => {
  const { code, output } = mutagen(`config import --auto-confirm /tmp/foo`);

  expect(output).toMatchSnapshot("no such file or directory");
  expect(code).not.toBe(0);
});

test(`[mutagen config import file] should not import the object if it has an invalid tenant`, () => {
  mutagen(`config import --auto-confirm ${SWITCH_DEPENDENCIES_CONFIGURATION_PATH}`);
  const { code, output } = mutagen(`config import --auto-confirm ${INVALID_TENANT_CONFIGURATION_PATH}`);

  expect(output).toMatchSnapshot("no object has been imported");
  expect(code).toBe(1);
});

test(`[mutagen config import file] should import multiple objects of different types`, () => {
  const { code, output } = mutagen(`config import --auto-confirm ${MULTIPLE_OBJECTS_CONFIGURATION_PATH}`);

  expect(output).toMatchSnapshot("every object has been imported");
  expect(code).toBe(0);
});

test(`[mutagen config import file] should only import objects with a unique key`, () => {
  const { code, output } = mutagen(`config import --auto-confirm ${REPEATED_KEYS_CONFIGURATION_PATH}`);

  expect(output).toMatchSnapshot("only one object with the key has been imported");
  expect(code).toBe(0);
});

test(`[mutagen config import file] should not import any object if there's a cycle between object dependencies`, () => {
  const { code, output } = mutagen(`config import --auto-confirm ${CYCLE_CONFIGURATION_PATH}`);

  expect(output).toMatchSnapshot("cycle detected");
  expect(code).toBe(1);
});

test(`[mutagen config import file] should not import any object if some dependencies cannot be resolved`, () => {
  const { code, output } = mutagen(`config import --auto-confirm ${MISSING_DEPENDENCY_CONFIGURATION_PATH}`);

  expect(output).toMatchSnapshot("found missing dependency");
  expect(code).toBe(1);
});

test(`[mutagen config import file] should not import the objects if the user does not confirm the changes`, (done) => {
  const childProcess = mutagenAsync(`config import ${REJECT_CHANGES_CONFIGURATION_PATH}`, ({ code, output }) => {
    expect(output).toMatchSnapshot("changes not applied");
    expect(code).toBe(1);

    done();
  });

  confirmWith(childProcess, 'n');
}, 15000);

test(`[mutagen config import file] should import the objects if the user confirms the changes`, (done) => {
  const childProcess = mutagenAsync(`config import ${ACCEPT_CHANGES_CONFIGURATION_PATH}`, ({ code, output }) => {
    expect(output).toMatchSnapshot("changes applied");
    expect(code).toBe(0);

    done();
  });

  confirmWith(childProcess, 'y');
}, 15000);

const confirmWith = (childProcess, answer) => {
  var buffer = "";
  childProcess.stdout.on('data', (data) => {
    buffer += data;
    if (buffer.trim().endsWith("Please confirm [y|n]:")) {
      childProcess.stdin.write(`${answer}\n`)
    }
  });
}