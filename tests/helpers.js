/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

const assertMutagenResult = (args, snapshot, returnCode) => {
  const { code, output } = mutagen(args);
  expect(output).toMatchSnapshot(snapshot);
  expect(code).toBe(returnCode);
}

const formatResponse = ({ code, stdout, stderr }) => ({
  code,
  output: { stdout, stderr }
});

module.exports = {
  assertMutagenResult,
  Configserver,
  exec,
  execAsync,
  getResourcePath,
  mutagen,
  mutagenAsync,
  MUTAGEN_PATH
};
