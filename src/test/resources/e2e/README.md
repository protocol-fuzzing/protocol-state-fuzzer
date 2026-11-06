# End-to-end test scenarios

Each subdirectory of `mealy/` (Mealy machines) and `ra/` (register automata)
is a protocol, and each of its subdirectories is one scenario: a variant of
the protocol, e.g. `mealy/auth-simple/server-buggy/`. The end-to-end tests run
the full ProtocolState-Fuzzer pipeline against a fake, in-memory endpoint that
executes the scenario's model, then check that the learned model is equivalent
to it.

To add a behavior, add a scenario directory, or a protocol directory for a new
protocol. No code changes are needed. A scenario outside a protocol directory
is an error; other directories, such as `mealy/common-args/`, are not
scenarios.

A scenario is identified by `<protocol>/<name>`, e.g. `auth-simple/server-buggy`.
Scenarios are named `<role>-<variant>[-slow]`, e.g. `server-buggy` or
`server-buggy-slow`:

| Part    | Meaning                                                  |
|---------|----------------------------------------------------------|
| `buggy` | The protocol with one intended bug                       |
| `fixed` | The same protocol without the bug                        |
| `slow`  | The same model as without `-slow`, with a `latency`      |

## What is checked

The end-to-end tests run everything through the command line, as a real
fuzzer is used, with the scenario's `args/` files and, for Mealy scenarios, the
common ones in `mealy/common-args/`, see below.

For every Mealy scenario, they:

1. learn the endpoint's model and check that it is equivalent to the
   scenario's model, and, chained after learning with `--`, compare the
   learned model with `model.dot` by `diff-test`, expecting no divergences;
2. run all input words up to length 3 with the test runner, using the
   scenario's model as test specification, and check every output against it.

For every scenario with a `latency`, they also run the timing probe on
`responseWait` and check that it finds the latency.

Differential testing also runs on the learned models of each pair of
scenarios with the same protocol, role and inputs, e.g. `server-buggy` vs
`server-fixed`. Divergences are expected if and only if the two scenario
models differ. Each divergence must also be a real difference: following its
inputs, the two scenario models give the same outputs except on the last
input. The shortest divergence must be as short as the shortest input
sequence on which the scenario models differ.

Fingerprinting and identification also run on the learned models, collected
into one directory per group, i.e. protocol and role:

1. fingerprinting each group's models, expecting every leaf of the resulting
   ADG to hold one set of equivalent models, e.g. `server-buggy` and
   `server-buggy-slow`;
2. identifying each scenario's endpoint with the ADG of its group, expecting
   exactly the scenarios with equivalent models, followed by a conformance
   test of the identified models, expecting it to pass;
3. the same, with each identified model replaced by the learned model of an
   inequivalent scenario, expecting the conformance test to fail.

So a scenario that differs from another one in a known way, such as
`server-fixed` from `server-buggy`, also tests differential testing,
fingerprinting and identification.

Each Mealy scenario is learned once per test run, and differential testing,
fingerprinting and identification reuse the learned models. If a scenario
fails to learn, its learning test fails, and the tests that need its learned
model are skipped instead of failing too. For example, if `server-fixed` fails
to learn:

- differential testing skips the pairs with `server-fixed`, i.e.
  `server-buggy` vs `server-fixed` and `server-buggy-slow` vs `server-fixed`;
- fingerprinting and identification skip all `auth-simple` server scenarios,
  since the fingerprint is built from the models of all of them. The client
  scenarios still run.

For every RA scenario, they:

1. learn the endpoint's model and check that it is equivalent to the
   scenario's model, by RALib's `IOEquivalenceTest`;
2. run all input words up to length 3, with the data values 0, 1 and 2, with
   the test runner, and check every output against a simulation of the
   scenario's model; the RA test runner takes no test specification.

The timing probe, differential testing, fingerprinting and identification
only support Mealy machines, so they do not run on RA scenarios, and there are
no `-slow` RA scenarios.

## Mealy scenarios: `mealy/<protocol>/<name>/`

| File                  | Required | Description                                       |
|-----------------------|----------|---------------------------------------------------|
| `model.dot`           | yes      | The model the endpoint executes and learning must reproduce |
| `scenario.properties` | yes      | Scenario settings, see below                      |
| `args/learn`          | yes      | Command-line arguments learning the endpoint, see below |
| `args/test`           | yes      | Command-line arguments testing the endpoint against `model.dot` |
| `args/probe`          | if `latency` | Command-line arguments probing the endpoint's `responseWait` |
| `args/identify`       | yes      | Command-line arguments identifying the endpoint with the fingerprint of its protocol and role |

### `model.dot`

A Mealy machine in DOT format with edges labeled `INPUT / OUTPUT`, where
inputs and outputs are message names. The model must be complete, i.e. define
every input in every state.

Start the file with a `/* ... */` comment describing the scenario. DOT comments
are ignored by both Graphviz and the model parser. The comment contains:

1. A one-line summary, and a `Covers:` line listing what the scenario
   exercises, e.g. `TIMEOUT outputs, a sink state`.
2. A legend: `Each step reads <received> / <sent>`, plus the meaning of any
   special outputs used.
3. The key traces (the happy path, intended bugs, and other notable behavior),
   one step per line in the model's own `INPUT / OUTPUT` notation, so each
   trace follows a path of edges. A message sent on connect is written
   `- / MESSAGE`, like the start edge below.

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
 *   AUTH / ACK
 *   SREQ / SRES
 */
