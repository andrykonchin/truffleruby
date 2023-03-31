#!/usr/bin/env bash

set -x
set -e

# Create generated files
pushd ../../yarp
bundle exec rake
popd

rm -rf src/main/c/yarp/src/yarp
cp -R ../../yarp/src src/main/c/yarp/src/yarp

rm -rf src/yarp/java
cp -R ../../yarp/java src/yarp/java