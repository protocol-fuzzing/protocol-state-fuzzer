# End-to-End Test Scenarios

The end-to-end tests run the full pipeline against an in-memory SUL/T endpoint that behaves
exactly like the stored model, against which the test results are also checked.
Everything runs through the command line entrypoint, similar to a PSF-implementing fuzzer.
The tests support both Mealy Machines and Register Automata.

The file hierarchy follows the format of `{mealy,ra}/<protocol>/<scenario>/<files>`.
Each `protocol` may contain many `scenarios` that represent variants of the same protocol.

We can extend the `scenarios` of an existing protocol, or create a new `protocol` with several scenarios.
Scenarios outside a `protocol` directory are ignored, as well as special directories, such as `mealy/common-args/`.

A scenario is conventionally named `<scenario> :: <role>-<variant>[-attribute]`:

| Roles   | Variants                                    | Attributes                                                                |
|---------|---------------------------------------------|---------------------------------------------------------------------------|
| client  | `buggy`: The model has one intended bug     | `slow`: Adds latency to the model behavior (currently only for `buggy`)   |
| server  | `fixed`: The model does not exhibit the bug |                                                                           |


## How to Run Standalone

The tests are the `*IT` classes in the `e2e` test package run by `failsafe`.
`mvn verify` runs them after the unit tests, but to explicitly run them use:

```
mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=MealyScenariosIT,RaScenariosIT
```

## Mealy Scenarios

### Scenario Files

The `mealy/<protocol>/<scenario>/` contains:

| File                  | Required     | Description                                            |
|-----------------------|--------------|--------------------------------------------------------|
| `model.dot`           | yes          | The model the endpoint executes                        |
| `scenario.properties` | yes          | Properties such as role and latency                    |
| `args/learn`          | yes          | Args for learning the endpoint                         |
| `args/test`           | yes          | Args for testing the endpoint against `model.dot`      |
| `args/probe`          | if `latency` | Args for probing the endpoint's `responseWait`         |
| `args/identify`       | yes          | Args for identifying the endpoint with its fingerprint |


Some common arguments are shared in `mealy/common-args/`:

| File          | Enabled         | Description                                  |
|---------------|-----------------|----------------------------------------------|
| `diff-test`   | yes, by default | The common args for differential testing     |
| `fingerprint` | yes, by default | The common args template for fingerprinting  |

#### `model.dot`

A Mealy machine in DOT format with edges labeled `INPUT / OUTPUT`, where
inputs and outputs are message names. The model must be complete,
i.e. define every input in every state.

Start the file with a `/* ... */` comment, which Graphviz and the model parser ignore.
Usually the comment should contain:

1. A one-line summary, and a `Covers:` line with what the scenario exercises.
2. Instructions on how to read the model and any special outputs used.
3. The key traces (the happy path, intended bugs, other notable behavior), one
   step per line in the model's notation.

For example:

```
/*
 * Authenticating server with one bug.
 * Covers: TIMEOUT outputs, a sink state.
 *
 * Each step reads <received> / <sent>, where
 * TIMEOUT means nothing is sent.
 *
 * Happy path:
 *   M1 / M2
 *   M3 / M4
 */
```

On each received message, the endpoint takes the transition of that input and
replies with its output. Messages that do not appear in the model are ignored. Additionally, there are some special outputs:

1. `TIMEOUT`       : Sends no reply
2. `SOCKET_CLOSED` : Sends no reply and closes the connection. After that, the model must keep outputting it.


#### Initial message

The `client` usually initiates the protocol sending the first message to the `server`, which in this case is PSF.

The model can express the client's initial message in the pseudo-transition of the starting state. This message is not part of the expected model, however PSF waits for it when acting like the `server`.

```
__start0 -> s0 [label="- / MESSAGE"]
```

#### `scenario.properties`

| Key       | Required | Description                                          |
|-----------|----------|------------------------------------------------------|
| `role`    | yes      | `server` or `client`, the role of the endpoint       |
| `latency` | no       | Maximum response latency in ms, `0` by default               |

### Argument Files

They store the arguments of a PSF command, one per line allowing comment lines with `#` :
In this document, `-arg` denotes a command-line argument and `arg` its value, which can be specified like `-arg arg`.

The argument files also utilize the following custom properties:

| Property        | Value                                                              |
|-----------------|--------------------------------------------------------------------|
| `scenario.dir`  | The directory of the scenario used to run the model                |
| `work.dir`      | The work directory of the run holding generated inputs and outputs |


