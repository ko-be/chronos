# Releasing Chronos at Yelp

## tl;dr
* Run `mvn release:prepare`, `mvn release:clean`
* Versions in pom.xml get updated, commited and new tag is pushed to github
* Travis runs maven on tag, pushes deb to bintray

## jdeb

This fork of Chronos uses the jdeb plugin to repackage the chronos jar as a deb package directly from maven.

Files used for the deb metadata are stored in `src/deb/*`, including the control file template.

Note: src/deb/bintray.json includes maven style placeholders, e.g. `${project.version}`. These are interpolated
when maven runs, with the resulting file ending in `target/classes/deb`, which is where travis is configured to look.

## Travis

Travis is configured to release only on tags starting `v`. It runs maven (which will generate a deb), then pushes the deb to bintray.
