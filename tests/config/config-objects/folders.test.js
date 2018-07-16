const checkKey = (cfgObject) => {return cfgObject.name === "cfgObjectTest"};

cfgObjectTests("folders", {checkKey: checkKey, checkMandatoryProperties: false});
