---
name: vue-page-convention
description: "Playbook 프론트엔드(Vue 3) 코드를 작성·수정할 때 반드시 사용하는 규약 스킬. api/*.js 모듈 경유 호출, axios 인터셉터 금지(파일별 직접 수정), res.data.data 이중 래핑 언래핑, --pb-* 디자인 토큰, sweetAlert·apiErrorHandler·dateFormatter·statusMapper·exportSheet·usePagination 등 기존 유틸 재사용을 다룬다. front/ 하위 .vue/.js 파일을 만들거나 고칠 때, 관리자 탭·화면·컴포넌트를 추가할 때, API 호출을 붙일 때, 목록·검색·페이지네이션·엑셀·차트를 구현할 때, 그리고 그 결과를 다시 고치거나 보완할 때도 이 스킬을 사용할 것."
---

# Playbook 프론트엔드 규약

`front/src/` 하위 코드는 아래 규약을 따른다. 스택은 Vue 3 + Vite + axios + Bootstrap + SweetAlert2 + Chart.js + xlsx + jsbarcode다. **TypeScript를 도입하지 않는다** — 순수 JS 프로젝트이며 주변 파일의 스타일을 따른다.

## 1. 인터셉터 금지 — 파일별 직접 처리

**axios 인터셉터, 전역 플러그인, 새 래퍼 계층을 만들지 않는다.** 사용자가 명시적으로 금지한 사항이다. 공통 처리를 하고 싶더라도 각 파일에서 직접 호출하는 방식을 유지한다.

이유: 인터셉터는 호출 지점에서 무슨 일이 일어나는지 감춘다. 이 프로젝트는 화면마다 에러 처리·리다이렉트 정책이 달라 전역 처리가 오히려 버그를 만든다.

에러 처리는 호출부에서 `handleApiError`를 개별 호출한다.

```js
import { handleApiError } from '@/utils/apiErrorHandler.js'
import * as favorApi from '@/api/favor.js'

try {
  const res = await favorApi.getAll()
  this.items = res.data.data
} catch (e) {
  await handleApiError(e, '즐겨찾기 조회에 실패했습니다.')
}
```

`handleApiError(error, 기본메시지)`는 401/403/400을 구분해 `swAlert`로 안내하고, 그 외는 서버 `msg` 또는 기본 메시지를 띄운다.

## 2. API 호출은 api 모듈 경유

컴포넌트에서 `axios`나 `fetch`를 직접 부르지 않는다. `front/src/api/{domain}.js`에 함수를 추가하고 그것을 임포트한다. 과거 raw fetch를 모듈로 통합한 리팩토링 이력이 있다(커밋 `9b4eef2`).

기존 모듈: `admin.js` `book.js` `campus.js` `course.js` `external.js` `favor.js` `history.js` `integration.js` `sort.js` `terms.js` `user.js`

```js
// front/src/api/favor.js
import axios from 'axios'

export const getAll = () => axios.get('/api/favor')

export const add = (seqBook) =>
  axios.post('/api/favor', seqBook, { headers: { 'Content-Type': 'application/json' } })

export const remove = (seqBook) =>
  axios.delete('/api/favor', { headers: { 'Content-Type': 'application/json' }, data: seqBook })
```

규약:

- URL은 `/api` 접두사를 포함한다 (백엔드 `context-path=/api`). 컨트롤러 매핑이 `/favor`면 `/api/favor`
- 함수는 얇게 유지한다 — 언래핑·에러 처리·상태 변경을 여기서 하지 않고 호출부에 맡긴다
- 백엔드가 원시 타입을 `@RequestBody`로 받는 엔드포인트는 **`Content-Type: application/json`을 명시**해야 한다. 누락하면 415/400이 난다
- DELETE에 바디를 보낼 때는 `{ data: payload }` 형태

## 3. 응답 언래핑 — res.data.data

백엔드는 모든 응답을 `{ code, msg, data }`로 감싼다. axios가 한 겹 더 감싸므로 **실제 데이터는 `res.data.data`**다.

```js
const res = await bookApi.getList(params)
const books = res.data.data          // ✅
// const books = res.data            // ❌ { code, msg, data } 객체가 들어온다
```

