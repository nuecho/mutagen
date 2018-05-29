const path = require("path");
const sh = require("shelljs");

// === CONFIGURE SHELLJS

//sh.config.fatal = true; // or set('-e');
sh.config.silent = true;

// === CONFIGURE MUTAGEN LOCATION

const DEFAULT_MUTAGEN_PATH = path.join(
  __dirname,
  "..",
  "build",
  process.platform === "win32"
    ? path.join("launch4j", "mutagen.exe")
    : "mutagen"
);

const MUTAGEN_PATH = sh.env["MUTAGEN_PATH"] || DEFAULT_MUTAGEN_PATH;
const RESOURCES_PATH = path.join(__dirname, "resources");

// global helper function to retrieve path of file in resources
getResourcePath = (filePath = "") => path.join(RESOURCES_PATH, filePath);

// expose a global mutagen function to all tests
mutagen = (args = "", callback) =>
  simplifyResponse(sh.exec(`${MUTAGEN_PATH} ${args}`, callback));

const simplifyResponse = ({ code, stdout, stderr }) => ({
  code,
  output: { stdout, stderr }
});
