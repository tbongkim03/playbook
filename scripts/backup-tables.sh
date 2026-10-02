#!/bin/bash

# 테이블 선택 백업 스크립트
# 사용법: ./scripts/backup-tables.sh [dev|prod]

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 환경 설정
ENV=${1:-dev}
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
    echo "사용법: ./scripts/backup-tables.sh [dev|prod]"
    exit 1
fi

echo -e "${GREEN}=== 데이터베이스 테이블 백업 스크립트 ===${NC}"
echo -e "환경: ${YELLOW}$ENV${NC}"
echo -e "컨테이너: ${YELLOW}$CONTAINER_NAME${NC}"
echo -e "데이터베이스: ${YELLOW}$DB_NAME${NC}"
echo ""

# 컨테이너 실행 여부 확인
if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo -e "${RED}오류: $CONTAINER_NAME 컨테이너가 실행 중이 아닙니다.${NC}"
    exit 1
fi

# 백업 디렉토리 생성
BACKUP_DIR="backups"
mkdir -p "$BACKUP_DIR"

# 현재 날짜/시간
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 데이터베이스의 모든 테이블 목록 가져오기
echo -e "${GREEN}데이터베이스의 테이블 목록을 가져오는 중...${NC}"
TABLES=$(docker exec "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -e "SHOW TABLES;" -s --skip-column-names)

if [ -z "$TABLES" ]; then
    echo -e "${RED}오류: 테이블을 찾을 수 없습니다.${NC}"
    exit 1
fi

# 테이블 목록 출력
echo -e "\n${YELLOW}사용 가능한 테이블 목록:${NC}"
i=1
TABLE_ARRAY=()
while IFS= read -r table; do
    echo "$i) $table"
    TABLE_ARRAY+=("$table")
    ((i++))
done <<< "$TABLES"

echo -e "\n${GREEN}백업할 테이블을 선택하세요:${NC}"
echo "- 단일 선택: 1"
echo "- 여러 개 선택: 1,2,3 또는 1 2 3"
echo "- 범위 선택: 1-5"
echo "- 전체 선택: all 또는 a"
echo ""
read -p "선택: " selection

# 선택된 테이블 처리
SELECTED_TABLES=()

if [[ "$selection" == "all" ]] || [[ "$selection" == "a" ]]; then
    SELECTED_TABLES=("${TABLE_ARRAY[@]}")
else
    # 쉼표를 공백으로 변환
    selection=${selection//,/ }

    for item in $selection; do
        if [[ $item =~ ^([0-9]+)-([0-9]+)$ ]]; then
            # 범위 선택 (예: 1-5)
            start=${BASH_REMATCH[1]}
            end=${BASH_REMATCH[2]}
            for ((j=start; j<=end; j++)); do
                if [ $j -ge 1 ] && [ $j -le ${#TABLE_ARRAY[@]} ]; then
                    SELECTED_TABLES+=("${TABLE_ARRAY[$((j-1))]}")
                fi
            done
        elif [[ $item =~ ^[0-9]+$ ]]; then
            # 단일 선택 (예: 1)
            if [ $item -ge 1 ] && [ $item -le ${#TABLE_ARRAY[@]} ]; then
                SELECTED_TABLES+=("${TABLE_ARRAY[$((item-1))]}")
            fi
        fi
    done
fi

if [ ${#SELECTED_TABLES[@]} -eq 0 ]; then
    echo -e "${RED}오류: 유효한 테이블이 선택되지 않았습니다.${NC}"
    exit 1
fi

echo -e "\n${GREEN}선택된 테이블 (${#SELECTED_TABLES[@]}개):${NC}"
printf '%s\n' "${SELECTED_TABLES[@]}"

# 백업 옵션 선택
echo -e "\n${GREEN}백업 옵션을 선택하세요:${NC}"
echo "1) 구조 + 데이터 (전체)"
echo "2) 데이터만 (구조 제외)"
echo "3) 구조만 (데이터 제외)"
read -p "선택 (1-3): " backup_option

DUMP_OPTIONS=""
case $backup_option in
    1)
        DUMP_OPTIONS=""
        BACKUP_TYPE="full"
        ;;
    2)
        DUMP_OPTIONS="--no-create-info"
        BACKUP_TYPE="data_only"
        ;;
    3)
        DUMP_OPTIONS="--no-data"
        BACKUP_TYPE="schema_only"
        ;;
    *)
        echo -e "${RED}잘못된 선택입니다. 기본값(전체)으로 진행합니다.${NC}"
        BACKUP_TYPE="full"
        ;;
esac

# 백업 파일명
BACKUP_FILE="${BACKUP_DIR}/${ENV}_${BACKUP_TYPE}_${TIMESTAMP}.sql"

# 백업 실행
echo -e "\n${GREEN}백업을 시작합니다...${NC}"
echo -e "백업 파일: ${YELLOW}$BACKUP_FILE${NC}"

docker exec "$CONTAINER_NAME" mysqldump -u"$DB_USER" -p"$DB_PASSWORD" \
    $DUMP_OPTIONS \
    --single-transaction \
    --routines \
    --triggers \
    "$DB_NAME" "${SELECTED_TABLES[@]}" > "$BACKUP_FILE"

# 결과 확인
if [ -f "$BACKUP_FILE" ]; then
    FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo -e "${GREEN}✓ 백업이 완료되었습니다!${NC}"
    echo -e "파일: ${YELLOW}$BACKUP_FILE${NC}"
    echo -e "크기: ${YELLOW}$FILE_SIZE${NC}"
    echo -e "테이블 수: ${YELLOW}${#SELECTED_TABLES[@]}${NC}"
else
    echo -e "${RED}✗ 백업 실패${NC}"
    exit 1
fi
