#!/bin/bash

# 데이터베이스 마이그레이션 통합 스크립트 (Dev → Prod)
# 사용법: ./scripts/migrate-db.sh

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 환경 변수
DEV_CONTAINER="db-dev"
PROD_CONTAINER="db"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

load_db_env() {
    local env_file="$1"
    local prefix="$2"
    if [ ! -f "${env_file}" ]; then
        echo -e "${RED}오류: ${env_file} 파일을 찾을 수 없습니다.${NC}"
        exit 1
    fi
    eval "${prefix}_DB=$(grep '^MYSQL_DATABASE=' "${env_file}" | cut -d'=' -f2-)"
    eval "${prefix}_USER=$(grep '^MYSQL_USER=' "${env_file}" | cut -d'=' -f2-)"
    eval "${prefix}_PASSWORD=$(grep '^MYSQL_PASSWORD=' "${env_file}" | cut -d'=' -f2-)"
}

load_db_env "${SCRIPT_DIR}/../db/.env.dev" "DEV"
load_db_env "${SCRIPT_DIR}/../db/.env.prod" "PROD"

DB_USER="${DEV_USER}"
DB_PASSWORD="${DEV_PASSWORD}"

BACKUP_DIR="backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo -e "${CYAN}"
echo "╔════════════════════════════════════════════╗"
echo "║   데이터베이스 마이그레이션 (Dev → Prod)   ║"
echo "╚════════════════════════════════════════════╝"
echo -e "${NC}"

# Step 1: 환경 확인
echo -e "${GREEN}[1/6] 환경 확인${NC}"

# Dev 컨테이너 확인
if docker ps | grep -q "$DEV_CONTAINER"; then
    echo -e "${GREEN}✓ Dev 컨테이너 실행 중${NC}"
else
    echo -e "${RED}✗ Dev 컨테이너가 실행되지 않았습니다.${NC}"
    echo -e "${YELLOW}Dev 컨테이너를 시작하시겠습니까? (y/n)${NC}"
    read -p "> " start_dev
    if [[ "$start_dev" =~ ^[Yy]$ ]]; then
        docker compose -f docker-compose.dev.yml up -d db-dev
        sleep 5
    else
        exit 1
    fi
fi

# Prod 컨테이너 확인
if docker ps | grep -q "$PROD_CONTAINER"; then
    echo -e "${GREEN}✓ Prod 컨테이너 실행 중${NC}"
else
    echo -e "${YELLOW}! Prod 컨테이너가 실행되지 않았습니다.${NC}"
    echo -e "${YELLOW}Prod 컨테이너를 시작하시겠습니까? (y/n)${NC}"
    read -p "> " start_prod
    if [[ "$start_prod" =~ ^[Yy]$ ]]; then
        docker compose -f docker-compose.prod.yml up -d db
        sleep 5
    else
        exit 1
    fi
fi

# 백업 디렉토리 생성
mkdir -p "$BACKUP_DIR/safety"

echo ""

# Step 2: Dev DB 테이블 목록 확인
echo -e "${GREEN}[2/6] Dev 데이터베이스 테이블 목록 조회${NC}"

DEV_TABLES=$(docker exec "$DEV_CONTAINER" mysql -u"$DB_USER" -p"$DB_PASSWORD" -D"$DEV_DB" -e "SHOW TABLES;" -s --skip-column-names 2>/dev/null)

if [ -z "$DEV_TABLES" ]; then
    echo -e "${RED}✗ Dev 데이터베이스에 테이블이 없습니다.${NC}"
    exit 1
fi

echo -e "${YELLOW}Dev DB 테이블 목록:${NC}"
i=1
TABLE_ARRAY=()
while IFS= read -r table; do
    # 각 테이블의 레코드 수 확인
    ROW_COUNT=$(docker exec "$DEV_CONTAINER" mysql -u"$DB_USER" -p"$DB_PASSWORD" -D"$DEV_DB" \
        -e "SELECT COUNT(*) FROM \`$table\`;" -s --skip-column-names 2>/dev/null || echo "0")
    echo "$i) $table (레코드: $ROW_COUNT개)"
    TABLE_ARRAY+=("$table")
    ((i++))
done <<< "$DEV_TABLES"

echo ""

# Step 3: 마이그레이션할 테이블 선택
echo -e "${GREEN}[3/6] 마이그레이션할 테이블 선택${NC}"
echo -e "${YELLOW}선택 방법:${NC}"
echo "  - 단일: 1"
echo "  - 여러 개: 1,2,3 또는 1 2 3"
echo "  - 범위: 1-5"
echo "  - 전체: all 또는 a"
echo ""
read -p "선택: " selection

