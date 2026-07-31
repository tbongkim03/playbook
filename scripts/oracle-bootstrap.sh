#!/bin/bash
set -euo pipefail

# ─────────────────────────────────────────────────────────────────────────────
# Oracle Cloud Always Free (ARM Ampere A1 / Ubuntu 22.04 aarch64) 개발서버 셋업
#
# 사용법: 새로 만든 인스턴스에 SSH로 접속한 뒤 root 권한으로 1회 실행
#   curl -fsSL <이 스크립트 raw URL> | sudo bash
#   또는 파일을 올린 뒤:  sudo bash oracle-bootstrap.sh
#
# 수행 내용:
#   1) 시스템 패키지 업데이트
#   2) Docker Engine + compose 플러그인 설치
#   3) Oracle Ubuntu 이미지의 기본 iptables 방화벽에 80/8080 포트 허용
#   4) 현재 사용자(ubuntu)를 docker 그룹에 추가
#
# 코드 배포(git clone / .env 업로드 / compose up)는 별도 단계 — 아래 안내 참조.
# ─────────────────────────────────────────────────────────────────────────────

TARGET_USER="${SUDO_USER:-ubuntu}"

echo "[1/4] 시스템 패키지 업데이트..."
apt-get update -y
apt-get upgrade -y

echo "[2/4] Docker Engine + compose 플러그인 설치..."
if ! command -v docker >/dev/null 2>&1; then
  apt-get install -y ca-certificates curl gnupg
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg
  echo \
    "deb [arch=arm64 signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
    $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    > /etc/apt/sources.list.d/docker.list
  apt-get update -y
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
else
  echo "  → Docker가 이미 설치되어 있어 건너뜁니다."
fi
systemctl enable --now docker

echo "[3/4] iptables 방화벽에 80 / 8080 포트 허용 (Oracle Ubuntu 기본 차단 해제)..."
# Oracle Ubuntu 이미지는 INPUT 체인에 REJECT 룰이 있어 publish 포트가 막힙니다.
# REJECT 룰 앞(라인 6 부근)에 ACCEPT 룰을 삽입합니다.
for PORT in 80 8080; do
  if ! iptables -C INPUT -p tcp --dport "$PORT" -j ACCEPT 2>/dev/null; then
    iptables -I INPUT 6 -p tcp --dport "$PORT" -j ACCEPT
    echo "  → ${PORT}/tcp 허용 추가"
  fi
done
# 재부팅 후에도 유지
apt-get install -y netfilter-persistent iptables-persistent
netfilter-persistent save

echo "[4/4] '${TARGET_USER}' 사용자를 docker 그룹에 추가..."
usermod -aG docker "$TARGET_USER" || true

echo ""
echo "✅ 셋업 완료. 'docker' 그룹 적용을 위해 SSH 재접속 후 'docker ps'로 확인하세요."
echo ""
echo "다음 단계 (코드 배포):"
echo "  1) 코드 가져오기 — 둘 중 하나"
echo "       A) git clone git@github.com:tbongkim03/playbook.git   (배포키 등록 필요)"
echo "       B) 로컬에서 rsync로 업로드 (아래 안내 참조)"
echo "  2) .env 파일 업로드  (gitignore라 clone에 안 따라옴):"
echo "       back/.env.dev, db/.env.dev 를 로컬에서 scp 로 전송"
echo "  3) 빌드 & 기동:"
echo "       cd playbook && docker compose -f docker-compose.dev.yml up -d --build"