A normal PSF-implementing fuzzer usually specifies both `-processDir` and `-cmd` to execute an SUL as a separate process.
Our endpoints here find the `model.dot` using `-processDir ${scenario.dir}`, and do not require a `-cmd`, because they operate only in-memory.

### Learning via `args/learn`

The learning arguments are specified in `args/learn`.
The endpoint behaves like the scenario's model (selected via `-processDir`).

The process is the following:
- The test generates the `${work.dir}/alphabet.xml`, which the args point to, from the model's inputs.
- The endpoint's model is learned with the learning and equivalence algorithms set in the args.
- The learned model is written to the output directory, along with the statistics, the mapper connection config and a copy of the command's arguments.

The learning is covered by two tests:
- the learning test checks that the command produced a learned model, that the statistics and the mapper connection config are in the output directory, and that it holds the command's arguments, i.e. the args file's content followed by the other arguments
- the equivalence test checks that the learned model is equivalent to the scenario's model, using AutomataLib's separating word search, which finds a separating word if it is not

The learning runs once per scenario, and the learned models are cached and reused by subsequent tests.
If a scenario does not learn a model equivalent to its own, the subsequent tests that depend on its learned model are skipped.

### Testing via `args/test`

The testing arguments are specified in `args/test`, which includes `-times`, the number of times each input word is run.
The endpoint behaves like the scenario's model (selected via `-processDir`), and the same model is also given to the test runner as its specification (via `-testSpecification`).

The process is the following:
- A temporary file `${work.dir}/tests.txt` is populated with input words over the scenario's inputs, e.g. `M1` and `M2`.
  It contains every input word, from a single input up to a **fixed maximum length of 3**, one input word per line and separated by `reset`, for example:

  ```
  M1
  reset
  M2
  reset
  M1 M1
  reset
  M1 M2
  reset
  ...
  reset
  M2 M2
  reset
  M1 M1 M1
  reset
  M1 M1 M2
  reset
  ...
  reset
  M2 M2 M2
  ```

- For each input word, the test runner starts a fresh connection, sends its inputs in order and records the outputs. It repeats this `times` times.

Since the scenario models are deterministic, the test checks that:
- every input word is run `times` times, with one result per word
- all runs of a word give the same output
- that output equals the output of the scenario model
- that output equals the output of the specification, which is the same model here

### Timing Probe via `args/probe`

The probe arguments are specified in `args/probe` and contain the input word rerun count `-times T`, the field `-probeCmd responseWait` that the probe will search values for, the inclusive bounds `-probeLow` and `-probeHigh`, and the probe tolerance `-probeTol`.

The endpoint behaves like the scenario's model (selected via `-processDir`) and exhibits the maximum response `latency` (ms) set in `scenario.properties`. PSF waits for each response of the endpoint for at most `-responseWait` ms.

To avoid waiting, the endpoint's latency is simulated by comparing the two values:

- if `responseWait >= latency`, the endpoint delivers every response in time, so it looks deterministic. This is the case for the non-probe commands that do not specify a latency, so `responseWait >= latency = 0`.

- if `responseWait < latency`, the endpoint drops every second response by design, so it looks non-deterministic. When a response is dropped, PSF sees a `TIMEOUT`.
    - Dropping every response would also be detected, but the endpoint would just time out consistently.
    - Dropping every second response makes the endpoint inconsistent between repetitions, as a real endpoint is when it is waited on too briefly.
    - Dropping random responses would not be reproducible.

For example, with `responseWait < latency`, `times = 2` and a model that answers each input `Mi` with `Oi`, PSF sees:

| Input word | Run | PSF sees                      | Endpoint dropped |
|------------|-----|-------------------------------|------------------|
| `M1 M2`    | 1   | `M1 O1 M2 TIMEOUT`            | `O2`             |
| `M1 M2`    | 2   | `M1 O1 M2 TIMEOUT`            | `O2`             |
| `M1 M2 M3` | 1   | `M1 O1 M2 TIMEOUT M3 O3`      | `O2`             |
| `M1 M2 M3` | 2   | `M1 TIMEOUT M2 O2 M3 TIMEOUT` | `O1, O3`         |

The endpoint drops every second response regardless of which input words PSF runs.
The runs of `M1 M2` agree, but both input words differ from what PSF sees with a longer `responseWait`, which the probe detects as nondeterminism.
`M1 M2 M3` also differs between its own runs, since it has an odd number of responses.

