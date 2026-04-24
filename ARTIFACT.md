## Prepare the artifact directory (from the PSF root)

1. Build the docker image
```bash
docker build -t psf:cav26 .
```

2. Optionally create and inspect the container of the image
```bash
# this is optional to verify everything is fine
docker run -it --name psf-cav26 psf:cav26
```

3. Create the image tarball for the artifact
```bash
# the image is being saved, not the container
docker save psf:cav26 | gzip > artifact/psf-cav26-image.tar.gz
```

4. Modify README if needed ([online template](https://raw.githubusercontent.com/ondrik/cav26-artifact-evaluation/refs/heads/master/templates/README-cav26.txt))
```bash
vim artifact/README.md
```

5. Zip the artifact directory
```bash
zip -r artifact.zip artifact
```

## Use the artifact directory

1. Remove any previous containers and images (to avoid conflicts)
```bash
docker container rm psf-cav26
docker image rm psf:cav26
```

2. Load the image tarball
```bash
cd artifact
docker load < psf-image.tar.gz
# inspect that image was created (look for psf:cav26)
docker image  ls -a
```

3. Create and run the container
```bash
# this creates a container named `psf-cav26`
# and starts an interactive shell inside the container
docker run -it --name psf-cav26 psf:cav26
```

4. Restart the container after exiting
```bash
# verify that the container exists (look for psf-cav26)
docker container ls -a
# start the same container
docker start --interactive psf-cav26
```
