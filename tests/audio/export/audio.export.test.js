const sh = require("shelljs");
const { readCsv } = require("../audio.test.helpers.js");

const DEPENDENCIES_FILE_PATH = getResourcePath("audio/export/dependencies.csv");
const OUTPUT_PREFIX = "/tmp/mutagen/test/export";

beforeAll(() => {
  const { code } = mutagen(`--env gax audio import ${DEPENDENCIES_FILE_PATH}`);
  expect(code).toBe(0);
})

test(`[mutagen audio export --help] should print export usage`, () => {
  assertMutagenResult(`--env gax audio export --help`, "export usage", 0);
});

test(`[mutagen audio export] should print usage when output file is not specified`, () => {
  assertMutagenResult(`--env gax audio export`, "export usage", 1);
});

test(`[mutagen audio export] should export messages into a csv file`, () => {
  const outputFile = `${OUTPUT_PREFIX}/csv-only/output.csv`;
  assertMutagenResult(`--env gax audio export ${outputFile}`, "export done", 0);

  const csvFile = readCsv(outputFile, "export_test");
  expect(csvFile).toMatchSnapshot("csv file");
});

test(`[mutagen audio export --personality-ids=x] should export messages into a csv file only for selected personalities`, () => {
  const outputFile = `${OUTPUT_PREFIX}/personalities/output.csv`;
  assertMutagenResult(`--env gax audio export --personality-ids=11 ${outputFile}`, "export done", 0);

  const csvFileContent = readCsv(outputFile, "export_test");
  expect(csvFileContent).toMatchSnapshot("csv file");
});

test(`[mutagen audio export --with-audios] should export audio files`, () => {
  const outputDirectory = `${OUTPUT_PREFIX}/with-audios`;
  assertMutagenResult(`--env gax audio export --with-audios ${outputDirectory}/output.csv`, "export done", 0);

  const downloadedFiles = exec(`find ${outputDirectory} -type f -name *export_test* | sort`);
  expect(downloadedFiles.output).toMatchSnapshot("downloaded files");
});
