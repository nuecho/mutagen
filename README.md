
> Provide a powerful, enjoyable, yet lightweight, Genesys toolbox for the Service
> Delivery team to rely on as part of any troubleshooting, testing, support, management,
> development tasks.

## Getting Started

To get a list of all available tasks:

```bash
./gradlew tasks --all
```

## Development

```bash
./gradlew assemble
./gradlew runShadow -Dexec.args="<args>"
```

To perform all verifications (tests, ktlint, detekt)

```bash
./gradlew check
```

IDEA can be configured to work nice with ktlint using this procedure: https://github.com/shyiko/ktlint#-with-intellij-idea

## Test

```bash
./gradlew test
```

## Release

```bash
./gradlew -Pversion=$VERSION release
```

Creates `shadowJar` jar file as well as associated distribution artifacts including standalone
Windows executable from [launch4j](http://launch4j.sourceforge.net/).
Artifacts are then available from `build/launch4j/mutagen.exe` and `build/mutagen` for
Windows and Uni* respectively.

## Publish

Publish is performed within the pipeline on tagging where binary releases (and associated sha1 file)
are pushed over a public readable S3 bucket (`s://nuecho.com-mutagen-releases/`).

For instance:

 	https://s3.amazonaws.com/nuecho.com-mutagen-releases/0.0.0/windows/mutagen.exe
 	https://s3.amazonaws.com/nuecho.com-mutagen-releases/0.0.0/unix/mutagen