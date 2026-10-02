#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/../docker-compose.prod.yml"

# GHCR 인증 (패키지가 private인 경우 주석 해제 후 PAT 발급 필요)
# docker login ghcr.io -u tbongkim03 -p <GHCR_PAT>

echo "[deploy] 최신 이미지 pull..."
docker compose -f "${COMPOSE_FILE}" pull back front

echo "[deploy] 컨테이너 재시작..."
docker compose -f "${COMPOSE_FILE}" up -d --remove-orphans back front

echo "[deploy] 미사용 이미지 정리..."
docker image prune -f

echo "[deploy] 완료."
docker compose -f "${COMPOSE_FILE}" ps