이 프로젝트에서 가장 자주 나는 경계면 버그다. 백엔드 응답 shape을 확인할 때는 컨트롤러가 `ResponseHandler.success(x)`에 넘긴 `x`가 배열인지 객체인지를 보고, 그것이 `res.data.data`에 그대로 온다고 전제한다.

성공/실패 분기가 필요하면 `res.data.code === '0000'`을 확인한다.

## 4. 디자인 토큰 — --pb-*

색상·간격·반경·그림자를 하드코딩하지 않는다. `assets/main.css`에 정의된 `--pb-*` 토큰을 쓴다.

```css
.card {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  box-shadow: var(--pb-shadow-xs);
  color: var(--pb-color-text);
}
```

토큰 계열: `--pb-color-*` (`text`, `canvas`, `surface`, `border`, `brand`, `brand-strong`, `brand-soft`, `danger`, `danger-soft`), `--pb-radius-{sm,md}`, `--pb-shadow-{xs,sm}`, `--pb-content-max`.

필요한 토큰이 없으면 기존 네이밍 규칙에 맞춰 `assets/main.css`에 추가한 뒤 사용하고, 무엇을 추가했는지 보고한다. 임의의 hex 값을 컴포넌트에 박아 넣으면 디자인 일관성이 깨진다.

## 5. 기존 유틸·컴포저블 재사용

같은 기능을 새로 만들지 않는다. 먼저 아래를 확인한다.

| 파일 | 용도 |
|------|------|
| `utils/apiErrorHandler.js` | API 에러 → 사용자 안내 (`handleApiError`) |
| `utils/sweetAlert.js` | 알림·확인 창 (`swAlert` 등). **`alert()`/`confirm()` 금지** |
| `utils/dateFormatter.js` | 날짜 포맷 |
| `utils/statusMapper.js` | 상태 코드 → 한글 라벨 |
| `utils/exportSheet.js` | 엑셀 내보내기 (xlsx) |
| `utils/constants.js` | 공용 상수 |
| `utils/mobileDetect.js` | 모바일 분기 |
| `composables/usePagination.js` | 페이지네이션 상태 |
| `composables/useAdminCampusFilter.js` | 관리자 캠퍼스 필터 |

엑셀 내보내기는 `exportSheet.js`의 컬럼 키가 DTO 필드명과 정확히 일치해야 한다 — 어긋나면 빈 열이 조용히 생긴다.

## 6. 화면 구성

- 페이지 단위는 `views/`, 재사용 단위는 `components/`, 라우팅은 `router/`
- 관리자 화면은 `views/AdminPage.vue`가 탭 컨테이너이고, 각 탭이 `components/*Management.vue` 또는 `*Dashboard.vue`다. **새 관리자 기능은 탭 컴포넌트로 추가하고 `AdminPage.vue`에 등록한다**
- 전체관리자 전용 탭(연동 설정 등)은 노출 조건을 확인한다 — 백엔드가 `requireSuperAdmin`으로 막고 있으면 프론트도 캠퍼스 관리자에게 탭을 보이지 않아야 한다
- 차트는 Chart.js, 바코드는 jsbarcode를 사용한다 (`Barcode.vue`, `BookPrintBatch.vue` 참고)

## 7. 권한·상태 처리

- 화면 노출을 권한 판단의 유일한 수단으로 쓰지 않는다. 서버가 막는 것이 본체이고 프론트는 UX다
- 401은 로그인 안내 후 로그인 화면으로, 403은 권한 없음 안내 — `handleApiError`가 처리하므로 별도 분기를 중복 구현하지 않는다
- 대출 상태·계정 상태 라벨은 `statusMapper.js`를 거친다. 상태 값 문자열을 컴포넌트에 흩뿌리지 않는다

## 검증

```bash
cd front && npm run build
```

빌드 통과는 최소 조건이다. JS 프로젝트라 타입 검사가 없으므로 **응답 shape 오류는 빌드로 잡히지 않는다** — 백엔드 DTO 필드명과 접근 키를 직접 대조하거나 `integration-qa`의 교차 검증을 받는다.

로컬 실행은 `npm run dev` (백엔드 8090이 기동돼 있어야 API가 응답한다).
