#!/bin/bash

mkdir -p /etc/webprotege
touch /etc/webprotege/webprotege.properties
echo data.directory=/srv/webprotege >> /etc/webprotege/webprotege.properties
echo application.version=4.0.0 >> /etc/webprotege/webprotege.properties
echo mongodb.host=$MONGO_DB_HOST >> /etc/webprotege/webprotege.properties
echo mongodb.port=$MONGO_DB_PORT >> /etc/webprotege/webprotege.properties
echo project.dormant.time=180000 >> /etc/webprotege/webprotege.properties

export CATALINA_OPTS="$CATALINA_OPTS -Xmx4g"

catalina.sh run