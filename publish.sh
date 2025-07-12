#!/bin/bash
set -e

# usage: ./publish.sh <release type> <release scope> <repository>
#   release type must be one of { devSnapshot, snapshot, candidate, final }
#   release scope must be one of { major, minor, patch }
#   repository must be one of { local, sonatype }
#
# example: ./publish.sh snapshot minor local
#   will increment minor version component and publish snapshots to local maven repository


# See further documentation here: https://jreleaser.org/guide/latest/examples/maven/maven-central.html
#
# Basic workflow:
# Artifacts are staged in a local working directory, then uploaded to maven central. This allows for local checking
# and verification of the directory layout, signing, and fine content requirements.
#
# Dry-Run:
# It can be useful to set dry-run for the upload phase. This can be accomplished with the environment variable JRELEASER_DRY_RUN=true
#
# As a final verification step, it can also be useful to inspect the uploaded file structure and content before
# the staging repository is closed and content is published.
# If the environment variable JRELEASER_MAVENCENTRAL_STAGE is set to UPLOAD, content will only be uploaded to the remote staging
# repository, and the repository will be left unreleased to allow for a final verification step. The last step of closing
# and releasing the remote repository can be done through the maven central UI.
#

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

# don't allow publishing devSnapshots to sonatype
if [[ $RELEASE_TYPE = 'devSnapshot' && $REPOSITORY = 'sonatype' ]]
then
  echo "Do not publish devSnapshot to sonatype. (use local repository instead)"
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
    if [[ $REPOSITORY = 'sonatype' ]]
    then
      echo "uploading staged artifacts"
      JRELEASER_MAVENCENTRAL_STAGE=UPLOAD ./gradlew -Prelease.useLastTag=true -PallScalaVersions jReleaserDeploy --stacktrace
    fi
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

