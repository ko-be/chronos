# Releasing Chronos at Yelp

## tl;dr
* run `make release`, which in turn runs `mvn release:prepare release:perform` in a docker container
* Versions in pom.xml get updated, commited and new tag is pushed to github
* Travis runs maven on tag, pushes deb to bintray
* Also mirrored internally for jenkins build

## chronos-pkg
This fork of chronos pulls the chronos-pkg project down into a docker container
and builds a deb package from the currently checked out version of chronos.

This mechanism allows us to build the project easily whenever a new maven release is pushed.  

The deb-build.sh script is run inside the Dockerfile.deb-build container, packaging
the deb and templating our bintray.json enabling us to push the artifacts up to our
public repository.

## Travis

Travis is configured to release only on tags starting `v`. It runs `make itest_trusty`, 
then pushes the deb to bintray, using the json templated in `deb-build.sh`

