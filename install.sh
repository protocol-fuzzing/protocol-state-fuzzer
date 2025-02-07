#!/usr/bin/env bash

# This can be removed once RALib is available from a maven repository.
git clone "https://github.com/LearnLib/ralib"
cd ralib
git checkout 6de3c3883fffdc0b0a1aba36365daddcc7ccaca5
mvn install -DskipTests
cd ..

mvn install
