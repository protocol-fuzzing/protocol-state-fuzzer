# ProtocolState-Fuzzer

[![CI](https://github.com/protocol-fuzzing/protocol-state-fuzzer/actions/workflows/ci.yml/badge.svg)](https://github.com/protocol-fuzzing/protocol-state-fuzzer/actions/workflows/ci.yml)

## Contents

* [Description](#description)
* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Quick Start](#quick-start)
* [Learning](#learning)
* [Testing](#testing)
* [Timing](#timing)
* [Logging](#logging)
* [Resource Files](#resource-files)
* [Used By](#used-by)

--------

## Description

ProtocolState-Fuzzer is a _generic_, _modular_ and _extensible_ protocol state fuzzer,
which can be used as a framework for the state machine learning and fuzzing of
different network protocol implementations.

ProtocolState-Fuzzer supports the following functionality for a protocol-specific client or server implementation:

1. Learning the state machine model of the implementation.
2. Testing the implementation by executing test input sequences.
3. Timing the implementation on test input sequences to suggest timeout values
   for avoiding time-related non-determinism during Learning or Testing.

## Prerequisites

* Java 21 JDK.
* maven correctly setup to point to Java 21 JDK.
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
            new StateFuzzerComposerStandard<>(stateFuzzerEnabler, alphabetBuilder, sulBuilder).initialize()
        );
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        return new TestRunnerStandard<>(testRunnerEnabler, alphabetBuilder, sulBuilder).initialize();
    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        return new TimingProbeStandard<>(timingProbeEnabler, alphabetBuilder, sulBuilder).initialize();
    }
}
```

Notes:

* The [StateFuzzer](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/statefuzzer/core/StateFuzzer.java)
  interface represents the learning procedure and is implemented using
  the [StateFuzzerStandard](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/statefuzzer/core/StateFuzzerStandard.java)
  and the [StateFuzzerComposerStandard](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/statefuzzer/core/StateFuzzerComposerStandard.java).

* The [TestRunner](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/statefuzzer/testrunner/core/TestRunner.java)
  interface represents the testing procedure and is implemented using
  the [TestRunnerStandard](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/statefuzzer/testrunner/core/TestRunnerStandard.java).

* The [TimingProbe](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/statefuzzer/testrunner/timingprobe/TimingProbe.java)
  interface represents the timing procedure and is implemented using
  the [TimingProbeStandard](src/main/java/com/github/protocolfuzzing/protocolstatefuzzer/statefuzzer/testrunner/timingprobe/TimingProbeStandard.java).


## Learning
After setting up the specific tool based on ProtocolState-Fuzzer and the SUL of interest,
one can initiate learning using argument files or providing command line arguments.
The argument files can simply contain command-line arguments and their values.
Command-line arguments can also be provided to overwrite those in the argument files.
The `@` symbol before the argument file can be omitted.
The simplest command is:
```
java -jar specific-fuzzer.jar @path/to/argfile
```
The above command without the argument file lists all the available command line options.


## Testing
Testing requires not only an argument file but also a test sequence, usually stored in a file.
Test sequence files can contain input symbols one-per-line.

The test command is:
```
java -jar specific-fuzzer.jar @path/to/arg/file -test path/to/test/file [-additional_param]

Additional Testing Parameters:

-times N
  Run each test sequence N number of times, defaults to 1

-testSpecification path/to/dot/model
  If a .dot model is provided as a specification, the resulting outputs are
  compared against it. The test file will be run both on the implementation
  and on the specification model

-showTransitionSequence
  Shows the sequence of transitions at the end in a nicer format
```


## Timing
Timing is an extension of testing and requires the `-test` and `-probeCmd` parameters to be specified.
It is used to find timing values that prevent non-deterministic outputs from the SUL.
It uses the same initial range for all commands in `-probeCmd` and performs the search
based **only** on the provided tests of `-test`. Thus for learning, the timing values
might need further manual adjustment.
The timing probe command is:
```
java -jar specific-fuzzer.jar @path/to/arg/file -test path/to/test/file -probeCmd <probe commands> [-additional_param]


Available comma-separated probe commands:
    - responseWait    (time to wait for an SUL response)
    - startWait       (time to wait after starting the SUL)
    - <input symbol>  (time to wait for the response of this alphabet input symbol)

    Example: -probeCmd responseWait,startWait,input1,input2


Additional Timing Parameters:

-times N
  Run each test sequence N number of times, defaults to 1

-probeLow N
  The lowest non-negative integer probe timing value, defaults to 0

-probeHigh N
  The highest integer probe timing value, defaults to 1000

-probeTol N
  Search tolerance value that defines the desired precision, defaults to 10
  Small tolerance values increase accuracy but may require more iterations

-probeExport path/to/alphabet/out
  The output file for the alphabet augmented with timing values
  Useful when an input symbol has been provided in -probeCmd
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

* [BLE-Fuzzer](https://github.com/protocol-fuzzing/ble-fuzzer)
* [DTLS-Fuzzer](https://github.com/assist-project/dtls-fuzzer)
* [EDHOC-Fuzzer](https://github.com/protocol-fuzzing/edhoc-fuzzer)
