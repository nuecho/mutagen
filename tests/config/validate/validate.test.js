const MISSING_DEPENDENCY_CONFIGURATION_PATH = getResourcePath("config/validate/missing-dependency-config.json");
const MULTIPLE_VALIDATION_ERRORS_CONFIGURATION_PATH = getResourcePath("config/validate/multiple-validation-errors-config.json");
const VALID_CONFIGURATION_PATH = getResourcePath("config/validate/valid-config.json");

test(`[mutagen config validate file] should indicate validation failure if there are missing dependencies, and print them`, () => {
  assertMutagenResult(`config validate ${MISSING_DEPENDENCY_CONFIGURATION_PATH}`, "validation failed", 1);
});

test(`[mutagen config validate file] should indicate validation failure if there are multiple validation errors, and print them`, () => {
  assertMutagenResult(`config validate ${MULTIPLE_VALIDATION_ERRORS_CONFIGURATION_PATH}`, "validation failed", 1);
});

test(`[mutagen config validate file] should indicate validation success if there are no validation errors`, () => {
  assertMutagenResult(`config validate ${VALID_CONFIGURATION_PATH}`, "validation succeeded", 0);
});