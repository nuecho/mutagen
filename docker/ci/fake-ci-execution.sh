#!/bin/sh

# This file purpose to build a setup closer to the one on the CI for easier debugging

docker run -d --privileged --name ci --network host -v $PWD:/usr/src/mutagen docker:dind

PROJECT_ROOT="$(dirname "$(dirname "$(dirname "$(readlink -fm "$0")")")")"
RELEASE_PATH=build/releases

mkdir -p $PROJECT_ROOT/build/releases/unix
cp $PROJECT_ROOT/build/mutagen $PROJECT_ROOT/build/releases/unix/mutagen

docker exec -t ci sh -c " \
  docker run \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v /usr/src/mutagen:/usr/src/mutagen \
    -w /usr/src/mutagen \
    -e RELEASE_PATH=$RELEASE_PATH \
    tmaier/docker-compose sh -c ' \
      chmod 0755 /usr/src/mutagen/$RELEASE_PATH/unix/mutagen \
      && cd docker \
      && docker-compose -f docker-compose.yml -f docker-compose.test.yml -f docker-compose.test.ci.yml run test \
    ' \
"

docker kill ci && docker rm ci
rm -rf $PROJECT_ROOT/build/releases