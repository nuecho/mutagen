const defaultOptions = {
  checkKey: null,
  checkMandatoryProperties: true
};

function cfgObjectTests (
  cfgObjectType,
  options = {}
) {
  describe(`[mutagen ${cfgObjectType} config import-export]`, () => {
    const mergedOptions = {...defaultOptions, ...options}

    cfgObjectInitialExportTest(cfgObjectType, mergedOptions.checkKey);
    if (mergedOptions.checkMandatoryProperties) {
      cfgObjectMissingPropertiesTest(cfgObjectType, getResourcePath(`config/config-objects/${cfgObjectType}-invalid-config.json`));
    }
    cfgObjectImportNewTest(cfgObjectType, getResourcePath(`config/config-objects/${cfgObjectType}-config.json`));
    cfgObjectImportUnmodifiedTest(cfgObjectType, getResourcePath(`config/config-objects/${cfgObjectType}-config.json`));
    cfgObjectImportModifiedTest(cfgObjectType, getResourcePath(`config/config-objects/${cfgObjectType}-updated-config.json`));
    cfgObjectExportTest(cfgObjectType, mergedOptions.checkKey);
  });
};

function cfgObjectInitialExportTest(cfgObjectType, checkKey) {
  test(`cfgObject ${cfgObjectType} should not initially exists on the configuration server`, () => {
    checkObjectCount(cfgObjectType, 0, checkKey);
  });
};

function cfgObjectMissingPropertiesTest(cfgObjectType, emptyConfigurationPath) {
  test(`should throw exception when new ${cfgObjectType} to import miss mandatory properties ${cfgObjectType}`, () => {
    assertMutagenResult(`config import --auto-confirm ${emptyConfigurationPath}`, "exception has been thrown", 1);
  });
};

function cfgObjectImportNewTest(cfgObjectType, configurationPath) {
  test(`should create the ${cfgObjectType}`, () => {
    assertMutagenResult(`config import --auto-confirm ${configurationPath}`, `the ${cfgObjectType} have been created`, 0);
  });
};

function cfgObjectImportUnmodifiedTest(cfgObjectType, configurationPath) {
  test(`should update the same ${cfgObjectType}`, () => {
    assertMutagenResult(`config import --auto-confirm ${configurationPath}`, `the ${cfgObjectType} have been updated`, 0);
  });
};

function cfgObjectImportModifiedTest(cfgObjectType, configurationPath) {
  test(`should update the modified ${cfgObjectType}`, () => {
    assertMutagenResult(`config import --auto-confirm ${configurationPath}`, `the modified ${cfgObjectType} have been updated`, 0);
  });
};

function cfgObjectExportTest(cfgObjectType, checkKey) {
  test(`should properly export the imported ${cfgObjectType}`, () => {
    checkObjectCount(cfgObjectType, 1, checkKey);
  });
};

function checkObjectCount(cfgObjectType, objectCount, checkKey = null) {
  const { code, output } = mutagen(`config export --format JSON`);
  const exportedConfig = JSON.parse(output.stdout);

  expect(code).toBe(0);
  expect(exportedConfig[cfgObjectType]).toBeDefined;
  if(checkKey)
    expect(exportedConfig[cfgObjectType].filter(checkKey).length).toBe(objectCount);
  else
    expect(exportedConfig[cfgObjectType].filter(cfgObject => cfgObject.name === "cfgObjectTest").length).toBe(objectCount);
}

const importDependencies = (dependenciesFile) => {mutagen(`config import --auto-confirm ${getResourcePath(`config/config-objects/${dependenciesFile}`)}`);}

module.exports = {
  cfgObjectTests,
  importDependencies
};
