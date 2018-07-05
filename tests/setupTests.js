// expose some global functions to all tests
({
  assertMutagenResult,
  exec,
  execAsync,
  getResourcePath,
  mutagen,
  mutagenAsync
} = require("./helpers.js"));

({ cfgObjectTests, importDependencies } = require("./config/config-objects/config-objects-tests.js"));
