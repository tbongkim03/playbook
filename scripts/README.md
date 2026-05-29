# 스크립트 모음

배포, 롤백, 데이터베이스 마이그레이션을 위한 스크립트 모음입니다.

## 📋 스크립트 목록

### 1. `deploy.sh` - 서버 배포

GHCR에서 최신 이미지를 pull하고 컨테이너를 재시작합니다.

**사용법:**
```bash
./scripts/deploy.sh
```

**동작:**
1. `ghcr.io/tbongkim03/playbook-back:latest` pull
2. `ghcr.io/tbongkim03/playbook-front:latest` pull
3. `docker compose -f docker compose.prod.yml up -d --remove-orphans back front`
4. 미사용 이미지 정리

---

### 2. `rollback.sh` - 이미지 롤백

특정 sha 태그로 back/front 컨테이너를 롤백합니다.

**사용법:**
```bash
./scripts/rollback.sh <sha-태그>
```

**예시:**
```bash
./scripts/rollback.sh sha-a1b2c3d
```

---

### 3. `backup-tables.sh` - 테이블 선택 백업
특정 테이블을 선택하여 백업합니다.

**사용법:**
```bash
./scripts/backup-tables.sh [dev|prod]
```

**기능:**
- 대화형 테이블 선택 (단일/복수/범위/전체)
- 백업 옵션 선택:
  - 전체 (구조 + 데이터)
  - 데이터만
  - 구조만
- 백업 파일 자동 생성 (`backups/` 디렉토리)
- 타임스탬프 자동 추가

**예시:**
```bash
# Dev DB 백업
./scripts/backup-tables.sh dev

# Prod DB 백업
./scripts/backup-tables.sh prod
```

---

### 4. `restore-tables.sh` - 테이블 선택 복원
백업 파일을 선택하여 데이터베이스에 복원합니다.

**사용법:**
```bash
./scripts/restore-tables.sh [dev|prod] [백업파일]
```

**기능:**
- 백업 파일 목록에서 선택 가능
- 안전 복원 모드 (기존 데이터 자동 백업)
- 복원 실패 시 자동 롤백 옵션
- 복원 전 경고 및 확인

**예시:**
```bash
# 대화형으로 백업 파일 선택
./scripts/restore-tables.sh prod

# 특정 백업 파일 지정
./scripts/restore-tables.sh prod backups/dev_full_20240114_120000.sql
```

---

### 5. `migrate-db.sh` - 통합 마이그레이션 (Dev → Prod)
개발 서버에서 운영 서버로 데이터를 안전하게 마이그레이션합니다.

**사용법:**
```bash
./scripts/migrate-db.sh
```

**기능:**
- 전체 프로세스 자동화 (백업 + 복원)
- Dev/Prod 컨테이너 자동 확인 및 시작
- 각 테이블의 레코드 수 표시
- 안전 백업 (복원 전 Prod DB 자동 백업)
- 마이그레이션 후 결과 확인
- 실패 시 롤백 옵션

**프로세스:**
1. 환경 확인 (컨테이너 상태)
2. Dev DB 테이블 목록 조회
3. 마이그레이션할 테이블 선택
4. 백업 옵션 선택
5. Dev DB 백업
6. Prod DB 복원 (안전 백업 포함)

---

## 🚀 사용 예시

### 시나리오 1: 전체 데이터 마이그레이션
```bash
# 간단한 방법: 통합 스크립트 사용
./scripts/migrate-db.sh

# 대화형 프롬프트를 따라 진행:
# 1. 테이블 선택: all
# 2. 백업 옵션: 1 (전체)
# 3. 안전 백업: y
# 4. 최종 확인: yes
```

### 시나리오 2: 특정 테이블만 마이그레이션
```bash
# 방법 1: 통합 스크립트
./scripts/migrate-db.sh
# 선택: 1,3,5 (테이블 번호)

# 방법 2: 개별 스크립트
./scripts/backup-tables.sh dev
# 테이블 선택 후...
./scripts/restore-tables.sh prod backups/dev_full_20240114_120000.sql
```

### 시나리오 3: Dev DB만 백업
```bash
./scripts/backup-tables.sh dev
# 백업 파일이 backups/ 디렉토리에 생성됨
```

### 시나리오 4: 이전 백업으로 복원
```bash
./scripts/restore-tables.sh prod
# 백업 파일 목록에서 선택
```

---

## 📁 디렉토리 구조

