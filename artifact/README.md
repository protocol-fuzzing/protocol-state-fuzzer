CAV 2026 Artifact
=======================================
Paper title: PSF: A Generic and Extensible Framework for Protocol State Fuzzing

Claimed badges: Available + Functional + Reusable [remove which do not apply; note that Reusable subsumes Functional]

Justification for the badges: [no need to justify Available -- just provide the DOI link in HotCRP]

  * Functional: [give reasons why you believe that the Functional badge should
    be awarded (if applied for Functional or Reusable); example:  The artifact
    replicates most of the results in the paper (see below for details).  It
    compiles Tool and executes the benchmarks on it and the other tools.  We
    validate the correctness of the outputs of Tool by cross-comparison with
    the results of the other tools.  The source code of Tool is included in the
    artifact.]

    The artifact contains the source code of ProtocolState-Fuzzer, which can be installed
    successfully in the local maven repository after passing all the required maven phases.
    The most notable of them are:
        - compilation
        - unit testing
        - documentation creation
        - dependency analysis
        - formatting checks via spotless
        - static analysis checks via spotbugs
        - installation

Requirements:

  * RAM: 2 GB
  * CPU cores: 1
  * Time (smoke test): 1 to 2 minutes
  * Time (full review): 1 to 2 minutes

external connectivity: YES

  Maven needs to download possibly missing components from the repository,
  Spotless also downloads some metadata from maven central repository.

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

This creates a container named `psf-cav26` and starts an interactive shell inside the container
```
docker run -it --name psf-cav26 psf:cav26
```

3. Inside the container install ProtocolState-Fuzzer

The current working directory is at the root of the repository. So the installation
will perform all the necessary phases, before installing it in the local maven repository.
```
mvn clean install
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

TODO
