FROM docker-mirror.sh.synyi.com/maven:3.6.0-jdk-11-slim AS build

#RUN apt-get update && \
#    apt-get install -y git mongodb

RUN cat /proc/sys/fs/inotify/max_user_watches

RUN env

#RUN echo fs.inotify.max_user_watches=524288 | tee -a /etc/sysctl.conf && \
#    sysctl -p

#RUN sysctl fs.inotify.max_user_watches=524288 && sysctl -p

COPY ./data/sources.list /etc/apt/sources.list

RUN apt-get update && \
    apt-get install -y git

COPY . /webprotege

WORKDIR /webprotege

#RUN mkdir -p /data/db \
#    && mongod --fork --syslog \
#    && mvn clean package

#RUN mvn clean install -Dmaven.test.skip=true -q

RUN mvn clean -q
RUN mvn package -Dmaven.test.skip=true >> mvn_package.log || tail -n 300 mvn_package.log

#RUN cd ./webprotege-shared-core && mvn install -Dmaven.test.skip=true -q
#RUN cd ./webprotege-shared && mvn install -Dmaven.test.skip=true -q
#RUN cd ./webprotege-client && mvn package -Dmaven.test.skip=true

FROM docker-mirror.sh.synyi.com/tomcat:8-jre11-slim

RUN rm -rf /usr/local/tomcat/webapps/* \
    && mkdir -p /srv/webprotege \
    && mkdir -p /usr/local/tomcat/webapps/ROOT

COPY ./data /srv/webprotege

WORKDIR /usr/local/tomcat/webapps/ROOT

COPY --from=build /webprotege/webprotege-cli/target/webprotege-cli-4.0.0.jar /webprotege-cli.jar
COPY --from=build /webprotege/webprotege-server/target/webprotege-server-4.0.0.war ./webprotege.war
RUN unzip webprotege.war \
    && rm webprotege.war

COPY ./run.sh /usr/local/
RUN chmod 777 /usr/local/run.sh

EXPOSE 8080

CMD ["/usr/local/run.sh"]