itest_%: dist
	mkdir dist
	mvn package
	cp target/*.deb dist/

clean:
	mvn clean
	rm -rf dist
