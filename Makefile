# This makefile is only used to build chronos within Yelp's jenkins infrastructure.
#
# Maven is still the recommended route for building chronos.
#
itest_%:
	mkdir dist
	mvn package
	cp target/*.deb dist/

clean:
	mvn clean
	rm -rf dist
