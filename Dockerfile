FROM maven:3.9-eclipse-temurin-21

RUN apt-get update && \
    apt-get install -y \
        less \
        vim \
        graphviz

WORKDIR /home/ubuntu/protocol-state-fuzzer

COPY .githooks .githooks
COPY .mvn .mvn
COPY .spotbugs .spotbugs
COPY .spotless .spotless
COPY LICENSE LICENSE
COPY README.md README.md
COPY pom.xml pom.xml
COPY src src

RUN git init &&\
    git config --global user.name "cav26" &&\
    git config --global user.email "cav26@example.com" &&\
    git add . &&\
    git commit -m "Snapshot of protocol-state-fuzzer"

RUN mvn clean install

ENTRYPOINT ["/bin/bash"]
