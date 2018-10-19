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

const MISSING_DEPENDENCY_CONFIGURATION_PATH = getResourcePath("config/validate/missing-dependency-config.json");
const MULTIPLE_VALIDATION_ERRORS_CONFIGURATION_PATH = getResourcePath("config/validate/multiple-validation-errors-config.json");
const VALIDATION_DEPENDENCIES_CONFIGURATION_PATH = getResourcePath("config/validate/validation-dependencies-config.json");
const VALID_CONFIGURATION_PATH = getResourcePath("config/validate/valid-config.json");

test(`[mutagen config validate file] should indicate validation failure if there are missing dependencies, and print them`, () => {
  assertMutagenResult(`config validate ${MISSING_DEPENDENCY_CONFIGURATION_PATH}`, "validation failed", 1);
});

test(`[mutagen config validate file] should indicate validation failure if there are multiple validation errors, and print them`, () => {
  mutagen(`config import --auto-confirm ${VALIDATION_DEPENDENCIES_CONFIGURATION_PATH}`);
  assertMutagenResult(`config validate ${MULTIPLE_VALIDATION_ERRORS_CONFIGURATION_PATH}`, "validation failed", 1);
});

test(`[mutagen config validate file] should indicate validation success if there are no validation errors`, () => {
  assertMutagenResult(`config validate ${VALID_CONFIGURATION_PATH}`, "validation succeeded", 0);
});
