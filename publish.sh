#!/bin/bash
set -e

RELEASE_TYPE=$1

if [[ $RELEASE_TYPE = 'final' || $RELEASE_TYPE = 'candidate' ]]
then
  # create the tag once, then cross-compile
  echo "creating repo tag for $RELEASE_TYPE release"
  ./gradlew clean $RELEASE_TYPE
  echo "publishing artifacts for $RELEASE_TYPE release"
  ./gradlew -Prelease.useLastTag=true publishToMavenLocal
elif [[ $RELEASE_TYPE = 'devSnapshot' || $RELEASE_TYPE == 'snapshot' ]]
then
  echo "publishing artifacts for $RELEASE_TYPE release. repo tag will not be created."
  # if its not a final or candidate, there won't be a tag created
  ./gradlew clean $RELEASE_TYPE publishToMavenLocal
else
  echo "$RELEASE_TYPE is not a valid release type"
  exit 1
fi
