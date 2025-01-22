# ProtocolState-Fuzzer

[![CI](https://github.com/protocol-fuzzing/protocol-state-fuzzer/actions/workflows/ci.yml/badge.svg)](https://github.com/protocol-fuzzing/protocol-state-fuzzer/actions/workflows/ci.yml)

## Contents

* [Description](#description)
* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Quick Start](#quick-start)
* [Logging](#logging)
* [Resource Files](#resource-files)
* [Used By](#used-by)
--------

## Description

ProtocolState-Fuzzer is a _generic_, _modular_ and _extensible_ protocol state fuzzer,
which can be used as a framework for the state machine learning and fuzzing of
different network protocol implementations.

ProtocolState-Fuzzer supports the following functionality:

1. Learning a state machine model of a protocol-specific client or server implementation.
2. Testing (executing sequences of inputs) of a protocol-specific client or server implementation.

## Prerequisites

* Java 17 JDK.
* maven correctly setup to point to Java 17 JDK.
* graphviz library, containing the dot utility, which should be located in the system's PATH.

## Installation

Assuming the commands are executed from the root directory:

1. To check the prerequisites use:
```bash
java -version
mvn -version
dot -V
```

2. To install the ProtocolState-Fuzzer use:
```bash
mvn install
```
This command will:

* create in the `target` directory the **jar** file containing the compiled
  classes and install it in the local Maven repository;

* create in the `target` directory the javadocs in **html** and **jar** format.
  The **html** pages will be located in the `target/apidocs` directory and can
  be viewed using a web browser. The **jar** format will be installed in the
  local Maven repository and can be used for inline documentation during
  programming;

* create in the `target` directory the **jar** file containing the source files,
  and install it in the local Maven repository. This can be used for debugging
  purposes and for inline browsing of the source code during programming.

> In the output of `mvn install` command, the installation of the above jars
  will be shown above the **Build Success** message. If the **javadocs** or the
  **sources** **jar** are not needed, they can be manually removed from the
  local Maven repository.

## Quick Start

> The import statements are omitted from the following snippets for brevity.

A main class can be like this:
```java
public class Main {
    public static void main(String[] args) {
        // Multibuilder implements all necessary builders
        MultiBuilder mb = new MultiBuilder();

        // single parentLogger, if Main resides in the outermost package
        String[] parentLoggers = {Main.class.getPackageName()};

        CommandLineParser<?> commandLineParser = new CommandLineParser<>(mb, mb, mb, mb);
        commandLineParser.setExternalParentLoggers(parentLoggers);

        List<LearnerResult> results = commandLineParser.parse(args, true);

        // further process the results if needed
    }
}
```

Notes:

* The [CommandLineParser](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/entrypoints/CommandLineParser.java)
  class is one entrypoint to the ProtocolState-Fuzzer. Its constructor needs some builders,
  which can be implemented for instance as in the `MultiBuilder` class defined below,
  which is specific to `MealyMachine` models.

* The package name of the *Main* class suffices as the *external parent logger* of the
  application, when the Main class resides in the outermost package. The `setExternalParentLoggers`
  method is used in order to have the application's log level follow the ProtocolState-Fuzzer's
  log level in case the arguments `-debug` or `-quiet` are encountered.

* There are different `parse` methods in `CommandLineParser`, which can parse
  and execute the provided arguments, export the learned `DOT` files to `PDF` and
  use specified `Consumers` on the results. The `parse` method used above, exports the
  learned `DOT` files to `PDF`.

```java
public class MultiBuilder implements
    StateFuzzerConfigBuilder,
    StateFuzzerBuilder<MealyMachineWrapper<InputImpl, OutputImpl>>,
    TestRunnerBuilder,
    TimingProbeBuilder {

    // InputImpl, OutputImpl, AlphabetPojoXmlImpl need to be implemented
    protected AlphabetBuilder<InputImpl> alphabetBuilder = new AlphabetBuilderStandard<>(
        new AlphabetSerializerXml<InputImpl, AlphabetPojoXmlImpl>(InputImpl.class, AlphabetPojoXmlImpl.class)
    );

    // ExecutionContextImpl, SulBuilderImpl need to be implemented
    protected SulBuilder<InputImpl, OutputImpl, ExecutionContextImpl> sulBuilder = new SulBuilderImpl();
    protected SulWrapper<InputImpl, OutputImpl, ExecutionContextImpl> sulWrapper = new SulWrapperStandard<>();

    @Override
    public StateFuzzerClientConfig buildClientConfig() {
        return new StateFuzzerClientConfigStandard(
            new LearnerConfigStandard(),
            new SulClientConfigStandard(new MapperConfigStandard(), new SulAdapterConfigStandard()),
            new TestRunnerConfigStandard(),
            new TimingProbeConfigStandard()
        );
    }

    @Override
    public StateFuzzerServerConfig buildServerConfig() {
        return new StateFuzzerServerConfigStandard(
            new LearnerConfigStandard(),
            new SulServerConfigStandard(new MapperConfigStandard(), new SulAdapterConfigStandard()),
            new TestRunnerConfigStandard(),
            new TimingProbeConfigStandard()
        );
    }

    @Override
    public StateFuzzer<MealyMachineWrapper<InputImpl, OutputImpl>> build(StateFuzzerEnabler stateFuzzerEnabler) {
        return new StateFuzzerStandard<>(
            new StateFuzzerComposerStandard<>(stateFuzzerEnabler, alphabetBuilder, sulBuilder, sulWrapper).initialize()
        );
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        return new TestRunnerStandard<>(testRunnerEnabler, alphabetBuilder, sulBuilder, sulWrapper).initialize();
    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        return new TimingProbeStandard<>(timingProbeEnabler, alphabetBuilder, sulBuilder, sulWrapper).initialize();
    }
}
```

## Logging

The default log level of ProtocolState-Fuzzer is `ERROR`. One way to change this is
via a `log4j2.xml` configuration. An example is the configuration used for testing
[here](src/test/resources/log4j2.xml), which sets the log level to `TRACE`.
The log level can be changed to:

* `INFO`, in order to have minimal and sufficient logging or
* `DEBUG`, in order to also log the exchanged input and output messages


## Resource Files

The following files can be provided in the `src/main/resources` directory, in
order to be discovered by ProtocolState-Fuzzer.

* `default_alphabet.xml` **(Mandatory)** This file acts as the default alphabet
  file, in case no other alphabet file is specified via the `-alphabet` argument
  parameter. A template of this file is [here](src/test/resources/default_alphabet.xml),
  which can be read using an implementation of
  [AlphabetPojoXml](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/components/learner/alphabet/xml/AlphabetPojoXml.java).
  If no alphabet file is specified via the `-alphabet` argument parameter and the
  `default_alphabet.xml` is not found in resources, then a fatal exception occurs,
  because an alphabet cannot be built and the process cannot continue.

* `default_fuzzer.properties` **(Optional).** It allows to specify some properties
  that can be used in the argument files. You can see an example of this file
  [here](src/test/resources/default_fuzzer.properties).
  Regarding the entry `results.learning.clients=results/clients`, the property
  `results.learning.clients` can be used in an argument file as `${results.learning.clients}`,
  in order to be resolved to `results/clients`. Additionally, the JVM property
  `-Dfuzzer.properties=file` can be used to load a specific properties file instead
  of `default_fuzzer.properties`, like `java -Dfuzzer.properties=file -jar ...`.

* `default_mapper_connection.config` **(Optional).** This file allows to specify
  some configuration options for the specific mapper. Also the `-mapperConnectionConfig`
  argument parameter can be used in order to use another configuration file instead
  of the default one. The input stream of the configuration file can be obtained
  via `getMapperConnectionConfigInputStream()`
  in [MapperConfig](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/components/sul/mapper/config/MapperConfig.java).
  The content format of this file relies on the user. Note that if no configuration
  file is specified via `-mapperConnectionConfig` and the `default_mapper_connection.config`
  is not found in resources, then `getMapperConnectionConfigInputStream()` returns `null`.

## Used By

* [DTLS-Fuzzer](https://github.com/assist-project/dtls-fuzzer)
* [EDHOC-Fuzzer](https://github.com/protocol-fuzzing/edhoc-fuzzer)
