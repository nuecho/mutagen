const checkKey = (cfgObject) => {return cfgObject.name === "cfgObjectTest"};

cfgObjectTests("folders", "folders-config.json", {checkKey: checkKey, checkMandatoryProperties: false});
