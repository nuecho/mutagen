const CONFIGURATION_PATH = getResourcePath("configurations.json");

describe(`[mutagen config import configurations.json]`, () => {
  test(`should import objects when they don't exist`, () => {
    const { code, output } = mutagen(`config import ${CONFIGURATION_PATH}`);

    expect(output).toMatchSnapshot(
      "the two persons have been imported but there are missing dependencies"
    );
    expect(code).toBe(0);
  });

  test(`should do nothing when objects already exist`, () => {
    const { code, output } = mutagen(`config import ${CONFIGURATION_PATH}`);

    expect(output).toMatchSnapshot(
      "the two persons already exist and have not been imported"
    );
    expect(code).toBe(0);
  });
});
