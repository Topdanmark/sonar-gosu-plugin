#!/bin/sh

echo "COPY SONAR GOSU PLUGIN"
cp ../build/libs/sonar-gosu-plugin-*.jar sonar-gosu-plugin.jar

echo "BUILDING CUSTOM SONAR SERVER DOCKER IMAGE:"
docker build -f Dockerfile -t "if:sonar-server" .

echo "STARTING:"
docker-compose up -d
