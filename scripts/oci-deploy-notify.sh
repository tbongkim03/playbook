#!/bin/bash
set -uo pipefail

# ─────────────────────────────────────────────────────────────────────────────
# oci-a1-launch-retry.sh 를 백그라운드로 돌리다가, 인스턴스 생성에 "성공"하면
# tbongkim03@gmail.com 으로 알림 메일을 보내는 래퍼.
#
# 원본 스크립트는 성공 시 화면 출력 후 exit 0 만 하므로, 자리를 비운 사이에도
# 알 수 있도록 이 래퍼가 메일을 대신 발송한다.
#
# 메일 발송은 시스템에 mail/sendmail 이 없어 Gmail SMTP + curl 로 처리한다.
#
# 사전 준비 (Gmail 앱 비밀번호):
#   1) 발신 Gmail 계정에 2단계 인증 켜기
#   2) https://myaccount.google.com/apppasswords 에서 16자리 앱 비밀번호 발급
#   3) 아래 환경변수를 채워서 실행 (셸 히스토리에 안 남기려면 .env 파일 권장):
#        export SMTP_USER="보내는주소@gmail.com"
#        export SMTP_PASS="앱비밀번호16자리"   # 공백 없이
#        export MAIL_TO="tbongkim03@gmail.com"  # 기본값이라 생략 가능
#
# 사용:
#   nohup bash scripts/oci-deploy-notify.sh > ~/oci-deploy.log 2>&1 &
#   → 백그라운드로 용량 풀릴 때까지 재시도. 성공하면 메일 1통 발송 후 종료.
# ─────────────────────────────────────────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LAUNCH_SCRIPT="$SCRIPT_DIR/oci-a1-launch-retry.sh"

# .env 파일이 있으면 SMTP 자격증명을 거기서 읽는다 (셸 히스토리 노출 방지)
[ -f "$SCRIPT_DIR/.oci-notify.env" ] && source "$SCRIPT_DIR/.oci-notify.env"

MAIL_TO="${MAIL_TO:-tbongkim03@gmail.com}"
SMTP_USER="${SMTP_USER:-}"
SMTP_PASS="${SMTP_PASS:-}"
SMTP_URL="${SMTP_URL:-smtps://smtp.gmail.com:465}"

fail() { echo "❌ $1" >&2; exit 1; }

[ -f "$LAUNCH_SCRIPT" ] || fail "런처 스크립트 없음: $LAUNCH_SCRIPT"

send_mail() {
  local subject="$1" body="$2"

  if [ -z "$SMTP_USER" ] || [ -z "$SMTP_PASS" ]; then
    echo "⚠️ SMTP_USER/SMTP_PASS 미설정 → 메일 발송 생략. 본문만 출력:" >&2
    echo "── $subject ──" >&2
    echo "$body" >&2
    return 1
  fi

  local date_hdr
  date_hdr="$(date -R)"

  # RFC822 메시지 구성 (UTF-8 한글 제목/본문)
  local msg
  msg=$(cat <<EOF
From: OCI Deploy Bot <${SMTP_USER}>
To: ${MAIL_TO}
Subject: =?UTF-8?B?$(printf '%s' "$subject" | base64 -w0)?=
Date: ${date_hdr}
MIME-Version: 1.0
Content-Type: text/plain; charset=UTF-8
Content-Transfer-Encoding: 8bit

${body}
EOF
)

  if curl -sS --ssl-reqd \
      --url "$SMTP_URL" \
      --user "${SMTP_USER}:${SMTP_PASS}" \
      --mail-from "${SMTP_USER}" \
      --mail-rcpt "${MAIL_TO}" \
      -T <(printf '%s' "$msg"); then
    echo "📧 메일 발송 완료 → ${MAIL_TO}"
    return 0
  else
    echo "⚠️ 메일 발송 실패 (curl rc=$?)" >&2
    return 1
  fi
}

echo "▶ OCI 배포 감시 시작 — 성공 시 ${MAIL_TO} 로 메일 발송"
echo "  런처: $LAUNCH_SCRIPT"
echo ""

# 원본 런처 실행. 성공(exit 0)하면 출력에서 인스턴스 OCID 추출 후 메일.
OUTPUT="$(bash "$LAUNCH_SCRIPT" 2>&1)"
rc=$?
echo "$OUTPUT"

if [ $rc -eq 0 ]; then
  INSTANCE_ID="$(echo "$OUTPUT" | grep -i '"id"' | head -1 | sed -E 's/.*"id"[^"]*"([^"]+)".*/\1/')"
  TS="$(date '+%Y-%m-%d %H:%M:%S %Z')"
  SUBJECT="✅ OCI A1.Flex 인스턴스 생성 성공"
  BODY="Oracle Cloud A1.Flex(ARM 무료) 인스턴스가 생성되었습니다.

시각: ${TS}
인스턴스 OCID: ${INSTANCE_ID:-(출력에서 추출 실패, 로그 확인)}

다음 단계:
  1) 콘솔에서 공인 IP(Reserved) 부여
  2) scripts/oracle-bootstrap.sh 실행

── 런처 출력 일부 ──
$(echo "$OUTPUT" | tail -15)
"
  send_mail "$SUBJECT" "$BODY"
  exit 0
else
  echo "⚠️ 런처가 실패(rc=$rc)로 종료됨 — 설정/인증/한도 오류일 수 있음. 메일 발송 안 함." >&2
  exit $rc
fi