```

On each received message, the endpoint takes the transition of that input and
replies with the output message, except for these outputs:

| Output          | Endpoint behavior                                     |
|-----------------|-------------------------------------------------------|
| `TIMEOUT`       | Sends no reply                                        |
| `SOCKET_CLOSED` | Sends no reply and closes the connection; the model must keep outputting `SOCKET_CLOSED` from then on |

### Initial message

A message the endpoint sends on connect, typically by a client, is the label
`- / MESSAGE` of the start edge:

```
__start0 -> s0 [label="- / AUTH"]
```

It is not a transition of the model: the model parser ignores it, and learned
models do not contain it.

The fuzzer waits for it as for an accepted connection, regardless of
`responseWait` and `latency`.

### `scenario.properties`

| Key              | Required | Description                                        |
|------------------|----------|----------------------------------------------------|
| `role`           | yes      | `server` or `client`: the role of the endpoint being learned |
| `latency`        | no       | Simulated response latency in ms, default `0`. With a shorter receive timeout, responses are lost every other time, which makes the endpoint look nondeterministic |

### `args/`

The tests run each scenario as a real fuzzer is run: they pass
the path of an args file as the first argument, followed by `-D` definitions
of the properties it uses:

| Property        | Value                                                    |
|-----------------|----------------------------------------------------------|
| `scenarios.dir` | The directory of the Mealy scenarios, `mealy/`           |
| `work.dir`      | The run's work directory, holding `alphabet.xml` and the tests in `tests.txt` (all input words up to length 3) |
| `fingerprint.dir` | `identify` only: the learned models of each protocol and role, in `<protocol>/<role>/<name>/`, and their ADG, in `<protocol>/<role>-adg.dot` |

An args file holds one argument per line; lines starting with `#` are
comments. It selects the scenario's endpoint with
`-processDir ${scenarios.dir}/<protocol>/<name>`, as a real fuzzer selects the
implementation it starts with `-cmd`. No `-cmd` is given, so no process is
started. `-connect` (servers) or `-port` (clients) is required by the command
line, but unused by the in-memory SUL. Write all output under `${work.dir}`.

The tests check that every args file's `-processDir` names its own scenario,
that `test` uses its own `model.dot`, and that `identify` uses its own protocol
and role, so an args file copied from another scenario must be updated.

| File    | Runs                                                              |
|---------|-------------------------------------------------------------------|
| `learn` | `state-fuzzer-<role>`, with `-responseWait` covering the `latency` |
| `test`  | `-test ${work.dir}/tests.txt -times 10` with `-testSpecification` set to the scenario's `model.dot`; each test runs 10 times, so that nondeterministic outputs show |
| `probe` | `-test ${work.dir}/tests.txt -times 2` with `-probeCmd responseWait` |
| `identify` | `-identify ${fingerprint.dir}/<protocol>/<role>-adg.dot` with `-conformance ${fingerprint.dir}/<protocol>/<role>/`; the trailing `/` is needed, since the identified model's name is appended without a separator |

The commands that run no endpoint, `diff-test` and `fingerprint`, are common
to all scenarios, in `mealy/common-args/`, and take their models and output as
properties.

## RA scenarios: `ra/<protocol>/<name>/`

| File                  | Required | Description                                       |
|-----------------------|----------|---------------------------------------------------|
| `model.xml`           | yes      | The register automaton the endpoint executes and learning must reproduce |
| `scenario.properties` | yes      | Scenario settings, as for Mealy scenarios         |
| `args/learn`          | yes      | Command-line arguments learning the endpoint      |
| `args/test`           | yes      | Command-line arguments testing the endpoint       |

### `model.xml`

A register automaton in RALib's XML format, whose inputs and outputs are
messages with data values. Data values are integers compared for equality.
In each location that reads an input, the guards of every input must cover
all data values.

Start the file with a `<!-- ... -->` comment describing the scenario, as for
Mealy scenarios, with data values written in parentheses, e.g.
`SREQ(t) / SRES`, and a note on which values are fresh.

The endpoint sends each message as its name followed by its data values,
separated by spaces, e.g. `SREQ 3`. On each received message, it takes the
enabled transition of that input and replies with the output of the next
transition, except for `TIMEOUT` and `SOCKET_CLOSED`, which behave as for Mealy
scenarios. A fresh output value, i.e. an output parameter not taken from a
register or constant, is the smallest integer greater than all data values of
the connection so far.

The endpoint sends nothing on connect. A client's initial message is not in
the learned model anyway, so it is left out of the model and only mentioned in
its comment, as in `auth-simple`.

### `args/`

As for Mealy scenarios, with `scenarios.dir` the directory of the RA scenarios,
`ra/`, and the same check of `-processDir`:

| File    | Runs                                                              |
|---------|-------------------------------------------------------------------|
| `learn` | `state-fuzzer-<role>` with `SLSTAR` and `IO_RANDOM_WALK`, and a `-timeLimit`, so that a stuck run stops and fails the comparison |
| `test`  | `-test ${work.dir}/tests.txt -times 10`, without `-testSpecification` |

The alphabet in `${work.dir}/alphabet.xml` lists the model's inputs and
outputs with the data types of their parameters, e.g.
`<Input name="SREQ" params="token"/>`. The fuzzer's SUL knows the message formats
from it, as a real fuzzer knows those of its protocol.
