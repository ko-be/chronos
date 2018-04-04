# This makefile is only used to build chronos within Yelp's jenkins infrastructure.
#
# Maven is still the recommended route for building chronos.
mvn_var=$(shell which mvn3 || which mvn)

itest_trusty: docker-run-ubuntu-trusty

itest_xenial: docker-run-ubuntu-xenial

release:
	# create correctly versioned poms, tag and push. Don't bother running tests as travis/jenkins will run them
	$(mvn_var) -B -DskipTests release:prepare release:clean

# this calls the build container to actually build the .deb package for Chronos
docker-run-%: docker-build dist
	docker run -v $(CURDIR):/work:rw chronos_maven_builder bash -c "/work/deb-build.sh $*"

# this is the build container, used for building the chronos package
docker-build:
	docker build -f Dockerfile.deb-build -t "chronos_maven_builder" .

dist: clean
	mkdir dist

clean:
	rm -rf dist target
