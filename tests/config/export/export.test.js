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

test(`[mutagen config export --help] should print import usage`, () => {
  assertMutagenResult(`config export --help`, "export usage", 0);
});

test(`[mutagen config export] should throw an exception when the encoding specified is not supported`, () => {
  const { code, output } = mutagen(`-e invalid-encoding config export`);

  expect(output.stderr).toMatchSnapshot("export invalid encoding exception");
  expect(code).toBe(1);
});

test(`[mutagen config export] should not throw an exception when no encoding is specified`, () => {
  const { code, output } = mutagen(`config export`);

  expect(output.stderr).toMatchSnapshot("export without exception");
  expect(code).toBe(0);
});

test(`[mutagen -p config export] should allow passing password on standard input`, () => {
  const { code, output } = exec(`echo "password" | ${MUTAGEN_PATH} -p --env=nopassword config export`)
  expect(output.stderr).toMatchSnapshot("export stdin password");
  expect(code).toBe(0);
});

test(`[mutagen --metrics=$metricsFile config export] should export the metrics in a file`, () => {
  const fs = require("fs")
  const metricsFile = "/tmp/mutagen/test/metrics.json"
  const { code, output } = mutagen(`--metrics=${metricsFile} config export`);

  expect(code).toBe(0);
  expect(fs.existsSync(metricsFile)).toBe(true);

  const metrics = JSON.parse(fs.readFileSync(metricsFile, 'utf-8'));
  expect(metrics.version).toEqual('4.0.0');
  expect(metrics.timers).not.toEqual({});
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
