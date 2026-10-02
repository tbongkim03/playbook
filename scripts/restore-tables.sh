#!/bin/bash

# 테이블 선택 복원 스크립트
# 사용법: ./scripts/restore-tables.sh [dev|prod] [백업파일]

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 환경 설정
ENV=${1:-prod}
BACKUP_FILE=$2
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

load_db_env() {
    local env_file="$1"
    if [ ! -f "${env_file}" ]; then
        echo -e "${RED}오류: ${env_file} 파일을 찾을 수 없습니다.${NC}"
        exit 1
    fi
    DB_NAME=$(grep '^MYSQL_DATABASE=' "${env_file}" | cut -d'=' -f2-)
    DB_USER=$(grep '^MYSQL_USER=' "${env_file}" | cut -d'=' -f2-)
    DB_PASSWORD=$(grep '^MYSQL_PASSWORD=' "${env_file}" | cut -d'=' -f2-)
}

if [ "$ENV" = "dev" ]; then
    CONTAINER_NAME="db-dev"
    load_db_env "${SCRIPT_DIR}/../db/.env.dev"
elif [ "$ENV" = "prod" ]; then
    CONTAINER_NAME="db"
    load_db_env "${SCRIPT_DIR}/../db/.env.prod"
else
    echo -e "${RED}오류: 잘못된 환경입니다. 'dev' 또는 'prod'를 선택하세요.${NC}"
    echo "사용법: ./scripts/restore-tables.sh [dev|prod] [백업파일]"
    exit 1
fi

echo -e "${GREEN}=== 데이터베이스 테이블 복원 스크립트 ===${NC}"
echo -e "환경: ${YELLOW}$ENV${NC}"
echo -e "컨테이너: ${YELLOW}$CONTAINER_NAME${NC}"
echo -e "데이터베이스: ${YELLOW}$DB_NAME${NC}"
echo ""

# 컨테이너 실행 여부 확인
if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo -e "${RED}오류: $CONTAINER_NAME 컨테이너가 실행 중이 아닙니다.${NC}"
    exit 1
fi

