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

const DEPENDENCIES_FILE_PATH = getResourcePath("audio/dependencies.csv");
const INVALID_FILE_PATH = getResourcePath("audio/invalid.csv");
const INVALID_PERSONALITY_PATH = getResourcePath("audio/invalid-personality.csv");
const ISO_LATIN_FILE_PATH = getResourcePath("audio/import-iso-latin.csv");
const MINIMALIST_FILE_PATH = getResourcePath("audio/import-minimalist.csv");
const MISSING_AUDIO_FILES_PATH = getResourcePath("audio/missing-audio-files.csv");
const VALID_FILE_PATH = getResourcePath("audio/import.csv");

const OUTPUT_PREFIX = "/tmp/mutagen/test";
const PASSWORD_FILE = "/tmp/gax_password"

beforeAll(() => {
  const { code } = mutagen(`-p --env gax audio import ${DEPENDENCIES_FILE_PATH} < ${PASSWORD_FILE}`);
  expect(code).toBe(0);
})

test("[mutagen audio --help] should print audio usage", () => {
  assertMutagenResult(`audio --help`, "audio usage", 0);
});

///////////
// Audio Export

test("[mutagen audio export --help] should print export usage", () => {
  assertMutagenResult(`audio export --help`, "export usage", 0);
});

test("[mutagen audio export] should print usage when output file is not specified", () => {
  assertMutagenResult(`audio export`, "export usage", 1);
});

test("[mutagen audio export] should export messages into a csv file", () => {
  const outputFile = `${OUTPUT_PREFIX}/csv-only/output.csv`;
  assertMutagenResult(`-p --env gax audio export ${outputFile} < ${PASSWORD_FILE}`, "export done", 0);

  const csvFile = readCsv(outputFile, "export_test");
  expect(csvFile).toMatchSnapshot("csv file");
});

test("[mutagen audio export --personality-ids=x] should export messages into a csv file only for selected personalities", () => {
  const outputFile = `${OUTPUT_PREFIX}/personalities/output.csv`;
  assertMutagenResult(`-p --env gax audio export --personality-ids=11 ${outputFile} < ${PASSWORD_FILE}`, "export done", 0);

  const csvFileContent = readCsv(outputFile, "export_test");
  expect(csvFileContent).toMatchSnapshot("csv file");
});

test("[mutagen audio export --with-audios] should export audio files", () => {
  const outputDirectory = `${OUTPUT_PREFIX}/with-audios`;
  assertMutagenResult(`-p --env gax audio export --with-audios ${outputDirectory}/output.csv < ${PASSWORD_FILE}`, "export done", 0);

  const downloadedFiles = exec(`find ${outputDirectory} -type f -name *export_test* | sort`);
  expect(downloadedFiles.output).toMatchSnapshot("downloaded files");
});

test("[mutagen audio export] should fail when GAX API path is invalid", () => {
  const outputDirectory = `${OUTPUT_PREFIX}/invalid-gax-api-path`;
  assertMutagenResult(`-p --env gax audio export --gax-api-path=/foo/bar ${outputDirectory}/output.csv < ${PASSWORD_FILE}`, "invalid GAX API path", 1);
});


///////////
// Audio Import

test("[mutagen audio import --help] should print import usage", () => {
  assertMutagenResult(`audio import --help`, "import usage", 0);
});

test("[mutagen audio import] should print usage when file to import is not specified", () => {
  assertMutagenResult(`audio import`, "import usage", 1);
});

test("[mutagen audio import] should fail when file to import does not exist", () => {
  assertMutagenResult(`audio import /tmp/foo`, "no such file or directory", 1);
});

test("[mutagen audio import] should fail when file to import is not a valid CSV", () => {
  assertMutagenResult(`audio import ${INVALID_FILE_PATH}`, "invalid csv", 1);
});

test("[mutagen audio import] should fail when some audio files are missing", () => {
  assertMutagenResult(`-p --env gax audio import ${MISSING_AUDIO_FILES_PATH} < ${PASSWORD_FILE}`, "missing audio files", 1);
});

test("[mutagen audio import] should fail when the csv file references invalid personality", () => {
  assertMutagenResult(`-p --env gax audio import ${INVALID_PERSONALITY_PATH} < ${PASSWORD_FILE}`, "missing personality", 1);
});

test("[mutagen audio import] should import messages and audio files", () => {
  testImportWithAudios(VALID_FILE_PATH, "import_test");
});

test("[mutagen audio import] message type and description should be optional", () => {
  testImportWithAudios(MINIMALIST_FILE_PATH, "import_minimalist_test");
});

test("[mutagen audio import] should allow setting input file encoding with --encoding", () => {
  testImportWithAudios(ISO_LATIN_FILE_PATH, "import_iso_latin_test", "--encoding=iso-8859-1");
});

test("[mutagen audio import] should fail when the specified encoding is invalid", () => {
  assertMutagenResult(`audio import --encoding=POTATO-8 ${VALID_FILE_PATH}`, "invalid encoding message", 1);
});

test("[mutagen audio import] should validate duplicated messages", () => {
  assertMutagenResult(`-p --env gax audio import ${VALID_FILE_PATH} < ${PASSWORD_FILE}`, "duplicated messages", 1);
});

test("[mutagen audio import] should fail when GAX API path is invalid", () => {
  assertMutagenResult(`-p --env gax audio import --gax-api-path=/foo/bar ${VALID_FILE_PATH} < ${PASSWORD_FILE}`, "invalid GAX API path", 1);
});

///////////
// Utilities

function testImportWithAudios(importFilePath, messagesKey, additionalArgument = "") {
  assertMutagenResult(`-p --env gax audio import ${additionalArgument} ${importFilePath} < ${PASSWORD_FILE}`, "mutagen import output", 0);

  const outputDirectory = `${OUTPUT_PREFIX}/${messagesKey}`;
  assertMutagenResult(`-p --env gax audio export --with-audios ${outputDirectory}/output.csv < ${PASSWORD_FILE}`, "mutagen export output", 0);

  const findResult = exec(`find ${outputDirectory} -type f -name *${messagesKey}* | sort`);
  expect(findResult.output).toMatchSnapshot("export of the imported files");

  const catResult = readCsv(`${outputDirectory}/output.csv`, messagesKey);
  expect(catResult).toMatchSnapshot("re-export of the csv file");
}

const readCsv = (file, keyword) => {
  // About the sed: we are replacing the messageArId columns by <removed-for-testing-purposes> since we can't be sure
  // what the ID will be when comparing with the snapshot.
  return exec(`cat ${file} | egrep '(messageArId)|(${keyword})' | sed -r "s/,[0-9]{4},/,<removed-for-testing-purposes>,/g"`).output
}
