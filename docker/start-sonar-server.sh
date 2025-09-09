#!/bin/sh

echo "COPY build/libs/sonar-gosu-plugin-*.jar /opt/sonarqube/extensions/plugins"
cp ../build/libs/sonar-gosu-plugin-*.jar sonar-gosu-plugin.jar

echo "BUILD If custom Sonar server Docker image:"
docker build -f Dockerfile -t "if:sonar-server" .

echo "START:"
docker-compose up -d
