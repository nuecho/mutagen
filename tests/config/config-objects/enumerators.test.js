const checkKey = (cfgObject) => {return cfgObject.name === "cfgObjectTest" && cfgObject.tenant === "Environment"};

cfgObjectTests("enumerators", {initialNumberOfObjects: 23, checkKey: checkKey});
