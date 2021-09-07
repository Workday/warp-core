#!/bin/bash

if ! command -v hugo &> /dev/null
then
    echo "hugo could not be found, please install and rerun"
    exit 1
fi

rm -rf ../docs
hugo -D
mv docs ../