```
playbook/
├── scripts/
│   ├── deploy.sh             # 서버 배포 (GHCR pull → compose up)
│   ├── rollback.sh           # 이미지 롤백 (sha 태그 지정)
│   ├── backup-tables.sh      # DB 테이블 백업
│   ├── restore-tables.sh     # DB 테이블 복원
│   ├── migrate-db.sh         # Dev → Prod 마이그레이션
│   └── README.md             # 이 파일
├── backups/                   # 백업 파일 저장 (자동 생성)
│   ├── dev_full_20240114_120000.sql
│   ├── prod_data_only_20240114_130000.sql
│   └── safety/               # 안전 백업 (자동 생성)
│       └── prod_before_migration_20240114_140000.sql
├── db/
│   ├── .env.dev              # Dev DB 환경변수
│   └── .env.prod             # Prod DB 환경변수
├── docker compose.dev.yml
└── docker compose.prod.yml
```

---

## ⚠️ 주의사항

### 실행 전 확인사항
1. **Docker 컨테이너 실행 확인**
   ```bash
   docker ps | grep db
   ```

2. **백업 디렉토리 확인**
   - 자동으로 생성되지만 충분한 디스크 공간 확보 필요

3. **환경 변수 확인**
   - `db/.env.dev`: Dev DB 접속 정보
   - `db/.env.prod`: Prod DB 접속 정보

### 안전 수칙
1. **운영 데이터 백업**: 항상 마이그레이션 전에 안전 백업 생성
2. **스테이징 테스트**: 가능하면 스테이징 환경에서 먼저 테스트
3. **점검 시간 활용**: 사용자가 적은 시간대에 실행
4. **백업 파일 보관**: 최소 7일간 백업 파일 보관 권장
5. **롤백 계획**: 문제 발생 시 롤백 절차 숙지

### 복원 실패 시
스크립트가 자동으로 안전 백업으로 롤백을 제안합니다:
```bash
# 수동 롤백이 필요한 경우
./scripts/restore-tables.sh prod backups/safety/prod_before_migration_YYYYMMDD_HHMMSS.sql
```

---

## 🔧 문제 해결

### 컨테이너가 실행되지 않는 경우
```bash
# Dev 컨테이너 시작
docker compose -f docker compose.dev.yml up -d db-dev

# Prod 컨테이너 시작
docker compose -f docker compose.prod.yml up -d db
```

### 권한 오류 발생 시
```bash
chmod +x scripts/*.sh
```

### 백업 파일이 너무 큰 경우
```bash
# 데이터만 백업 (구조 제외) 또는 특정 테이블만 선택
./scripts/backup-tables.sh dev
# 옵션 2 선택: 데이터만
```

### MySQL 비밀번호 오류
`db/.env.dev` 또는 `db/.env.prod` 파일의 비밀번호 확인

---

## 📊 백업 파일 명명 규칙

```
{환경}_{타입}_{타임스탬프}.sql

예시:
- dev_full_20240114_120000.sql        # Dev 전체 백업
- prod_data_only_20240114_130000.sql  # Prod 데이터만
- dev_schema_only_20240114_140000.sql # Dev 구조만
- dev_to_prod_full_20240114_150000.sql # 마이그레이션 백업
```

---

## 💡 팁

1. **정기 백업**: cron을 사용하여 자동 백업 설정
   ```bash
   # 매일 새벽 2시 자동 백업
   0 2 * * * /path/to/playbook/scripts/backup-tables.sh prod
   ```

2. **백업 파일 정리**: 오래된 백업 파일 정리
   ```bash
   # 30일 이상된 백업 파일 삭제
   find backups/ -name "*.sql" -mtime +30 -delete
   ```

3. **테이블 선택 범위**: 대량의 테이블을 선택할 때
   ```
   입력 예시:
   - 1-10      # 1번부터 10번까지
   - 1,3,5,7   # 1, 3, 5, 7번
   - 1-5,10-15 # 1~5번과 10~15번
   - all       # 전체
   ```

4. **백업 전 데이터 확인**: 각 테이블의 레코드 수가 표시되므로 확인 후 선택

---

## 📝 로그

스크립트는 다음 정보를 실시간으로 표시합니다:
- ✓ 성공한 작업 (초록색)
- ! 경고 메시지 (노란색)
- ✗ 실패한 작업 (빨간색)
- 각 테이블의 레코드 수
- 백업 파일 크기
- 복원 결과

---

## 🆘 지원

문제가 발생하면:
1. 스크립트 실행 로그 확인
2. Docker 컨테이너 로그 확인: `docker logs db-dev` 또는 `docker logs db`
3. 백업 파일 무결성 확인: `head -n 10 backups/파일명.sql`
