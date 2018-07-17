const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("placeGroups", {checkKey: checkKey, checkMandatoryProperties: false});
