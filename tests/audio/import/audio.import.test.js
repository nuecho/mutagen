const { readCsv } = require("../audio.test.helpers.js");

const INVALID_FILE_PATH = getResourcePath("audio/import/invalid.csv");
const MISSING_AUDIO_FILES_PATH = getResourcePath("audio/import/missing-audio-files.csv");
const INVALID_PERSONALITY_PATH = getResourcePath("audio/import/invalid-personality.csv");
const VALID_FILE_PATH = getResourcePath("audio/import/import.csv");
const MINIMALIST_FILE_PATH = getResourcePath("audio/import/import-minimalist.csv");
const ISO_LATIN_FILE_PATH = getResourcePath("audio/import/import-iso-latin.csv");

const OUTPUT_PREFIX = "/tmp/mutagen/test/import";

test(`[mutagen audio import --help] should print import usage`, () => {
  assertMutagenResult(`--env gax audio import --help`, "import usage", 0);
});

test(`[mutagen audio import] should print usage when file to import is not specified`, () => {
  assertMutagenResult(`--env gax audio import`, "import usage", 1);
});

test(`[mutagen audio import] should fail when file to import does not exist`, () => {
  assertMutagenResult(`--env gax audio import /tmp/foo`, "no such file or directory", 1);
});

test(`[mutagen audio import] should fail when file to import is not a valid CSV`, () => {
  assertMutagenResult(`--env gax audio import ${INVALID_FILE_PATH}`, "invalid csv", 1);
});

test(`[mutagen audio import] should fail when some audio files are missing`, () => {
  assertMutagenResult(`--env gax audio import ${MISSING_AUDIO_FILES_PATH}`, "missing audio files", 1);
});

test(`[mutagen audio import] should fail when the csv file references invalid personality`, () => {
  assertMutagenResult(`--env gax audio import ${INVALID_PERSONALITY_PATH}`, "missing personality", 1);
});

test(`[mutagen audio import] should import messages and audio files`, () => {
  testImportWithAudios(VALID_FILE_PATH, "import_test");
});

test(`[mutagen audio import] message type and description should be optional`, () => {
  testImportWithAudios(MINIMALIST_FILE_PATH, "import_minimalist_test");
});

test(`[mutagen audio import] should allow setting input file encoding with --encoding`, () => {
  testImportWithAudios(ISO_LATIN_FILE_PATH, "import_iso_latin_test", "--encoding=iso-8859-1");
});

test(`[mutagen audio import] should fail when the specified encoding is invalid`, () => {
  assertMutagenResult(`--env gax audio import --encoding=POTATO-8 ${VALID_FILE_PATH}`, "invalid encoding message", 1);
});

test(`[mutagen audio import] should validate duplicated messages`, () => {
  assertMutagenResult(`--env gax audio import ${VALID_FILE_PATH}`, "duplicated messages", 1);
});

function testImportWithAudios(importFilePath, messagesKey, additionalArgument = "") {
  assertMutagenResult(`--env gax audio import ${additionalArgument} ${importFilePath}`, "mutagen import output", 0);

  const outputDirectory = `${OUTPUT_PREFIX}/${messagesKey}`;
  assertMutagenResult(`--env gax audio export --with-audios ${outputDirectory}/output.csv`, "mutagen export output", 0);

  const findResult = exec(`find ${outputDirectory} -type f -name *${messagesKey}* | sort`);
  expect(findResult.output).toMatchSnapshot("export of the imported files");

  const catResult = readCsv(`${outputDirectory}/output.csv`, messagesKey);
  expect(catResult).toMatchSnapshot("re-export of the csv file");
}