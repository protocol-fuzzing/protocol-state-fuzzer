# ProtocolState-Fuzzer
## Contents

* [Description](#description)
* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Quick Start](#quick-start)
* [Used By](#used-by)
--------

## Description

ProtocolState-Fuzzer is a generic, modular and extensible protocol state fuzzer,
which can be used as a framework for the protocol state fuzzing of different protocols.
It draws inspiration from a similar tool, called [DTLS-Fuzzer](https://github.com/assist-project/dtls-fuzzer),
for the DTLS network protocol.

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

* create in the **target** directory the **jar** file containing the `compiled classes`
  and install it in the local Maven repository;

* create in the **target** directory the `javadocs` in **html** and **jar** format.
  The **html** format will be located in the **target/apidocs** directory and can be
  used to browse the documentation using a web browser. The **jar** format will be
  installed in the local Maven repository and can be used for inline documentation
  during programming;

* create in the **target** directory the **jar** file containing the `source files`
  and install it in the local Maven repository. This can be used for debugging
  purposes and for inline browsing of the source code during programming.

> Normally in the output of **mvn install** command, above the **Build Success**
  message the installation of the above jars will be logged, so if the
  **javadocs** or the **sources** **jar** are not needed they can be manually
  removed from the local Maven repository.

## Quick Start

> The import statements are omitted from the following snippets for brevity.

A main class can be like this:
```java
public class Main {
    public static void main(String[] args) {
        // multibuilder implements all necessary builders
        MultiBuilder mb = new MultiBuilder();

        // single parentLogger, since Main resides in the outmost package
        String[] lgrs = {Main.class.getPackageName()};

        CommandLineParser commandLineParser = new CommandLineParser(mb, mb, mb, mb, lgrs);
        commandLineParser.parse(args);
    }
}
```

The basic class is the `CommandLineParser` class that is one entrypoint to the
ProtocolState-Fuzzer. The constructor of the class takes as parameters a number
of builders, which are implemented in the `MultiBuilder` class defined below.
The last parameter of the constructor is a list of Logger names, whose logging
level behavior can ProtocolState-Fuzzer change. Since Main resides in the outmost
package, in this example, its package name is (normally) contained in the prefix of the
Logger name of any subpackage, meaning that a change to the logging level of this 
'parent Logger' affects any other Logger with this prefix in its name.

```java
public class MultiBuilder implements 
    StateFuzzerConfigBuilder, StateFuzzerBuilder, TestRunnerBuilder, TimingProbeBuilder {

    // AlphabetPojoXmlImpl needs to be implemented
    protected AlphabetBuilder alphabetBuilder = new AlphabetBuilderStandard(
            new AlphabetSerializerXml<>(AlphabetPojoXmlImpl.class)
    );

    // SulBuilderImpl needs to be implemented
    protected SulBuilder sulBuilder = new SulBuilderImpl();
    protected SulWrapper sulWrapper = new SulWrapperStandard();

    // SulClientConfigImpl and MapperConfigImpl need to be implemented
    @Override
    public StateFuzzerClientConfig buildClientConfig() {
        return new StateFuzzerClientConfig(
                new LearnerConfig(),
                new SulClientConfigImpl(new MapperConfigImpl()),
                new TestRunnerConfig(),
                new TimingProbeConfig()
        );
    }

    // SulServerConfigImpl (and MapperConfigImpl) need to be implemented
    @Override
    public StateFuzzerServerConfig buildServerConfig() {
        return new StateFuzzerServerConfig(
                new LearnerConfig(),
                new SulServerConfigImpl(new MapperConfigImpl()),
                new TestRunnerConfig(),
                new TimingProbeConfig()
        );
    }

    @Override
    public StateFuzzer build(StateFuzzerEnabler stateFuzzerEnabler) {
        return new StateFuzzerStandard(
                new StateFuzzerComposerStandard(stateFuzzerEnabler, alphabetBuilder, sulBuilder, sulWrapper)
        );
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        return new TestRunner(testRunnerEnabler, alphabetBuilder, sulBuilder, sulWrapper);
    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        return new TimingProbe(timingProbeEnabler, alphabetBuilder, sulBuilder, sulWrapper);
    }
}
```

Regarding the comments about implementing some classes:

* `AlphabetPojoXmlImpl` should **extend** the `AlphabetPojoXml` abstract class

* `SulBuilderImpl` should **implement** the `SulBuilder` interface, which needs
   to build an `AbstractSulImpl` class that should **extend** the `AbstractSul` abstract class

* `MapperConfigImpl` can **extend** the `MapperConfig` class, in order to provide
   additional options for the protocol-specific mapper

* `SulClientConfigImpl` should **extend** the `SulClientConfig` abstract class

* `SulServerConfigImpl` should **extend** the `SulServerConfig` abstract class


## Used By

* [EDHOC-Fuzzer]()
