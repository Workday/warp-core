#!/bin/bash

release_type = $1
echo "release type set to $release_type"

if [ $release_type == 'final' || $release_type == 'candidate' ]
then
  # create the tag once, then cross-compile
  ./gradlew clean $release_type
  ./gradlew -Prelease.useLastTag=true publishToMavenLocal
elif [ $release_type = 'devSnapshot' || $release_type == 'snapshot' ]
  # if its not a final or candidate, there won't be a tag created
  ./gradlew clean $release_type publishToMavenLocal
else
  echo "$release_type is not a valid release type"
  exit 1
fi
