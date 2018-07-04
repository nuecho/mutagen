const sh = require("shelljs");
const { readCsv } = require("../audio.test.helpers.js");

const DEPENDENCIES_FILE_PATH = getResourcePath("audio/export/dependencies.csv");
const OUTPUT_PREFIX = "/tmp/mutagen/test/export";

mutagen(`--env gax audio import ${DEPENDENCIES_FILE_PATH}`);

test(`[mutagen audio export --help] should print export usage`, () => {
  const { code, output } = mutagen(`--env gax audio export --help`);

  expect(output).toMatchSnapshot("export usage");
  expect(code).toBe(0);
});

test(`[mutagen audio export] should print usage when output file is not specified`, () => {
  const { code, output } = mutagen(`--env gax audio export`);

  expect(output).toMatchSnapshot("export usage");
  expect(code).not.toBe(0);
});

test(`[mutagen audio export] should export messages into a csv file`, () => {
  const outputFile = `${OUTPUT_PREFIX}/csv-only/output.csv`;
  const mutagenResult = mutagen(`--env gax audio export ${outputFile}`);

  expect(mutagenResult.output).toMatchSnapshot("export done");
  expect(mutagenResult.code).toBe(0);

  const csvFile = exec(`cat ${outputFile} | egrep '(messageArId)|(export_test)'`);
  expect(csvFile.output).toMatchSnapshot("csv file");
});

test(`[mutagen audio export --personality-ids=x] should export messages into a csv file only for selected personalities`, () => {
  const outputFile = `${OUTPUT_PREFIX}/personalities/output.csv`;
  const mutagenResult = mutagen(`--env gax audio export --personality-ids=11 ${outputFile}`);

  expect(mutagenResult.output).toMatchSnapshot("export done");
  expect(mutagenResult.code).toBe(0);

  const csvFileContent = readCsv(outputFile, "export_test");
  expect(csvFileContent).toMatchSnapshot("csv file");
});

test(`[mutagen audio export --with-audios] should export audio files`, () => {
  const outputDirectory = `${OUTPUT_PREFIX}/with-audios`;
  const mutagenResult = mutagen(`--env gax audio export --with-audios ${outputDirectory}/output.csv`);

  expect(mutagenResult.output).toMatchSnapshot("export done");
  expect(mutagenResult.code).toBe(0);

  const downloadedFiles = exec(`find ${outputDirectory} -type f -name *export_test* | sort`);
  expect(downloadedFiles.output).toMatchSnapshot("downloaded files");
});