# 선택된 테이블 처리
SELECTED_TABLES=()

if [[ "$selection" == "all" ]] || [[ "$selection" == "a" ]]; then
    SELECTED_TABLES=("${TABLE_ARRAY[@]}")
else
    selection=${selection//,/ }
    for item in $selection; do
        if [[ $item =~ ^([0-9]+)-([0-9]+)$ ]]; then
            start=${BASH_REMATCH[1]}
            end=${BASH_REMATCH[2]}
            for ((j=start; j<=end; j++)); do
                if [ $j -ge 1 ] && [ $j -le ${#TABLE_ARRAY[@]} ]; then
                    SELECTED_TABLES+=("${TABLE_ARRAY[$((j-1))]}")
                fi
            done
        elif [[ $item =~ ^[0-9]+$ ]]; then
            if [ $item -ge 1 ] && [ $item -le ${#TABLE_ARRAY[@]} ]; then
                SELECTED_TABLES+=("${TABLE_ARRAY[$((item-1))]}")
            fi
        fi
    done
fi

if [ ${#SELECTED_TABLES[@]} -eq 0 ]; then
    echo -e "${RED}✗ 유효한 테이블이 선택되지 않았습니다.${NC}"
    exit 1
fi

echo -e "\n${GREEN}선택된 테이블 (${#SELECTED_TABLES[@]}개):${NC}"
printf '  - %s\n' "${SELECTED_TABLES[@]}"

echo ""

# Step 4: 백업 옵션 선택
echo -e "${GREEN}[4/6] 백업 옵션 선택${NC}"
echo "1) 구조 + 데이터 (전체)"
echo "2) 데이터만 (구조 제외)"
echo "3) 구조만 (데이터 제외)"
read -p "선택 (1-3): " backup_option

DUMP_OPTIONS=""
BACKUP_TYPE="full"
case $backup_option in
    1) BACKUP_TYPE="full" ;;
    2) DUMP_OPTIONS="--no-create-info"; BACKUP_TYPE="data_only" ;;
    3) DUMP_OPTIONS="--no-data"; BACKUP_TYPE="schema_only" ;;
    *)
        echo -e "${YELLOW}! 기본값(전체)으로 진행합니다.${NC}"
        BACKUP_TYPE="full"
        ;;
esac

echo ""

# Step 5: Dev DB 백업
echo -e "${GREEN}[5/6] Dev 데이터베이스 백업${NC}"

BACKUP_FILE="${BACKUP_DIR}/dev_to_prod_${BACKUP_TYPE}_${TIMESTAMP}.sql"

echo -e "백업 파일: ${YELLOW}$BACKUP_FILE${NC}"
echo "백업 중..."

docker exec "$DEV_CONTAINER" mysqldump -u"$DB_USER" -p"$DB_PASSWORD" \
    $DUMP_OPTIONS \
    --single-transaction \
    --routines \
    --triggers \
    "$DEV_DB" "${SELECTED_TABLES[@]}" > "$BACKUP_FILE" 2>/dev/null

if [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
    FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo -e "${GREEN}✓ 백업 완료 (크기: $FILE_SIZE)${NC}"
else
    echo -e "${RED}✗ 백업 실패${NC}"
    exit 1
fi

echo ""

# Step 6: Prod DB 복원
echo -e "${GREEN}[6/6] Prod 데이터베이스 복원${NC}"

# 안전 백업 (현재 Prod 데이터)
echo -e "${YELLOW}복원 전 Prod DB를 안전하게 백업하시겠습니까? (권장) (y/n)${NC}"
read -p "> " do_safety_backup

if [[ "$do_safety_backup" =~ ^[Yy]$ ]]; then
    SAFETY_BACKUP="${BACKUP_DIR}/safety/prod_before_migration_${TIMESTAMP}.sql"
    echo "Prod DB 안전 백업 중..."

    # 선택된 테이블이 Prod에 존재하는지 확인
    PROD_EXISTING_TABLES=()
    for table in "${SELECTED_TABLES[@]}"; do
        if docker exec "$PROD_CONTAINER" mysql -u"$DB_USER" -p"$DB_PASSWORD" -D"$PROD_DB" \
            -e "SHOW TABLES LIKE '$table';" -s --skip-column-names 2>/dev/null | grep -q "$table"; then
            PROD_EXISTING_TABLES+=("$table")
        fi
    done

    if [ ${#PROD_EXISTING_TABLES[@]} -gt 0 ]; then
        docker exec "$PROD_CONTAINER" mysqldump -u"$DB_USER" -p"$DB_PASSWORD" \
            --single-transaction "$PROD_DB" "${PROD_EXISTING_TABLES[@]}" > "$SAFETY_BACKUP" 2>/dev/null || true

        if [ -f "$SAFETY_BACKUP" ] && [ -s "$SAFETY_BACKUP" ]; then
            SAFETY_SIZE=$(du -h "$SAFETY_BACKUP" | cut -f1)
            echo -e "${GREEN}✓ 안전 백업 완료: $SAFETY_BACKUP ($SAFETY_SIZE)${NC}"
        else
            echo -e "${YELLOW}! Prod DB에 기존 데이터가 없거나 백업 실패${NC}"
        fi
    else
        echo -e "${YELLOW}! Prod DB에 해당 테이블이 없습니다 (신규 생성됨)${NC}"
    fi
fi

# 최종 확인
echo ""
echo -e "${RED}========================================${NC}"
echo -e "${RED}       경고: 데이터 덮어쓰기 작업${NC}"
echo -e "${RED}========================================${NC}"
echo -e "${YELLOW}아래 테이블의 Prod 데이터가 Dev 데이터로 덮어써집니다:${NC}"
printf '  - %s\n' "${SELECTED_TABLES[@]}"
echo ""
echo -e "${RED}정말로 마이그레이션을 진행하시겠습니까?${NC}"
echo -e "${YELLOW}계속하려면 'yes'를 입력하세요.${NC}"
read -p "> " final_confirm

if [ "$final_confirm" != "yes" ]; then
    echo -e "${RED}마이그레이션을 취소합니다.${NC}"
    exit 0
fi

# 복원 실행
echo ""
echo "Prod DB로 복원 중..."

docker exec -i "$PROD_CONTAINER" mysql -u"$DB_USER" -p"$DB_PASSWORD" "$PROD_DB" < "$BACKUP_FILE" 2>&1 | \
    grep -v "mysql: \[Warning\]" || true

if [ $? -eq 0 ]; then
    echo -e "${GREEN}"
    echo "╔════════════════════════════════════════╗"
    echo "║     ✓ 마이그레이션 완료!               ║"
    echo "╚════════════════════════════════════════╝"
    echo -e "${NC}"

    echo -e "${BLUE}마이그레이션 정보:${NC}"
    echo -e "  - 테이블 수: ${YELLOW}${#SELECTED_TABLES[@]}개${NC}"
    echo -e "  - 백업 파일: ${YELLOW}$BACKUP_FILE${NC}"
    if [ -f "$SAFETY_BACKUP" ]; then
        echo -e "  - 안전 백업: ${YELLOW}$SAFETY_BACKUP${NC}"
        echo -e "  ${CYAN}(문제 발생 시 이 파일로 롤백 가능)${NC}"
    fi

    # 복원된 테이블 확인
    echo -e "\n${GREEN}복원된 테이블 확인:${NC}"
    for table in "${SELECTED_TABLES[@]}"; do
        ROW_COUNT=$(docker exec "$PROD_CONTAINER" mysql -u"$DB_USER" -p"$DB_PASSWORD" -D"$PROD_DB" \
            -e "SELECT COUNT(*) FROM \`$table\`;" -s --skip-column-names 2>/dev/null || echo "ERROR")
        if [ "$ROW_COUNT" != "ERROR" ]; then
            echo -e "  ${GREEN}✓${NC} $table: $ROW_COUNT개 레코드"
        else
            echo -e "  ${RED}✗${NC} $table: 확인 실패"
        fi
    done
else
    echo -e "${RED}"
    echo "╔════════════════════════════════════════╗"
    echo "║     ✗ 마이그레이션 실패                ║"
    echo "╚════════════════════════════════════════╝"
    echo -e "${NC}"

    if [ -f "$SAFETY_BACKUP" ]; then
        echo -e "${YELLOW}안전 백업으로 롤백하시겠습니까? (y/n)${NC}"
        read -p "> " do_rollback
        if [[ "$do_rollback" =~ ^[Yy]$ ]]; then
            echo "롤백 중..."
            docker exec -i "$PROD_CONTAINER" mysql -u"$DB_USER" -p"$DB_PASSWORD" "$PROD_DB" < "$SAFETY_BACKUP"
            echo -e "${GREEN}✓ 롤백 완료${NC}"
        fi
    fi
    exit 1
fi
