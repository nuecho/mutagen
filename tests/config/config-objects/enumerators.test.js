const checkKey = (cfgObject) => {return cfgObject.name === "cfgObjectTest" && cfgObject.tenant === "Environment"};

cfgObjectTests("enumerators", "enumerators-config.json", {initialNumberOfObjects: 23, checkKey: checkKey});
