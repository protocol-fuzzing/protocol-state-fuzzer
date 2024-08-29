#!/usr/bin/env bash
git clone "https://github.com/LearnLib/ralib"
cd ralib
git checkout 2b6817ed7e532f0aecbd47907d3dc0de4fdf94e0
mvn install -DskipTests
cd ..
mvn install