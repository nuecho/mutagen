> Provide a powerful, enjoyable, yet lightweight, Genesys toolbox for the Service
> Delivery team to rely on as part of any troubleshooting, testing, support, management,
> development tasks.

## Windows
- Before checking out the code, ensure that autocrlf is properly set: `git config --global core.autocrlf true`
- The supported way to run Gradle commands is by using Git Bash through ConEmu.
  - Use the following launcher config: `"%ConEmuDir%\..\Git\git-cmd.exe" --no-cd --command=usr/bin/bash.exe -l -i`

## Development

Before you start, take a look at the [Getting Started](https://sites.google.com/m.nuecho.com/hub/mutagen) section.

We are using IntelliJ IDEA as primary IDE and compiling with JDK 8.

1. Get [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/).
    - _For Ubuntu users_: you may choose the "Without JDK" version.
2. After starting and configuring IntelliJ, choose "Import Project".
    1. Select the mutagen repository folder.
    2. Choose "Import project from external model", then choose Gradle.
    3. On the next page, leave default settings and click Next.
    4. You'll be asked if you want to override the `.idea` folder. Choose Yes.
3. The project will then be imported to IntelliJ. But since we have checked-in some project configuration files,
   other steps are mandatory:
   1. Within IntelliJ, close the project (`File -> Close Project`).
   2. Run `git clean -fdx` within the mutagen folder.
   3. Within IntelliJ, open the project again.
4. _For ubuntu users_, you may encounter compiling issues for whatever reason ("Unresolved reference: java").
   If this is the case, a final step is required:
   1. Go to `File -> Project Structure...`
   2. Select `Platform Settings -> SDKs`.
   3. You'll need to re-select the "JDK Home Path". This will refresh the Classpath items with missing jars.
   4. Click OK and the project should now build without any compilation error.


## Getting Started

To get a list of all available tasks:

```bash
./gradlew tasks --all
```

## Run

To run the application using Gradle

```bash
./gradlew assemble
./gradlew runShadow -Dexec.args="<args>"
```

## Check

To perform all verifications (tests, ktlint, detekt)

```bash
./gradlew check
```

## Code Coverage

Since []JaCoCo does not work that well with kotlin code base](https://youtrack.jetbrains.com/issue/KT-18383), 
we rely on IntelliJ to perform code coverage. To do so, click on the `test` then `Run All Tests with Coverage`.

We are aiming for 80% line code coverage.

## Test

```bash
./gradlew test
```

### Integration

For integration testing purposes, a `docker/docker-compose.yml` file is available to spin
your very own config server. The following will launch both a config server and a PostgreSQL
RDBMS:

```bash
cd docker
docker-compose run --rm -p 2020:2020 configserver
```

_For linux users_: since the docker-compose file uses environment variable, the preceding command won't work
if it is ran with `sudo`. Please follow the [Manage Docker as a non-root user](https://docs.docker.com/install/linux/linux-postinstall/) 
procedure.

Here is a sample `environments.yml` configuration:

```yaml
default:
  host: localhost
  user: default
  password: password
```

### Functional

Functionnal tests are coded in JS (node) using Jest(https://facebook.github.io/jest/).
The recommended method to run the functional test is to run them as part of the docker-compose setup.

To do so you you can run:
```bash
./gradlew clean release
cd docker
docker-compose run test
```

This will launch the configserver along with postgres and a test runner container, the test container uses the mutagen 
you built with `./gradlew release`.

You can pass arguments to `docker-compose run test` that will be forwarded to jest.

You can pass a string that will be matched against test file names. For example `docker-compose run test import` will run all
test files that contains `import` in their name.

To update the snapshot you need to run  `docker-compose run test -u`.

The database and the configserver won't be cleaned up between each run so you should do `docker-compose down` before/after your tests.

## Release

```bash
./gradlew -Pversion=$VERSION release
```

Creates `shadowJar` jar file as well as associated distribution artifacts including standalone
Windows executable from [launch4j](http://launch4j.sourceforge.net/).
Artifacts are then available from `build/launch4j/mutagen.exe` and `build/mutagen` for
Windows and Unix respectively.

## Commit

We use [commitizen](https://github.com/commitizen/cz-cli) to format our commit messages.
This is enforced at the CI level.

## Publish

Publish is performed within the pipeline on tagging where binary releases (and associated sha1 file)
are pushed over a public readable S3 bucket (`s://nuecho.com-mutagen-releases/`).

For instance:

 	https://s3.amazonaws.com/nuecho.com-mutagen-releases/0.0.0/windows/mutagen.exe
 	https://s3.amazonaws.com/nuecho.com-mutagen-releases/0.0.0/unix/mutagen
