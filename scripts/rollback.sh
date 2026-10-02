#!/bin/bash
set -euo pipefail

if [ -z "${1:-}" ]; then
  echo "사용법: $0 <sha-태그>"
  echo "예시:   $0 sha-a1b2c3d"
  echo ""
  echo "사용 가능한 태그 확인:"
  echo "  ghcr.io/tbongkim03/playbook-back"
  echo "  ghcr.io/tbongkim03/playbook-front"
  exit 1
fi

TAG="$1"
BACK_IMAGE="ghcr.io/tbongkim03/playbook-back:${TAG}"
FRONT_IMAGE="ghcr.io/tbongkim03/playbook-front:${TAG}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/../docker-compose.prod.yml"

echo "[rollback] 태그: ${TAG}"
echo "[rollback] 이미지 pull..."
docker pull "${BACK_IMAGE}"
docker pull "${FRONT_IMAGE}"

echo "[rollback] 해당 태그로 컨테이너 교체..."
BACK_IMAGE="${BACK_IMAGE}" FRONT_IMAGE="${FRONT_IMAGE}" \
  docker compose -f "${COMPOSE_FILE}" up -d --remove-orphans back front

echo "[rollback] 완료."
docker compose -f "${COMPOSE_FILE}" ps
