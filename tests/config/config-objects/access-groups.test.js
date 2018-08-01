const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("accessGroups", {checkKey: checkKey, checkMandatoryProperties: false, checkUnchangeableProperties: true});
