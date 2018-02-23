#!/bin/sh
# accept an env variable for the actual tag, in the semver form of vX.Y.Z
# defaults to v0.0.0

VERSION=$(git describe)
SOURCE_TAG=${TAG:-$VERSION}
# shellcheck disable=SC2143
if [ "$(echo "$SOURCE_TAG" | grep -E 'v[0-9]+\.[0-9]+\.[0-9]+')" ] ; then
  VERSION=$(echo "$SOURCE_TAG" | grep -E 'v[0-9]+\.[0-9]+\.[0-9]+');
fi
MAJOR=$(echo "$VERSION" | cut -dv -f2 | cut -d\. -f1)
MINOR=$(echo "$VERSION" | cut -dv -f2 | cut -d\. -f2)
MICRO=$(echo "$VERSION" | cut -dv -f2 | cut -d\. -f3)
export MAJOR
export MINOR
export MICRO
export VERSION=$MAJOR.$MINOR.$MICRO
echo "$SOURCE_TAG -> $VERSION"
