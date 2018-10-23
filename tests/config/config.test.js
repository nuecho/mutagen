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