The process is the following:
- The test generates `tests.txt` as for Testing.
- The timing probe records the endpoint outputs for every input word using `responseWait = probeHigh`, which is assumed to give the expected outputs.
- The timing probe runs each input word `times` times searching for the lowest `responseWait` so that all `times` runs:
    - agree on the same output, i.e. the endpoint answers consistently
    - agree with the reference output, i.e. the observed common output is the expected one, instead of `TIMEOUT`.
- The timing probe search doubles from the low end of the range, and then bisects, with a resolution of `probeTol`.

The test checks that:
- the timing probe ran and found a `responseWait*`,
- it holds that `latency <= responseWait* <= latency + probeTol`

### Differential Testing via `common-args/diff-test`

The differential testing arguments are specified in `common-args/diff-test` and are common to all scenarios, since they run on learned models instead of an endpoint.
It uses the following properties that are automatically populated from the cached learning of each scenario (see Learning), whose output directory holds the learned model and alphabet:

| Property             | Value                                |
|----------------------|--------------------------------------|
| `model.a`, `model.b` | The models to compare, as DOT files  |
| `name.a`, `name.b`   | Their names in the result            |
| `alphabet`           | The common alphabet of both models   |


The process is the following:
- Each learned model is compared with its own scenario's `model.dot` using the learned alphabet.
- Each pair of scenarios under the same protocol, role, and inputs, are compared to each other.

The differential testing is covered by two tests, which differ in how the expected result is obtained:
- the scenario test compares a learned model with its own scenario's model, which are expected to be equivalent and thus the diff tester to find no differences.

- the pair test compares the learned models of two scenarios.
  The expected divergences depend on how the models differ, so the test computes a reference with a separate algorithm, AutomataLib's separating word search, run on the same two learned models.
  It checks that:
    - there are divergences exactly when the two models differ
    - each divergence is a real difference, i.e. following its inputs the two models agree except on the last one
    - the shortest divergence is as short as the shortest separating word

If a scenario did not learn a model, its scenario test is skipped, and so are the pair tests with it.
If a scenario learned a model that is not equivalent to its own, only its scenario test fails, since the pair tests use the learned models as they are.

### Fingerprinting and Identification

A fingerprint is an Adaptive Distinguish Graph (ADG) that separates the learned models of a directory, so that its leaves hold unique models.
The identification uses it to find which of these models an endpoint behaves like, and then runs a conformance test of the endpoint against them.
Both run on the learned models of a group, i.e. a protocol and role, so that only models with the same inputs are compared.

The process is the following:
- The learned models of each scenario of a group are collected into one directory per group, with a subdirectory per scenario holding its `learnedModel.dot` and `alphabet.xml`.
- `fingerprint` runs on each group and writes its ADG.
- `identify` runs for each scenario's endpoint with the ADG of its group. The models found to identify the endpoint are then used for the conformance test.

#### Fingerprinting via `common-args/fingerprint`

The fingerprinting arguments are specified in `common-args/fingerprint` and are common to all scenarios, since they run on learned models instead of an endpoint.
It uses the following properties:

| Property     | Value                                                                  |
|--------------|------------------------------------------------------------------------|
| `models.dir` | The directory of a group, with one subdirectory per learned model      |
| `adg.file`   | The file holding the ADG of the group                                  |

The fingerprinting is covered by one test checking that
- every ADG leaf holds one set of equivalent scenario models, e.g. `{<role>-buggy, <role>-buggy-slow}`, and
- every such set has a leaf

#### Identification via `args/identify`

The identification arguments are specified in `args/identify`.
The endpoint behaves like the scenario's model (selected via `-processDir`) and is identified with `-identify ${adg.file}`.
It is tested for conformance against the identified models in `-conformance ${models.dir}/`.
The conformance path needs its trailing `/`, because the name of each identified model is appended to it without a separator.

The identification is covered by two tests, a positive and a negative, which differ in the models used for the conformance test:
- the positive test uses the learned models of the group and checks that:
    - the identified models are exactly the scenarios with a model equivalent to the endpoint's
    - the conformance test succeeds, with a result equivalent to the scenario's model and written to a file

- the negative test checks that the conformance test rejects models that are not equivalent to the endpoint's:
    - it replaces the learned model of each scenario with that of the first inequivalent scenario of the group
    - it checks that the identified models are the same as in the positive test
    - it checks that the conformance test fails, because the replacement is inequivalent to the endpoint's model

