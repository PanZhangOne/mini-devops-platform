#!/usr/bin/env bash

set -e

VERSION=${1:-1.0.0}
NACOS_SERVER_ADDR=${NACOS_SERVER_ADDR:-"192.168.199.107:8848"}
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"dev"}

docker network inspect mini-devops-net >/dev/null 2>&1 || docker network create mini-devops-net

docker rm -f devops-auth-service || true
docker rm -f devops-work-service || true
docker rm -f devops-code-service || true
docker rm -f devops-release-service || true
docker rm -f devops-gateway || true
docker rm -f devops-frontend || true

docker run -d \
  --name devops-auth-service \
  --network mini-devops-net \
  -p 9020:9020 \
  -e SERVER_PORT=9020 \
  -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
  -e NACOS_SERVER_ADDR=${NACOS_SERVER_ADDR} \
  mini-devops/devops-auth-service:${VERSION}

docker run -d \
  --name devops-work-service \
  --network mini-devops-net \
  -p 9001:9001 \
  -e SERVER_PORT=9001 \
  -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
  -e NACOS_SERVER_ADDR=${NACOS_SERVER_ADDR} \
  mini-devops/devops-work-service:${VERSION}

docker run -d \
  --name devops-code-service \
  --network mini-devops-net \
  -p 9010:9010 \
  -e SERVER_PORT=9010 \
  -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
  -e NACOS_SERVER_ADDR=${NACOS_SERVER_ADDR} \
  mini-devops/devops-code-service:${VERSION}

docker run -d \
  --name devops-release-service \
  --network mini-devops-net \
  -p 9030:9030 \
  -e SERVER_PORT=9030 \
  -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
  -e NACOS_SERVER_ADDR=${NACOS_SERVER_ADDR} \
  mini-devops/devops-release-service:${VERSION}


docker run -d \
  --name devops-gateway \
  --network mini-devops-net \
  -p 9000:9000 \
  -e SERVER_PORT=9000 \
  -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
  -e NACOS_SERVER_ADDR=${NACOS_SERVER_ADDR} \
  mini-devops/devops-gateway:${VERSION}


docker run -d \
  --name devops-frontend \
  --network mini-devops-net \
  -p 80:80 \
  mini-devops/devops-frontend:${VERSION}

docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Ports}}"