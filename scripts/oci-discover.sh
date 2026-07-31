#!/bin/bash
set -uo pipefail

# ─────────────────────────────────────────────────────────────────────────────
# oci-a1-launch-retry.sh 의 CONFIG 칸에 넣을 OCID들을 조회해 주는 도우미.
#
# 사전: OCI CLI 설치 + 'oci setup config' 완료.
# 사용: bash scripts/oci-discover.sh
#       출력된 값들을 oci-a1-launch-retry.sh 의 CONFIG에 복사.
# ─────────────────────────────────────────────────────────────────────────────

command -v oci >/dev/null 2>&1 || { echo "❌ OCI CLI 미설치"; exit 1; }
[ -f "$HOME/.oci/config" ] || { echo "❌ ~/.oci/config 없음. 'oci setup config' 먼저"; exit 1; }

# 테넌시 OCID = 보통 루트 compartment. config 의 tenancy 값에서 읽음.
TENANCY="$(grep -m1 '^tenancy' "$HOME/.oci/config" | cut -d'=' -f2 | tr -d ' ')"
echo "════════════════════════════════════════════════════════"
echo "COMPARTMENT_ID (루트=테넌시):"
echo "  $TENANCY"
echo ""

echo "AVAILABILITY_DOMAIN 후보:"
oci iam availability-domain list --compartment-id "$TENANCY" \
  --query "data[].name" --raw-output 2>/dev/null | sed 's/^/  /'
echo ""

echo "SUBNET_ID 후보 (이름에 'subnet' 들어간 것 찾기):"
oci network subnet list --compartment-id "$TENANCY" \
  --query "data[].{name:\"display-name\", id:id}" --output table 2>/dev/null
echo ""

echo "IMAGE_ID (Ubuntu 22.04 Minimal aarch64, A1.Flex 호환):"
oci compute image list --compartment-id "$TENANCY" \
  --operating-system "Canonical Ubuntu" \
  --operating-system-version "22.04 Minimal aarch64" \
  --shape "VM.Standard.A1.Flex" \
  --query "data[0].{name:\"display-name\", id:id}" --output table 2>/dev/null
echo "════════════════════════════════════════════════════════"
echo "위 값들을 scripts/oci-a1-launch-retry.sh 의 CONFIG 칸에 채우세요."
