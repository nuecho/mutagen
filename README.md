
> Provide a powerful, enjoyable, yet lightweight, Genesys toolbox for the Service
> Delivery team to rely on as part of any troubleshooting, testing, support, management,
> development tasks.

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
docker-compose run configserver
``` 

To use mutagen docker image as part of compose, build args needs to be used to override binary
location:

```bash
cd docker
docker-compose build --build-arg MUTAGEN=./build/mutagen mutagen
docker-compose run mutagen
```

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
