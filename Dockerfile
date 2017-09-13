FROM ubuntu:14.04

RUN echo "deb http://repos.mesosphere.io/ubuntu/ trusty main" > /etc/apt/sources.list.d/mesosphere.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv E56151BF && \
    apt-get update && \
    apt-get install -y maven \
    npm \
    default-jdk \
    mesos=1.0.1-2.0.93.ubuntu1404 \
    scala \
    curl && \
    apt-get clean all && \
    ln -s /usr/bin/nodejs /usr/bin/node

ADD . /chronos

WORKDIR /chronos

RUN mvn clean package

EXPOSE 8080

ENTRYPOINT ["bin/start-chronos.bash"]
