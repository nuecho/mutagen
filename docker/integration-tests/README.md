# Mutagen Integration Tests

Docker image used to execute _mutagen_ integration tests with Jest.

This image is based on node8 image to which the following is added:

- OpenJDK8
- Jest NPM package (test runner)
- ShellJS NPM package (to execute mutagen)
- wait-on NPM package (to ensure that the Config Server is up before executing tests)

## Getting Started

These instructions will cover usage information and for the docker container

### Prerequisities

In order to run this container you'll need docker installed.

- [Windows](https://docs.docker.com/windows/started)
- [OS X](https://docs.docker.com/mac/started/)
- [Linux](https://docs.docker.com/linux/started/)

### Usage

#### Build

The image can be build by simply calling `docker build .` in the project root directory.

#### Dependencies

This image is expected to be used in a `docker-compose` script that links it with a `configserver` image.

For example:

```
  test:
    image: dr.s.nuecho.com/mutagen-integration-tests:latest
    depends_on:
      - configserver
    links:
      - configserver
    volumes:
      ...
```

#### Environment Variables

* `WAIT_TIMEOUT` - Maximum time to wait for the Config Server (default: 180)
* `CONFIG_SERVER_HOST` - Config Server host name (default: configserver)
* `CONFIG_SERVER_PORT` - Config Server port (default:2020)

#### Volumes

- _/root/.mutagen/environments.yml:ro_ The _mutagen_ environments definitions.
- _/usr/local/bin/mutagen_ The _mutagen_ executable location.
- _/usr/src/app/tests_ The actual test files location, a `setupTests.js` file is expected to be there.

#### Useful File Locations

* `/entrypoint.sh` - Initialization script that executes the tests
* `/jest.config.js` - Jest configuration file

## Built With

- OpenJDK 8
- Jest v22.4.4
- ShellJS v0.8.2
- wait-on v2.1.0