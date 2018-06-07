const checkKey = (cfgObject) => {return cfgObject.employeeId === "cfgObjectTest"};

cfgObjectTests("persons", "config-objects/persons-config.json", checkKey, 1);
