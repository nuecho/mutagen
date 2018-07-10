importDependencies("dn-dependencies-config.json");
importDependencies("dnGroups-dependencies-config.json");
const checkKey = (cfgObject) => {return cfgObject.group.name === "cfgObjectTest"};

cfgObjectTests("dnGroups", {checkKey: checkKey});
