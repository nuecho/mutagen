const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("accessGroups", "access-groups-config.json", {checkKey: checkKey, checkMandatoryProperties: false});
