#! /usr/bin/env bash

# This script is designed to be run within the
# Dockerfile.deb-build docker image

# build targets are passed in as first parameter
# values for this can be found in github mesosphere/chronos-pkg/Makefile
TARGETS=$1

cd ~
git clone https://github.com/mesosphere/chronos-pkg.git
cd chronos-pkg
rm -rf chronos
ln -s /work chronos
cd chronos
PROJECT_VER=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version\
    |grep '^[0-9].*' | tail -n 1|xargs echo -n`
PKG_REL="0.1.`date -u +'%Y%m%d%H%M%S'`"
cd ..
make PKG_VER="$PROJECT_VER" PKG_REL="$PKG_REL" $TARGETS
cp *.deb chronos/dist

# Create bintray config for travis
# replaces placeholders with our vars
sed -e "s/\${project_version}/$MVN_VER/"\
    -e "s/\${build_timestamp}/`date -u +'%Y-%m-%d'`/"\
    chronos/src/deb/bintray.json > chronos/dist/bintray.json
