/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const INVALID_TENANT_CONFIGURATION_PATH = getResourcePath("config/import/switch-with-invalid-tenant-config.json");
const MULTIPLE_OBJECTS_CONFIGURATION_PATH = getResourcePath("config/import/multiple-objects-config.json");
const MULTIPLE_OBJECTS_UPDATED_CONFIGURATION_PATH = getResourcePath("config/import/multiple-objects-update-config.json");
const REPEATED_KEYS_CONFIGURATION_PATH = getResourcePath("config/import/same-key-switches-config.json");
const CYCLE_CONFIGURATION_PATH = getResourcePath("config/import/cycle-config.json");
const MULTIPLE_CYCLE_CONFIGURATION_PATH = getResourcePath("config/import/multiple-cycle-config.json");
const MISSING_DEPENDENCY_CONFIGURATION_PATH = getResourcePath("config/validate/missing-dependency-config.json");
const MULTIPLE_VALIDATION_ERRORS_CONFIGURATION_PATH = getResourcePath("config/validate/multiple-validation-errors-config.json");
const ACCEPT_CHANGES_CONFIGURATION_PATH = getResourcePath("config/import/accept-changes-config.json");
const REJECT_CHANGES_CONFIGURATION_PATH = getResourcePath("config/import/reject-changes-config.json");
const SWITCH_DEPENDENCIES_CONFIGURATION_PATH = getResourcePath("config/config-objects/switch-dependencies-config.json");
const VALIDATION_DEPENDENCIES_CONFIGURATION_PATH = getResourcePath("config/validate/validation-dependencies-config.json");

test(`[mutagen config import --help] should print import usage`, () => {
  assertMutagenResult(`config import --help`, "import usage", 0);
});

test(`[mutagen config import] should fail when file to import does not exist`, () => {
  assertMutagenResult(`config import --auto-confirm /tmp/foo`, "no such file or directory", 1);
});

test(`[mutagen config import file] should not import the object if it has an invalid tenant`, () => {
  mutagen(`config import --auto-confirm ${SWITCH_DEPENDENCIES_CONFIGURATION_PATH}`);

  assertMutagenResult(`config import --auto-confirm ${INVALID_TENANT_CONFIGURATION_PATH}`, "no object has been imported", 1);
});

test(`[mutagen config import file] should create multiple objects of different types`, () => {
  assertMutagenResult(`config import --auto-confirm ${MULTIPLE_OBJECTS_CONFIGURATION_PATH}`, "every object has been created", 0);
});

test(`[mutagen config import file] should skip update of multiple identical objects of different types`, () => {
  assertMutagenResult(`config import --auto-confirm ${MULTIPLE_OBJECTS_CONFIGURATION_PATH}`, "every object has been created", 0);
});

test(`[mutagen config import file] should update multiple objects of different types`, () => {
  assertMutagenResult(`config import --auto-confirm ${MULTIPLE_OBJECTS_UPDATED_CONFIGURATION_PATH}`, "every object has been updated", 0);
});

test(`[mutagen config import file] should only import objects with a unique key`, () => {
  assertMutagenResult(`config import --auto-confirm ${REPEATED_KEYS_CONFIGURATION_PATH}`, "only one object with the key has been imported", 0);
});

test(`[mutagen config import file] should import objects even if there are dependency cycles between objects`, () => {
  assertMutagenResult(`config import --auto-confirm ${CYCLE_CONFIGURATION_PATH}`, "all objects have been created", 0);
});

test(`[mutagen config import file] should import objects even if there are multiple dependency cycles between objects`, () => {
  const { code, output } = mutagen(`config import --auto-confirm ${MULTIPLE_CYCLE_CONFIGURATION_PATH}`);
  // Only checking return code since the import operations ordering is not always the same.
  expect(code).toBe(0);
});

test(`[mutagen config import file] should not import any objects if some dependencies cannot be resolved`, () => {
  assertMutagenResult(`config import --auto-confirm ${MISSING_DEPENDENCY_CONFIGURATION_PATH}`, "found missing dependency", 1);
});

test(`[mutagen config import file] should not import any objects if some dependencies cannot be resolved and some mandatory properties for creation are missing`, () => {
  mutagen(`config import --auto-confirm ${VALIDATION_DEPENDENCIES_CONFIGURATION_PATH}`);
  assertMutagenResult(`config import --auto-confirm ${MULTIPLE_VALIDATION_ERRORS_CONFIGURATION_PATH}`, "found missing dependencies and properties", 1);
});

test(`[mutagen --password-from-stdin config import file] should fail if --auto-confirm is not set`, () => {
  const { code, output } = exec(`echo "password" | ${MUTAGEN_PATH} --password-from-stdin --env=nopassword config import config.json`)
  expect(output).toMatchSnapshot("import stdin password");
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
