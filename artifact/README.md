CAV 2026 Artifact
=======================================
Paper title: PSF: A Generic and Extensible Framework for Protocol State Fuzzing

Claimed badges: Available + Functional + Reusable

Justification for the badges:

  Upfront, we note that our artifact comes in two separate forms:

    1. Following the CAV'26 Artifact Evaluation instructions, we provide a Docker
       image where a particular version of the source code of ProtocolState-Fuzzer
       library is included together with instructions on how to run it and verify
       that it works correctly (by running its tests).  This form of the artifact
       has also been deposited on Zenodo and the relevant DOI is:

          TDB

       This form of the artifact can be installed successfully in the local maven
       repository after passing all the required maven phases.
       The most notable of them are:
         - compilation
         - unit testing
         - documentation creation
         - dependency analysis
         - formatting checks via spotless
         - static analysis checks via spotbugs
         - installation

    2. A tagged version of ProtocolState-Fuzzer (PSF) also exists on GitHub:

         https://github.com/protocol-fuzzing/protocol-state-fuzzer/tree/cav26-artifact

       and we expect that most readers of our paper and, more importantly, the
       users of our tool will prefer this form if they decide to ever build a
       protocol-specific fuzzer on top of PSF.

  Note that our tool paper does NOT report or claim any performance numbers; it
  only claims some particular functionality (i.e. "Functional" badge), namely,
  that it can be used to:
    - Learn the state machine model of the implementation.
    - Test the implementation by executing test input sequences.
    - Time the implementation on test input sequences to suggest timeout values
      for avoiding time-related non-determinism during Learning or Testing.
  However, to showcase this functionality, the user of our library will need to
  build a protocol-specific fuzzer (for some particular network protocol)
  following the instructions in PSF's README.

  We claim the "Reusable" badge because there is already evidence that our tool,
  which has been available since 2023, is reusable (as a framework) by others.
  Please refer to Section 5 of our accepted CAV'26 paper and to the relevant
  links in the "Used by" section of the README.


Requirements:

  * RAM: 2 GB
  * CPU cores: 1
  * Time (smoke test): 1 to 2 minutes
  * Time (full review): 1 to 2 minutes

external connectivity: YES

  Maven needs to download possibly missing components from its repository,
  Spotless also downloads some metadata from maven's central repository.

-------------------------------------------------------------------------------
**                                SMOKE TEST                                 **
-------------------------------------------------------------------------------

1. Load the image tarball, which will create a docker image named `psf:cav26`.
```
docker load < psf-image.tar.gz
```

Inspect that the image was created (look for `psf:cav26`)
```
docker image  ls -a
```

2. Create and run the container

This creates a container named `psf-cav26` and starts an interactive shell inside the container.
```
docker run -it --name psf-cav26 psf:cav26
```

3. Inside the container clean install ProtocolState-Fuzzer

The current working directory is at the root of PSF's repository. The image had already
installed PSF. In order to reinstall from scratch, use the following command.
```
mvn clean install
```

Maven will execute the necessary phases, before installing PSF in the local maven repository.

During the unit testing phase, the tests include some negative tests, so it's natural
that you should see some messages colored in red, but you can verify that the tests
have passed by the lines which read:
```
Results :

Tests run: 61, Failures: 0, Errors: 0, Skipped: 0
```

4. If needed, restart the same container after exiting

Check that the container exists (look for `psf-cav26`), if not go to step 2.
```
docker container ls -a
```

Start the same container
```
docker start --interactive psf-cav26
```

-------------------------------------------------------------------------------
**                               FULL REVIEW                                 **
-------------------------------------------------------------------------------

The main instructions on how to employ PSF as a basis to build a protocol-specific
model learning and state fuzzing tool appear in its `README.md`.

As mentioned, there exists strong evidence that PSF is REUSABLE because four
open-source and publicly available protocol-specific fuzzers (for Bluetooth
Low Energy, DTLS, EDHOC, and TCP) are already using it as their basis. Links
to these tools also exists at the end of PSF's README.

In addition, we are aware of two more tools that are currently being built on
top of our PSF library - but these are currently in private repositories.
