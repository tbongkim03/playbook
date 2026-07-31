#!/bin/bash
set -uo pipefail

# ─────────────────────────────────────────────────────────────────────────────
# Oracle Cloud A1.Flex (ARM Always Free) 인스턴스 "용량 풀릴 때까지" 자동 재시도 런처
#
# 배경: ARM 무료 인스턴스는 만석이 잦아 "Out of host capacity"가 계속 뜬다.
#       용량은 몇 분~몇 시간 단위로 잠깐씩 풀리므로, 성공할 때까지 주기적으로
#       launch를 재시도한다. 콘솔에서 손으로 누르면 "Too many requests(429)"가 나니
#       적절한 backoff를 두고 CLI로 돌린다.
#
# 사전 준비:
#   1) OCI CLI 설치:   bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"
#   2) API 키 설정:     oci setup config   (콘솔 프로필 → API Keys 에 public key 등록)
#   3) 아래 CONFIG 칸의 OCID들을 채운다 (조회법은 scripts/oci-discover.sh 참고)
#
# 사용:
#   bash scripts/oci-a1-launch-retry.sh
#   → 성공하면 인스턴스 OCID를 출력하고 종료. 실패(용량부족)면 계속 재시도.
# ─────────────────────────────────────────────────────────────────────────────

# ========================= CONFIG (여기를 채우세요) ==========================
COMPARTMENT_ID="ocid1.tenancy.oc1..aaaaaaaa7ghjkl75sql3rawz2lxoq57gr3e5k3rxpd3yvmkyg6o7mbxqwiqq"
AVAILABILITY_DOMAIN="jGAX:AP-OSAKA-1-AD-1"
SUBNET_ID="ocid1.subnet.oc1.ap-osaka-1.aaaaaaaaozsocmhyq6zpmahjbvosqoosc7d2wp2yanroucxa37leqpve5ida"   # IGW+route+SL(22/80/8080) 확인된 public subnet
IMAGE_ID="ocid1.image.oc1.ap-osaka-1.aaaaaaaau6mqget7kbdovncwrbqshxn46h2vco4ok6p2wrqwrlmxfjdbrcjq"     # Canonical-Ubuntu-22.04-Minimal-aarch64-2026.04.30-1
SSH_PUBKEY_PATH="$HOME/.ssh/id_ed25519.pub"

DISPLAY_NAME="tbongkim03-dev"
SHAPE="VM.Standard.A1.Flex"
OCPUS=2                     # 2면 4보다 훨씬 잘 잡힘. 잡힌 뒤 콘솔에서 올릴 수 있음
MEMORY_GB=12               # 통상 OCPU x 6
BOOT_VOLUME_GB=100
ASSIGN_PUBLIC_IP="true"    # 서브넷 public 확인됨 → 생성 시 ephemeral 공인 IP 자동 부여
                           # (도메인 붙일 땐 나중에 콘솔에서 reserved IP로 전환 가능)

# 재시도 간격(초). 429 방지를 위해 너무 짧게 하지 말 것 (60초 권장, 지터 추가됨)
RETRY_INTERVAL=60
# ============================================================================

fail() { echo "❌ $1" >&2; exit 1; }

command -v oci >/dev/null 2>&1 || fail "OCI CLI 미설치. 스크립트 상단 사전준비 1) 참고"
[ -f "$HOME/.oci/config" ] || fail "~/.oci/config 없음. 'oci setup config' 먼저 실행"
[ -n "$COMPARTMENT_ID" ] && [ -n "$AVAILABILITY_DOMAIN" ] && [ -n "$SUBNET_ID" ] && [ -n "$IMAGE_ID" ] \
  || fail "CONFIG 칸의 OCID(COMPARTMENT/AD/SUBNET/IMAGE)를 모두 채우세요. (조회: scripts/oci-discover.sh)"
[ -f "$SSH_PUBKEY_PATH" ] || fail "SSH 공개키 없음: $SSH_PUBKEY_PATH"

SSH_PUBKEY_CONTENT="$(cat "$SSH_PUBKEY_PATH")"

echo "▶ A1.Flex 자동 재시도 시작 — ${OCPUS} OCPU / ${MEMORY_GB}GB / ${DISPLAY_NAME}"
echo "  AD=${AVAILABILITY_DOMAIN}"
echo "  ${RETRY_INTERVAL}초(±지터) 간격으로 용량 풀릴 때까지 시도. 중단은 Ctrl+C."
echo ""

attempt=0
while true; do
  attempt=$((attempt + 1))
  ts="$(date '+%H:%M:%S')"

  OUTPUT="$(oci compute instance launch \
    --compartment-id "$COMPARTMENT_ID" \
    --availability-domain "$AVAILABILITY_DOMAIN" \
    --display-name "$DISPLAY_NAME" \
    --shape "$SHAPE" \
    --shape-config "{\"ocpus\": ${OCPUS}, \"memoryInGBs\": ${MEMORY_GB}}" \
    --image-id "$IMAGE_ID" \
    --subnet-id "$SUBNET_ID" \
    --assign-public-ip "$ASSIGN_PUBLIC_IP" \
    --boot-volume-size-in-gbs "$BOOT_VOLUME_GB" \
    --metadata "{\"ssh_authorized_keys\": \"${SSH_PUBKEY_CONTENT}\"}" \
    --wait-for-state RUNNING \
    2>&1)"
  rc=$?

  if [ $rc -eq 0 ]; then
    echo ""
    echo "✅ [$ts] 인스턴스 생성 성공! (시도 ${attempt}회)"
    echo "$OUTPUT" | grep -i '"id"' | head -1
    echo ""
    echo "다음: 콘솔에서 공인 IP(Reserved) 부여 → scripts/oracle-bootstrap.sh 실행"
    exit 0
  fi

  # 명백한 설정/인증/한도 오류만 즉시 중단 — 이건 재시도해도 절대 안 됨
  # 주의: 맨숫자 400은 opc-request-id 같은 임의 문자열에 우연히 매칭되어
  #       용량부족(InternalError 500)을 오분류시키므로 쓰지 말 것.
  #       HTTP status는 "status": 400 형태로만 정확히 매칭한다. (429는 재시도 대상이라 제외)
  if echo "$OUTPUT" | grep -qiE "NotAuthenticated|NotAuthorized|Authorization failed|could not be found|InvalidParameter|\"status\": *40[0-9]|LimitExceeded|QuotaExceeded"; then
    echo ""
    echo "⚠️ [$ts] 재시도 불가한 설정/인증/한도 오류 — 확인 필요:"
    echo "$OUTPUT"
    exit 1
  fi

  # 그 외 모든 실패(용량부족 InternalError/TooManyRequests, 네트워크 타임아웃,
  # 일시적 5xx 등)는 전부 재시도 대상.
  reason="$(echo "$OUTPUT" | grep -oiE "Out of host capacity|TooManyRequests|InternalError|timed out|RequestException|ConnectionError" | head -1)"
  jitter=$((RANDOM % 20))
  wait=$((RETRY_INTERVAL + jitter))
  echo "[$ts] 시도 ${attempt}: ${reason:-일시적오류} → ${wait}초 후 재시도"
  sleep "$wait"
done
