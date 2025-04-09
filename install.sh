#!/usr/bin/env bash

# This can be removed once RALib is available from a maven repository.
git clone "https://github.com/LearnLib/ralib"
( cd ralib && git checkout 60d3a478e3d2289fcefde233ba5cdcf730812779 && mvn install -DskipTests )

mvn install
