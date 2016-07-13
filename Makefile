# This makefile is only used to build chronos within Yelp's jenkins infrastructure.
#
# Maven is still the recommended route for building chronos.

itest_trusty: deb

itest_lucid: deb

deb: maven-build
	[ -d dist ] || mkdir dist
	cp target/*.deb dist/

release: docker-build
	# create correctly versioned poms, tag and push. Don't bother running tests as travis/jenkins will run them
	docker run -v $(CURDIR):/work:rw chronos_maven_builder bash -c "cd /work && mvn -B -DskipTests release:prepare release:clean"

maven-build: docker-build
	docker run -v $(CURDIR):/work:rw chronos_maven_builder bash -c "cd /work && mvn package"

docker-build:
	docker build -f Dockerfile.deb-build -t "chronos_maven_builder" .

clean:
	rm -rf dist
