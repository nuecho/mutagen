const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("agentGroups", "config-objects/agent-groups-config.json", checkKey);
