#!/usr/bin/env bash

set -e

VERSION=${1:-1.0.0}
REGISTRY=${REGISTRY:-"192.168.199.107:8812"}
PROJECT=${PROJECT:-"mini-devops"}

SERVICES=(
 "devops-gateway"
  "devops-auth-service"
  "devops-work-service"
  "devops-code-service"
  "devops-release-service"
  "devops-frontend"
)

for SERVICE in "${SERVICES[@]}"; do
  LOCAL_IMAGE="mini-devops/${SERVICE}:${VERSION}"
  REMOTE_IMAGE="${REGISTRY}/${PROJECT}/${SERVICE}:${VERSION}"

  echo "Tag image: ${LOCAL_IMAGE} -> ${REMOTE_IMAGE}"
  docker tag "${LOCAL_IMAGE}" "${REMOTE_IMAGE}"

  echo "Push image: ${REMOTE_IMAGE}"
  docker push "${REMOTE_IMAGE}"
done

echo "All images pushed successfully."
