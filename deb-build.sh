#! /usr/bin/env bash

# This script is designed to be run within the
# Dockerfile.deb-build docker image

# build targets are passed in as first parameter
# values for this can be found in github mesosphere/chronos-pkg/Makefile
TARGETS=$1

cd ~
wget https://github.com/mesosphere/chronos-pkg/archive/master.zip
unzip master.zip
# git clone https://github.com/mesosphere/chronos-pkg.git
cd chronos-pkg-master
rm -rf chronos
ln -s /work chronos
cd chronos
PROJECT_VER=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version\
    |grep '^[0-9].*' | tail -n 1|xargs echo -n`
# From chronos-pkg/Makefile - For release builds: PKG_REL=1
PKG_REL="1"
cd ..
make PKG_VER="$PROJECT_VER" PKG_REL="$PKG_REL" $TARGETS
cp *.deb chronos/dist

# Create bintray config for travis
# replaces placeholders with our vars
sed -e "s/\${project_version}/$PROJECT_VER/"\
    -e "s/\${build_timestamp}/`date -u +'%Y-%m-%d'`/"\
    -e "s/\${git_version}/$PROJECT_VER/"\
    chronos/src/deb/bintray.json > chronos/dist/bintray.json