A group with a scenario that did not learn a model, or learned one not equivalent to its own, is skipped, since the fingerprint is built from the models of all of them. For example, if learning of `<protocol>/server-fixed` failed, all `<protocol>/server-*` scenarios are skipped, while the `<protocol>/client-*` scenarios still run.

## Register Automata (RA) Scenarios

The RA scenarios model messages that carry data values.
They follow the file hierarchy and naming of the Mealy scenarios, but only learning and testing are currently supported by RA.

### Scenario Files

The `ra/<protocol>/<scenario>/` contains:

| File                  | Required | Description                                       |
|-----------------------|----------|---------------------------------------------------|
| `model.xml`           | yes      | The model the endpoint executes                   |
| `scenario.properties` | yes      | Properties such as role (`latency` has no effect) |
| `args/learn`          | yes      | Args for learning the endpoint                    |
| `args/test`           | yes      | Args for testing the endpoint against `model.xml` |

#### `model.xml`

A register automaton follows the RALib's XML format with inputs and outputs being messages with integer data values compared for equality.
In each location that reads an input, the guards of every input must cover all data values.

Start the file with a `<!-- ... -->` comment, as for `model.dot`, with a summary, a `Covers:` line and the key traces.
Write the data values in parentheses, e.g. `M1(t) / M2`, and note which ones are fresh.

The endpoint sends a message as its name followed by its data values, separated by spaces, e.g. `M1 3`.
In the model, each input transition is followed by an output transition.
On each received message, the endpoint takes the enabled input transition and replies with the output of the transition that follows.
Messages that are not inputs of the model are ignored.
Additionally, there are some special outputs:

1. `TIMEOUT`       : Sends no reply
2. `SOCKET_CLOSED` : Sends no reply and closes the connection. After that, the model must keep outputting it.

A fresh output value is an output parameter that is not taken from a register or a constant.
The endpoint sends the smallest integer greater than all the data values of the connection so far.

The endpoint sends nothing on connect.
A client's initial message is not part of the learned model anyway, so it is left out of `model.xml` and only mentioned in its comment, as in `auth-simple`.

### Argument Files

The argument files work as for the Mealy scenarios.

The `${work.dir}/alphabet.xml` is generated by the test from the model's inputs and outputs, with the data types of their parameters, e.g. `<Input name="M1" params="token"/>`. The endpoint knows the message formats from it, as the SUL of a real fuzzer knows those of its protocol.

### Learning via `args/learn`

The learning arguments are specified in `args/learn`.
The endpoint behaves like the scenario's model (selected via `-processDir`).

The process is the following:
- The test generates the `${work.dir}/alphabet.xml`, which the args point to, from the model's inputs and outputs.
- The endpoint's model is learned with the algorithms set in the args.
- The learned model is written to the output directory, along with the statistics, the mapper connection config and a copy of the command's arguments.

The learning is covered by two tests:
- the learning test checks that the command produced a learned model, that the statistics and the mapper connection config are in the output directory, and that it holds the command's arguments, i.e. the args file's content followed by the other arguments
- the equivalence test checks that the learned model is equivalent to the scenario's model, using RALib's `IOEquivalenceTest`, which finds a counterexample word if it is not

The learning runs once per scenario, and its result is reused by the equivalence test, which is skipped if there is no learned model.

### Testing via `args/test`

The testing arguments are specified in `args/test`, which includes `-times`, the number of times each input word is run.
The endpoint behaves like the scenario's model (selected via `-processDir`).
The RA test runner takes no `-testSpecification`, so the expected outputs come from a RALib simulation of the scenario's model instead.

The process is the following:
- A temporary file `${work.dir}/tests.txt` is populated with input words over the scenario's inputs.
  It contains every input word, from a single input up to the same **fixed maximum length** as for the Mealy scenarios, one input word per line and separated by `reset`.
  An input with a parameter is instantiated with each of the data values 0, 1 and 2, written like `Mi[0]`.
  These are as many values as the maximum length, so that the words cover every pattern of equal and different values, e.g. `M2[0] M2[0]` and `M2[0] M2[1]`, which the guards of the model tell apart, for example:

  ```
  M1
  reset
  M2[0]
  reset
  M2[1]
  reset
  ...
  reset
  M1 M2[0]
  reset
  ...
  ```

- For each input word, the test runner starts a fresh connection, sends its inputs in order and records the outputs. It repeats this `times` times.

Since the scenario models are deterministic, the test checks that:
- every input word is run `times` times, with one result per word
- all runs of a word give the same output
- that output equals the output of the simulation of the scenario's model
