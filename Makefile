# This makefile is only used to build chronos within Yelp's jenkins infrastructure.
#
# Maven is still the recommended route for building chronos.
#

itest_%:
	[ -d dist ] || mkdir dist
	# maven command may be mvn or mvn3 depending on distro, if one fails try the other
	mvn3 package || mvn package
	cp target/*.deb dist/

clean:
	mvn clean
	rm -rf dist
