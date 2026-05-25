#!/usr/bin/env bash

set -e

VERSION=${1:-1.0.0}

echo "Build Maven packages..."

mvn clean package -DskipTests

echo "Build Docker images, version${VERSION}"

docker build \
  -t mini-devops/devops-gateway:${VERSION} \
  -t 192.168.199.107:8812/mini-devops/devops-gateway:${VERSION} \
  --build-arg JAR_FILE=devops-gateway/target/devops-gateway-1.0.0-SNAPSHOT.jar \
  .

docker build \
  -t mini-devops/devops-auth-service:${VERSION} \
  -t 192.168.199.107:8812/mini-devops/devops-auth-service:${VERSION} \
  --build-arg JAR_FILE=devops-auth-service/target/devops-auth-service-1.0.0-SNAPSHOT.jar \
  .

docker build \
  -t mini-devops/devops-work-service:${VERSION} \
  -t 192.168.199.107:8812/mini-devops/devops-work-service:${VERSION} \
  --build-arg JAR_FILE=devops-work-service/target/devops-work-service-1.0.0-SNAPSHOT.jar \
  .

docker build \
  -t mini-devops/devops-code-service:${VERSION} \
  -t 192.168.199.107:8812/mini-devops/devops-code-service:${VERSION} \
  --build-arg JAR_FILE=devops-code-service/target/devops-code-service-1.0.0-SNAPSHOT.jar \
  .

docker build \
  -t mini-devops/devops-release-service:${VERSION} \
  -t 192.168.199.107:8812/mini-devops/devops-release-service:${VERSION} \
  --build-arg JAR_FILE=devops-release-service/target/devops-release-service-1.0.0-SNAPSHOT.jar \
  .

docker build \
  -t mini-devops/devops-frontend:${VERSION} \
  -t 192.168.199.107:8812/mini-devops/devops-frontend:${VERSION} \
  ./devops-frontend

echo "Done."
