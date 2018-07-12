// expose some global functions to all tests
({
  assertMutagenResult,
  exec,
  execAsync,
  getResourcePath,
  mutagen,
  mutagenAsync,
  MUTAGEN_PATH
} = require("./helpers.js"));

({ cfgObjectTests, importDependencies } = require("./config/config-objects/config-objects-tests.js"));
