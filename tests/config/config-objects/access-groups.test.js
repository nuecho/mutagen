const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("accessGroups", "config-objects/access-groups-config.json", checkKey);
