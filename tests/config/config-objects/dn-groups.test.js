importDependencies("dn-dependencies-config.json");
importDependencies("dn-group-dependencies-config.json");
const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("dnGroups", "dn-groups-config.json", {checkKey: checkKey});
