#!/bin/bash
set -e

cd ~/stolat

echo "=== Rebuilding and restarting ==="
docker builder prune -f
docker compose up -d --build

echo "=== Waiting for startup ==="
sleep 5
docker compose logs --tail 20 app

echo "=== Done. Check http://stolat.local:8080 ==="
