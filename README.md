
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

To perform all verifications (tests, ktlint)

```bash
./gradlew check
```

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