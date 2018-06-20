importDependencies("config-objects/dn-dependencies-config.json");
importDependencies("config-objects/dn-group-dependencies-config.json");
const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("dnGroups", "config-objects/dn-groups-config.json", checkKey);
