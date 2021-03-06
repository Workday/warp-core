#!/bin/bash
set -e

# usage: ./publish.sh <release type> <release scope> <repository>
#   release type must be one of { devSnapshot, snapshot, candidate, final }
#   release scope must be one of { major, minor, patch }
#   repository must be one of { local, sonatype }
#
# example: ./publish.sh snapshot minor local
#   will increment minor version component and publish snapshots to local maven repository

if [[ $# -ne 3 ]]
then
  echo "usage: ./publish.sh <release type> <release scope> <repository>"
  exit 1
fi

RELEASE_TYPE=$1
RELEASE_SCOPE=$2
REPOSITORY=$3

# check that we have a valid release scope
if [[ $RELEASE_SCOPE != 'major' && $RELEASE_SCOPE != 'minor' && $RELEASE_SCOPE != 'patch' ]]
then
  echo "$RELEASE_SCOPE is not valid. (must be one of 'major', 'minor', or 'patch')."
  exit 1
fi

# check that we have a valid repository
if [[ $REPOSITORY = 'sonatype' ]]
then
  PUBLISH_TASK="publish"
elif [[ $REPOSITORY = 'local' ]]
then
  PUBLISH_TASK="publishToMavenLocal"
else
  echo "$REPOSITORY is not valid. (must be one of 'sonatype' or 'local')."
  exit 1
fi


if [[ $RELEASE_TYPE = 'final' || $RELEASE_TYPE = 'candidate' ]]
then
  if git remote show origin | grep "Fetch URL: git@github.com:Workday/warp-core.git"; then
    echo "we are on main fork, proceeding with $RELEASE_TYPE release"
    # create our repo tag
    # TODO despite being in `runOnceTasks`, it appears `candidate` is run multiple times with -PallScalaVersions, incorrectly creating multiple tags
    echo "creating repo tag for $RELEASE_TYPE release"
    ./gradlew -Prelease.scope=$RELEASE_SCOPE clean $RELEASE_TYPE

    # then publish our primary module with other scala versions
    echo "publishing $REPOSITORY artifacts for $RELEASE_SCOPE $RELEASE_TYPE release"
    ./gradlew -Prelease.useLastTag=true -PallScalaVersions $PUBLISH_TASK
  else
    echo "we are on a personal fork, must release from main (Workday) fork. aborting $RELEASE_TYPE release"
    exit 1
  fi

elif [[ $RELEASE_TYPE = 'devSnapshot' || $RELEASE_TYPE == 'snapshot' ]]
then
  echo "publishing $REPOSITORY artifacts for $RELEASE_SCOPE $RELEASE_TYPE release. repo tag will not be created."
  ./gradlew -Prelease.scope=$RELEASE_SCOPE -PallScalaVersions clean $RELEASE_TYPE $PUBLISH_TASK
else
  echo "$RELEASE_TYPE is not a valid release type"
  exit 1
fi
