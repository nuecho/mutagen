const checkKey = (cfgObject) => {return cfgObject.employeeId === "cfgObjectTest"};

cfgObjectTests("persons", "persons-config.json", {checkKey: checkKey, initialNumberOfObjects: 1});
