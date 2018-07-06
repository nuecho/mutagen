const checkKey = (cfgObject) => {return cfgObject.name === "cfgObjectTest" && cfgObject.tenant === "Environment"};

cfgObjectTests("hosts", "hosts-config.json");