# 백업 파일 선택
if [ -z "$BACKUP_FILE" ]; then
    echo -e "${GREEN}사용 가능한 백업 파일 목록:${NC}"

    if [ ! -d "backups" ]; then
        echo -e "${RED}오류: backups 디렉토리가 없습니다.${NC}"
        exit 1
    fi

    BACKUP_FILES=(backups/*.sql)

    if [ ! -e "${BACKUP_FILES[0]}" ]; then
        echo -e "${RED}오류: 백업 파일이 없습니다.${NC}"
        exit 1
    fi

    i=1
    for file in "${BACKUP_FILES[@]}"; do
        FILE_SIZE=$(du -h "$file" | cut -f1)
        FILE_DATE=$(stat -c %y "$file" | cut -d' ' -f1,2 | cut -d'.' -f1)
        echo "$i) $(basename "$file") - $FILE_SIZE (생성: $FILE_DATE)"
        ((i++))
    done

    echo ""
    read -p "복원할 백업 파일 번호: " file_num

    if ! [[ "$file_num" =~ ^[0-9]+$ ]] || [ "$file_num" -lt 1 ] || [ "$file_num" -gt ${#BACKUP_FILES[@]} ]; then
        echo -e "${RED}오류: 잘못된 선택입니다.${NC}"
        exit 1
    fi

    BACKUP_FILE="${BACKUP_FILES[$((file_num-1))]}"
fi

# 백업 파일 존재 확인
if [ ! -f "$BACKUP_FILE" ]; then
    echo -e "${RED}오류: 백업 파일 '$BACKUP_FILE'을 찾을 수 없습니다.${NC}"
    exit 1
fi

echo -e "\n${BLUE}선택된 백업 파일: ${YELLOW}$BACKUP_FILE${NC}"
FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
echo -e "파일 크기: ${YELLOW}$FILE_SIZE${NC}"

# 백업 파일에서 테이블 목록 추출
echo -e "\n${GREEN}백업 파일에 포함된 테이블 분석 중...${NC}"
TABLES_IN_BACKUP=$(grep -E "^-- Table structure for table \`.*\`|^INSERT INTO \`.*\`" "$BACKUP_FILE" | \
    sed -E "s/^-- Table structure for table \`(.*)\`/\1/; s/^INSERT INTO \`(.*)\`.*/\1/" | \
    sort -u)

if [ -z "$TABLES_IN_BACKUP" ]; then
    echo -e "${YELLOW}경고: 백업 파일에서 테이블 정보를 추출할 수 없습니다.${NC}"
    echo -e "${YELLOW}전체 파일을 복원하시겠습니까?${NC}"
    read -p "(y/n): " confirm
    if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
        echo -e "${RED}복원을 취소합니다.${NC}"
        exit 0
    fi
    RESTORE_ALL=true
else
    echo -e "\n${YELLOW}백업 파일에 포함된 테이블:${NC}"
    i=1
    TABLE_ARRAY=()
    while IFS= read -r table; do
        echo "$i) $table"
        TABLE_ARRAY+=("$table")
        ((i++))
    done <<< "$TABLES_IN_BACKUP"

    # 복원 전 현재 테이블 상태 확인
    echo -e "\n${BLUE}현재 데이터베이스의 테이블 목록:${NC}"
    CURRENT_TABLES=$(docker exec "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -e "SHOW TABLES;" -s --skip-column-names 2>/dev/null || echo "")

    if [ -n "$CURRENT_TABLES" ]; then
        echo "$CURRENT_TABLES" | head -10
        TABLE_COUNT=$(echo "$CURRENT_TABLES" | wc -l)
        if [ "$TABLE_COUNT" -gt 10 ]; then
            echo "... (총 $TABLE_COUNT 개)"
        fi
    else
        echo -e "${YELLOW}(테이블 없음 또는 조회 실패)${NC}"
    fi

    RESTORE_ALL=false
fi

# 경고 메시지
echo -e "\n${RED}========== 경고 ==========${NC}"
echo -e "${RED}이 작업은 선택한 테이블의 데이터를 덮어씁니다!${NC}"
echo -e "${RED}기존 데이터가 손실될 수 있습니다.${NC}"
echo -e "${RED}==========================${NC}"
echo ""

# 복원 옵션 선택
echo -e "${GREEN}복원 방식을 선택하세요:${NC}"
echo "1) 직접 복원 (기존 데이터 덮어쓰기)"
echo "2) 안전 복원 (기존 테이블 백업 후 복원)"
read -p "선택 (1-2): " restore_option

if [ "$restore_option" = "2" ]; then
    # 현재 데이터 백업
    SAFETY_BACKUP_DIR="backups/safety"
    mkdir -p "$SAFETY_BACKUP_DIR"
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    SAFETY_BACKUP_FILE="${SAFETY_BACKUP_DIR}/${ENV}_before_restore_${TIMESTAMP}.sql"

    echo -e "\n${GREEN}안전을 위해 현재 데이터를 백업합니다...${NC}"

    if [ "$RESTORE_ALL" = true ]; then
        docker exec "$CONTAINER_NAME" mysqldump -u"$DB_USER" -p"$DB_PASSWORD" \
            --single-transaction "$DB_NAME" > "$SAFETY_BACKUP_FILE" 2>/dev/null
    else
        # 백업 파일에 있는 테이블만 백업
        TABLES_TO_BACKUP=()
        while IFS= read -r table; do
            if echo "$CURRENT_TABLES" | grep -q "^${table}$"; then
                TABLES_TO_BACKUP+=("$table")
            fi
        done <<< "$TABLES_IN_BACKUP"

        if [ ${#TABLES_TO_BACKUP[@]} -gt 0 ]; then
            docker exec "$CONTAINER_NAME" mysqldump -u"$DB_USER" -p"$DB_PASSWORD" \
                --single-transaction "$DB_NAME" "${TABLES_TO_BACKUP[@]}" > "$SAFETY_BACKUP_FILE" 2>/dev/null
        fi
    fi

    if [ -f "$SAFETY_BACKUP_FILE" ] && [ -s "$SAFETY_BACKUP_FILE" ]; then
        SAFETY_SIZE=$(du -h "$SAFETY_BACKUP_FILE" | cut -f1)
        echo -e "${GREEN}✓ 안전 백업 완료: ${YELLOW}$SAFETY_BACKUP_FILE${NC} (${SAFETY_SIZE})"
    else
        echo -e "${YELLOW}현재 데이터가 없거나 백업에 실패했습니다. 계속 진행하시겠습니까?${NC}"
        read -p "(y/n): " continue_anyway
        if [[ ! "$continue_anyway" =~ ^[Yy]$ ]]; then
            echo -e "${RED}복원을 취소합니다.${NC}"
            exit 0
        fi
    fi
fi

# 최종 확인
echo -e "\n${YELLOW}정말로 복원하시겠습니까?${NC}"
read -p "(yes/no): " final_confirm

if [ "$final_confirm" != "yes" ]; then
    echo -e "${RED}복원을 취소합니다.${NC}"
    exit 0
fi

# 복원 실행
echo -e "\n${GREEN}복원을 시작합니다...${NC}"

docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$BACKUP_FILE" 2>&1 | \
    grep -v "mysql: \[Warning\]" || true

# 결과 확인
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 복원이 완료되었습니다!${NC}"

    # 복원된 테이블 확인
    echo -e "\n${GREEN}복원 후 테이블 목록:${NC}"
    docker exec "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" \
        -e "SHOW TABLES;" -s --skip-column-names 2>/dev/null | head -20

    if [ "$restore_option" = "2" ] && [ -f "$SAFETY_BACKUP_FILE" ]; then
        echo -e "\n${BLUE}안전 백업 파일이 보관되어 있습니다:${NC}"
        echo -e "${YELLOW}$SAFETY_BACKUP_FILE${NC}"
        echo -e "문제가 발생하면 이 파일로 롤백할 수 있습니다."
    fi
else
    echo -e "${RED}✗ 복원 실패${NC}"
    if [ "$restore_option" = "2" ] && [ -f "$SAFETY_BACKUP_FILE" ]; then
        echo -e "\n${YELLOW}안전 백업 파일로 롤백하시겠습니까?${NC}"
        read -p "(y/n): " rollback
        if [[ "$rollback" =~ ^[Yy]$ ]]; then
            echo -e "${GREEN}롤백 중...${NC}"
            docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$SAFETY_BACKUP_FILE"
            echo -e "${GREEN}✓ 롤백 완료${NC}"
        fi
    fi
    exit 1
fi
