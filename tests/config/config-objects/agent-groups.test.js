const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("agentGroups", "agent-groups-config.json", {checkKey: checkKey, checkMandatoryProperties: false});
