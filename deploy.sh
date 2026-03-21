#!/bin/bash
set -e

VERSION=${1:?Usage: ./deploy.sh <version-tag>}
PI_HOST=${2:-ubuntu@stolat.local}

echo "=== Building $VERSION ==="
git fetch --tags
git checkout "$VERSION"
mvn clean package -Pproduction -DskipTests

JAR=$(ls target/stolat-*.jar)
echo "=== Deploying $JAR to $PI_HOST ==="
scp "$JAR" "$PI_HOST":~/stolat/
scp Dockerfile "$PI_HOST":~/stolat/
scp docker-compose.rpi.yml "$PI_HOST":~/stolat/docker-compose.yml
scp deploy-pi.sh "$PI_HOST":~/stolat/

echo "=== Restarting on Pi ==="
ssh "$PI_HOST" "chmod +x ~/stolat/deploy-pi.sh && ~/stolat/deploy-pi.sh"

echo "=== Done. Check http://stolat.local:8080 ==="
git checkout main
