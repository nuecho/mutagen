const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("agentGroups", {checkKey: checkKey, checkMandatoryProperties: false});
