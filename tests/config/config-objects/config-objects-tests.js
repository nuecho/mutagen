const defaultOptions = {
  checkKey: null,
  initialNumberOfObjects: 0,
  checkInitialExport: true,
  checkMandatoryProperties: true
};

function cfgObjectTests (
  cfgObjectType,
  options = {}
) {
  describe(`[mutagen ${cfgObjectType} config import-export]`, () => {
    const mergedOptions = {...defaultOptions, ...options}

    if (mergedOptions.checkInitialExport) {
      cfgObjectInitialExportTest(cfgObjectType, mergedOptions.initialNumberOfObjects);
    }
    if (mergedOptions.checkMandatoryProperties) {
      cfgObjectMissingPropertiesTest(cfgObjectType, getResourcePath(`config/config-objects/${cfgObjectType}-invalid-config.json`));
    }
    cfgObjectImportNewTest(cfgObjectType, getResourcePath(`config/config-objects/${cfgObjectType}-config.json`));
    cfgObjectImportUnmodifiedTest(cfgObjectType, getResourcePath(`config/config-objects/${cfgObjectType}-config.json`));
    cfgObjectImportModifiedTest(cfgObjectType, getResourcePath(`config/config-objects/${cfgObjectType}-updated-config.json`));
    cfgObjectExportTest(cfgObjectType, mergedOptions.checkKey);
  });
};

function cfgObjectInitialExportTest(cfgObjectType, initialNumberOfObjects) {
  const shouldExport = initialNumberOfObjects ? "" : "not ";

  test(`should ${shouldExport}initially export ${cfgObjectType}`, () => {
    const { code, output } = mutagen(`config export --format JSON`);
    const exportedConfig = JSON.parse(output.stdout);
    // every tenant has a copy of every enumerator, and the number of tenants in the config varies if other tests have already imported tenants
    const expectedNumberOfObjects = cfgObjectType === "enumerators" ? exportedConfig.tenants.length * initialNumberOfObjects : initialNumberOfObjects;

    if (initialNumberOfObjects)
      expect(exportedConfig[cfgObjectType].length).toBe(expectedNumberOfObjects);
    else expect(exportedConfig[cfgObjectType]).toBeUndefined;
    expect(code).toBe(0);
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

function cfgObjectExportTest(cfgObjectType, checkKey = null) {
  test(`should properly export the imported ${cfgObjectType}`, () => {
    const { code, output } = mutagen(`config export --format JSON`);
    const exportedConfig = JSON.parse(output.stdout);

    expect(code).toBe(0);    
    expect(exportedConfig[cfgObjectType]).toBeDefined;
    if(checkKey)
      expect(exportedConfig[cfgObjectType].filter(checkKey).length).toBe(1);
    else    
      expect(exportedConfig[cfgObjectType].filter(cfgObject => cfgObject.name === "cfgObjectTest").length).toBe(1);
  });
};

const importDependencies = (dependenciesFile) => {mutagen(`config import --auto-confirm ${getResourcePath(`config/config-objects/${dependenciesFile}`)}`);}

module.exports = {
  cfgObjectTests,
  importDependencies
};
