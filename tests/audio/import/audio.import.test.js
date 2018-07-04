const { readCsv } = require("../audio.test.helpers.js");

const INVALID_FILE_PATH = getResourcePath("audio/import/invalid.csv");
const MISSING_AUDIO_FILES_PATH = getResourcePath("audio/import/missing-audio-files.csv");
const INVALID_PERSONALITY_PATH = getResourcePath("audio/import/invalid-personality.csv");
const VALID_FILE_PATH = getResourcePath("audio/import/import.csv");
const MINIMALIST_FILE_PATH = getResourcePath("audio/import/import-minimalist.csv");

const OUTPUT_PREFIX = "/tmp/mutagen/test/import";

test(`[mutagen audio import --help] should print import usage`, () => {
  const { code, output } = mutagen(`--env gax audio import --help`);

  expect(output).toMatchSnapshot("import usage");
  expect(code).toBe(0);
});

test(`[mutagen audio import] should print usage when file to import is not specified`, () => {
  const { code, output } = mutagen(`--env gax audio import`);

  expect(output).toMatchSnapshot("import usage");
  expect(code).not.toBe(0);
});

test(`[mutagen audio import] should fail when file to import does not exist`, () => {
  const { code, output } = mutagen(`--env gax audio import /tmp/foo`);

  expect(output).toMatchSnapshot("no such file or directory");
  expect(code).not.toBe(0);
});

test(`[mutagen audio import] should fail when file to import is not a valid CSV`, () => {
  const { code, output } = mutagen(`--env gax audio import ${INVALID_FILE_PATH}`);

  expect(output).toMatchSnapshot("invalid csv");
  expect(code).not.toBe(0);
});

test(`[mutagen audio import] should fail when some audio files are missing`, () => {
  const { code, output } = mutagen(`--env gax audio import ${MISSING_AUDIO_FILES_PATH}`);

  expect(output).toMatchSnapshot("missing audio files");
  expect(code).not.toBe(0);
});

test(`[mutagen audio import] should fail when the csv file references invalid personality`, () => {
  const { code, output } = mutagen(`--env gax audio import ${INVALID_PERSONALITY_PATH}`);

  expect(output).toMatchSnapshot("missing personality");
  expect(code).not.toBe(0);
});

test(`[mutagen audio import] should import messages and audio files`, () => {
testImportWithAudios(VALID_FILE_PATH, "import_test");
});

test(`[mutagen audio import] message type and description should be optional`, () => {
  testImportWithAudios(MINIMALIST_FILE_PATH, "import_minimalist_test");
});

test(`[mutagen audio import] should validate duplicated messages`, () => {
  const { code, output } = mutagen(`--env gax audio import ${VALID_FILE_PATH}`);

  expect(output).toMatchSnapshot("duplicated messages");
  expect(code).not.toBe(0);
});

function testImportWithAudios(importFilePath, messagesKey) {
  const importResult = mutagen(`--env gax audio import ${importFilePath}`);

  expect(importResult.output).toMatchSnapshot("number of messages imported");
  expect(importResult.code).toBe(0);

  const outputDirectory = `${OUTPUT_PREFIX}/${messagesKey}`;
  const exportResult = mutagen(`--env gax audio export --with-audios ${outputDirectory}/output.csv`);
  expect(exportResult.code).toBe(0);

  const findResult = exec(`find ${outputDirectory} -type f -name *${messagesKey}* | sort`);
  expect(findResult.output).toMatchSnapshot("export of the imported files");

  const catResult = readCsv(`${outputDirectory}/output.csv`, messagesKey);
  expect(catResult).toMatchSnapshot("re-export of the csv file");
}