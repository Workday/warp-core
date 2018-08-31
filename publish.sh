#!/bin/bash
set -e

RELEASE_TYPE=$1

if [[ $RELEASE_TYPE == 'final' || $RELEASE_TYPE == 'candidate' ]]
then
  # create the tag once, then cross-compile
  echo "creating repo tag for $RELEASE_TYPE"
  ./gradlew clean $RELEASE_TYPE
  echo "publishing artifacts
  ./gradlew -Prelease.useLastTag=true publishToMavenLocal
elseif [[ $RELEASE_TYPE = 'devSnapshot' || $RELEASE_TYPE == 'snapshot' ]]
  echo "publishing artifacts for $RELEASE_TYPE. note there won't be a repo tag."
  # if its not a final or candidate, there won't be a tag created
  ./gradlew clean $RELEASE_TYPE publishToMavenLocal
else
  echo "$RELEASE_TYPE is not a valid release type"
  exit 1
fi
