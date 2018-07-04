const path = require("path");
const sh = require("shelljs");
const waitOn = require("wait-on");

// Configure shelljs

// sh.config.fatal = true; // or set('-e');
sh.config.silent = true;

// Configure mutagen

// Assume mutagen is in your path by default
const MUTAGEN_PATH = sh.env["MUTAGEN_PATH"] || "mutagen";
const RESOURCES_PATH = path.join(__dirname, "resources");

// Helpers functions

// Misc info:
// Database dump: docker-compose exec -T postgres sh -c "pg_dump -U postgres --clean -f dump.sql"
// Database restore: docker-compose exec -T postgres sh -c "psql -U postgres -f dump.sql"

// Configure docker

const DOCKER_PATH = path.join(__dirname, "../docker");

const Configserver = {
  start(done) {
    sh.exec(
      `docker-compose -f ${path.join(
        DOCKER_PATH,
        "docker-compose.yml"
      )} up -d --no-recreate`
    );
    waitOn({ resources: ["tcp:configserver:2020"] }, done);
  },

  stop() {
    sh.exec(
      `docker-compose -f ${path.join(DOCKER_PATH, "docker-compose.yml")} down`
    );
  }
};

const getResourcePath = (filePath = "") => path.join(RESOURCES_PATH, filePath);

const exec = (command = "") => formatResponse(sh.exec(command));
const execAsync = (command = "", callback) =>
  sh.exec(command, (code, stdout, stderr) => callback(formatResponse({code, stdout, stderr})));

const mutagen = (args = "") => exec(`${MUTAGEN_PATH} ${args}`);
const mutagenAsync = (args = "", callback) => execAsync(`${MUTAGEN_PATH} ${args}`, callback);

const formatResponse = ({ code, stdout, stderr }) => ({
  code,
  output: { stdout, stderr }
});

module.exports = { Configserver, getResourcePath, mutagen, mutagenAsync, exec, execAsync };
