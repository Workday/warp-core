#!/bin/bash

rm -rf ../docs
hugo -D
mv docs ../
