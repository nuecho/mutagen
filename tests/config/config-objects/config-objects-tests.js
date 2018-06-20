function cfgObjectTests (
  cfgObjectType,
  configurationFile,
  checkKey = null,
  initialNumberOfObjects = 0,
  checkInitialExport = true
) {
  describe(`[mutagen ${cfgObjectType} config import-export]`, () => {
    if (checkInitialExport) {
      cfgObjectInitialExportTest(cfgObjectType, initialNumberOfObjects);
    }
    cfgObjectCreationTest(cfgObjectType, getResourcePath(configurationFile));
    cfgObjectUpdateTest(cfgObjectType, getResourcePath(configurationFile));
    cfgObjectExportTest(cfgObjectType, checkKey);
  });
};

function cfgObjectInitialExportTest(cfgObjectType, initialNumberOfObjects) {
  const shouldExport = initialNumberOfObjects ? "" : "not ";

  test(`should ${shouldExport}initially export ${cfgObjectType}`, () => {
    const { code, output } = mutagen(`config export --format JSON`);
    const exportedConfig = JSON.parse(output.stdout);

    if (initialNumberOfObjects)
      expect(exportedConfig[cfgObjectType].length).toBe(initialNumberOfObjects);
    else expect(exportedConfig[cfgObjectType]).toBeUndefined;
    expect(code).toBe(0);
  });
};


function cfgObjectCreationTest(cfgObjectType, configurationPath) {
  test(`should create the ${cfgObjectType}`, () => {
    const { code, output } = mutagen(`config import --auto-confirm ${configurationPath}`);

    expect(output).toMatchSnapshot(`the ${cfgObjectType} have been created`);
    expect(code).toBe(0);
  });
};

function cfgObjectUpdateTest(cfgObjectType, configurationPath) {
  test(`should update the ${cfgObjectType}`, () => {
    const { code, output } = mutagen(`config import --auto-confirm ${configurationPath}`);
    
    expect(output).toMatchSnapshot(`the ${cfgObjectType} have been updated`);
    expect(code).toBe(0);
  });
};

function cfgObjectExportTest(cfgObjectType, checkKey = null) {
  test(`should properly export the imported ${cfgObjectType}`, () => {
    const { code, output } = mutagen(`config export --format JSON`);
    const exportedConfig = JSON.parse(output.stdout);
    // every tenant has a copy of every enumerator, and the number of tenants in the config varies if other tests import tenants
    const expectedNumberOfObjects = cfgObjectType === "enumerators" ? exportedConfig.tenants.length : 1;

    expect(code).toBe(0);    
    expect(exportedConfig[cfgObjectType]).toBeDefined;
    if(checkKey)
      expect(exportedConfig[cfgObjectType].filter(checkKey).length).toBe(expectedNumberOfObjects);
    else    
      expect(exportedConfig[cfgObjectType].filter(cfgObject => cfgObject.name === "cfgObjectTest").length).toBe(expectedNumberOfObjects);
  });
};

const importDependencies = (dependenciesFile) => {mutagen(`config import --auto-confirm ${getResourcePath(dependenciesFile)}`);}

module.exports = {
  cfgObjectTests,
  importDependencies
};
