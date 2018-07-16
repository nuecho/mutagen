const checkKey = (cfgObject) => {return cfgObject.employeeId === "cfgObjectTest"};

cfgObjectTests("persons", {checkKey: checkKey, initialNumberOfObjects: 1});
