#!/bin/bash
set -e

# Usage: ./deploy.sh <version-tag> [--all] [pi-host]
# Default: copies only the JAR and restarts
# --all:   also copies Dockerfile, docker-compose, and deploy-pi.sh

VERSION=""
ALL=false
PI_HOST="ubuntu@stolat.local"

for arg in "$@"; do
    case "$arg" in
        --all) ALL=true ;;
        v*) VERSION="$arg" ;;
        *) PI_HOST="$arg" ;;
    esac
done

if [ -z "$VERSION" ]; then
    echo "Usage: ./deploy.sh <version-tag> [--all] [pi-host]"
    exit 1
fi

echo "=== Building $VERSION ==="
git fetch --tags
git checkout "$VERSION"
mvn clean package -Pproduction -DskipTests

JAR=$(ls target/stolat-*.jar)
echo "=== Deploying $JAR to $PI_HOST ==="
scp "$JAR" "$PI_HOST":~/stolat/

if [ "$ALL" = true ]; then
    echo "=== Copying all deployment files ==="
    scp Dockerfile "$PI_HOST":~/stolat/
    scp docker-compose.rpi.yml "$PI_HOST":~/stolat/docker-compose.yml
    scp deploy-pi.sh "$PI_HOST":~/stolat/
fi

echo "=== Restarting on Pi ==="
ssh "$PI_HOST" "chmod +x ~/stolat/deploy-pi.sh && ~/stolat/deploy-pi.sh"

echo "=== Done. Check http://stolat.local:8080 ==="
git checkout main
