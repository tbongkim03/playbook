<template>
  <div class="register-wrapper">
    <div class="register-format">
      <header>
        <router-link to="/" custom v-slot="{ navigate }">
          <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" @click="navigate" />
        </router-link>
      </header>
      <form @submit.prevent="handleSubmit" class="register-form">
        <input
          type="text"
          v-model="name"
          @blur="validateName"
          id="inputFormName"
          :class="{ 
            'input-error-name': errors.name,
            'input-success': name && !errors.name
          }"
          placeholder="이름"
        />
        <input
          type="text"
          v-model="username"
          @blur="validateUsername"
          id="inputFormId"
          :class="{ 
            'input-error-id': errors.username,
            'input-success': errors.username && errors.username.includes('사용 가능한')
          }"
          placeholder="아이디"
        />
        <input
          type="password"
          v-model="password"
          @blur="validatePassword"
          id="inputFormPw"
          :class="{ 
            'input-error-pw': errors.password,
            'input-success': password.length >= 6 && !errors.password
          }"
          placeholder="비밀번호"
        />

        <ul class="rule-message">
          <li v-if="errors.name">{{ errors.name }}</li>
          <li v-if="errors.username">{{ errors.username }}</li>
          <li v-if="errors.password">{{ errors.password }}</li>
        </ul>

        <input
          type="text"
          v-model="discord"
          @blur="validateDiscord"
          id="inputFormDiscord"
          :class="{ 
            'input-error-discord': errors.discord,
            'input-success': discord.length >= 6 && !errors.discord
          }"
          placeholder="디스코드 아이디 (반납일 알림)"
        />

        <ul class="rule-message2">
          <li v-if="errors.discord">{{ errors.discord }}</li>
        </ul>

        <div class="custom-select-wrapper" @click="toggleCourseBox" ref="dropdownWrapper">
            <button type="button" class="toggle-btn" :class="{ 'input-error-course': errors.course }">
                {{ selectedCourse || '수강중인 훈련과정을 선택해주세요' }}
                <!-- <img src="@/assets/icon-Triangle-down.svg" alt=""  /> -->
                <svg class="ico-down" xmlns="http://www.w3.org/2000/svg" width="800" height="800" viewBox="0 0 96.154 96.154"><path fill="currentColor" d="M.561 20.971l45.951 57.605c.76.951 2.367.951 3.127 0l45.956-57.609c.547-.689.709-1.716.414-2.61a2.686 2.686 0 00-.186-.437 2.004 2.004 0 00-1.765-1.056H2.093c-.736 0-1.414.405-1.762 1.056a2.62 2.62 0 00-.184.426c-.297.905-.136 1.934.414 2.625z"/></svg>
            </button>
            <ul class="selectbox-option" :class="{ 'position-up': dropdownPositionUp }" v-show="courseBoxOpen">
                <li
                v-for="(item, index) in courseList"
                :key="index"
                >
                <button
                    type="button"
                    class="option-btn"
                    :class="{ selected: selectedCourse === item.title + ' ' + item.trprDegr + '기' }"
                    @click.stop="selectCourse(item)"
                >
                    {{ item.title + ' ' + item.trprDegr + '기' }}
                </button>
                </li>
            </ul>
        </div>

        <ul class="rule-message3">
          <li v-if="errors.course">{{ errors.course }}</li>
        </ul>

        <button type="submit" class="submit-btn">
          <span>회원가입</span>
        </button>
      </form>
    </div>

    <footer>
      <span>Copyright © 2025 플레이데이터 All Rights Reserved.</span>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const dropdownWrapper = ref(null)
const dropdownPositionUp = ref(false)
const courseBoxOpen = ref(false)

const route = useRoute()
const router = useRouter()

const name = ref('')
const username = ref('')
const password = ref('')
const discord = ref('')

const errors = ref({
  name: '',
  username: '',
  password: '',
  discord: '',
  course: ''
})

const courseList = ref([])
const selectedCourse = ref('')

// 날짜 계산
const today = new Date()
const sixMonthsAgo = new Date()
sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6)

function formatDateToYYYYMMDD(date) {
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}${mm}${dd}`
}

const srchTraStDt = formatDateToYYYYMMDD(sixMonthsAgo)
const srchTraEndDt = formatDateToYYYYMMDD(today)

onMounted(() => {
  const state = window.history.state
  const hasValidState = state && state.terms === true && state.info === true && state.discord === true

  if (!hasValidState) {
    alert('잘못된 접근입니다.')
    setTimeout(() => {
      router.replace('/')
    }, 0)
  }

  getCourseList()
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

async function getCourseList() {
  const apiKey = import.meta.env.VITE_WORK24_API_KEY
  const url =
    `https://www.work24.go.kr/cm/openApi/call/hr/callOpenApiSvcInfo310L01.do?authKey=${apiKey}` +
    `&returnType=JSON&outType=1&pageNum=1&pageSize=100` +
    `&srchTraStDt=${srchTraStDt}&srchTraEndDt=${srchTraEndDt}` +
    `&srchTraArea1=11&srchNcs1=20&crseTracseSe=C0104&srchTraGbn=M1001&srchTraOrganNm=플레이데이터평생교육원` +
    `&sort=ASC&sortCol=2`

  try {
    const res = await fetch(url)
    const data = await res.json()
    const apiCoursesRaw = data?.srchList || []

    if (apiCoursesRaw.length === 0) {
      alert('훈련 과정을 찾을 수 없습니다.')
      return
    }

    // 1. 외부 API 데이터 가공
    const apiCourses = apiCoursesRaw.map(item => {
      const title = item.title.includes(' - ') ? item.title.split(' - ')[0] : item.title
      const fullName = `${title} ${item.trprDegr}기`
      return {
        nameCourse: fullName,
        startDtCourse: item.traStartDate,
        finishDtCourse: item.traEndDate,
        trprDegr: item.trprDegr
      }
    })

    // 2. DB 데이터 가져오기
    const dbRes = await fetch('http://localhost:8080/courses')
    const dbCourses = await dbRes.json()

    // 3. 추가: API에는 있는데 DB에는 없는 과정 → INSERT
    for (const apiItem of apiCourses) {
      const exists = dbCourses.find(dbItem => dbItem.nameCourse === apiItem.nameCourse)
      if (!exists) {
        await fetch('http://localhost:8080/courses', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(apiItem)
        })
      }
    }

    // 4. 삭제: DB에는 있는데 API에는 없는 과정 → DELETE
    for (const dbItem of dbCourses) {
      const exists = apiCourses.find(apiItem => apiItem.nameCourse === dbItem.nameCourse)
      if (!exists) {
        await fetch(`http://localhost:8080/courses/${dbItem.seqCourse}`, {
          method: 'DELETE'
        })
      }
    }

    // 5. 수정: 둘 다 있지만 데이터 변경되었으면 → UPDATE
    for (const apiItem of apiCourses) {
      const dbItem = dbCourses.find(db => db.nameCourse === apiItem.nameCourse)
      if (dbItem) {
        const isDifferent =
          dbItem.startDtCourse !== apiItem.startDtCourse ||
          dbItem.finishDtCourse !== apiItem.finishDtCourse

        if (isDifferent) {
          await fetch(`http://localhost:8080/courses/${dbItem.seqCourse}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              ...apiItem,
              seqCourse: dbItem.seqCourse
            })
          })
        }
      }
    }

    // 6. 드롭다운 표시용 courseList 값 세팅
    courseList.value = apiCourses
    .map(item => {
      const parts = item.nameCourse.split(' ')
      const isLastPartGeneration = parts[parts.length - 1].endsWith('기')
      const title = isLastPartGeneration
        ? parts.slice(0, -1).join(' ')
        : item.nameCourse
      return {
        title,
        trprDegr: item.trprDegr
      }
    })
    .sort((a, b) => a.title.localeCompare(b.title, 'ko'))


  } catch (err) {
    console.error('API 조회 실패:', err)
    alert('훈련과정 정보를 조회하는 중 오류가 발생했습니다.')
  }
}

function toggleCourseBox() {
  courseBoxOpen.value = !courseBoxOpen.value

  nextTick(() => {
    if (dropdownWrapper.value) {
      const rect = dropdownWrapper.value.getBoundingClientRect()
      const dropdownHeight = 240
      const spaceBelow = window.innerHeight - rect.bottom
      dropdownPositionUp.value = spaceBelow < dropdownHeight
    }
  })
}

function selectCourse(item) {
  selectedCourse.value = item.title + ' ' + item.trprDegr + '기'
  courseBoxOpen.value = false
}

// 외부 클릭 시 드롭다운 닫기
function handleClickOutside(event) {
  if (
    courseBoxOpen.value &&
    dropdownWrapper.value &&
    !dropdownWrapper.value.contains(event.target)
  ) {
    courseBoxOpen.value = false
  }
}



// 유효성 검사
function validateName() {
  errors.value.name = name.value.trim() ? '' : '이름: 필수 정보입니다.'
}

async function validateUsername() {
  const trimmedId = username.value.trim()

  if (!trimmedId) {
    errors.value.username = '아이디: 필수 정보입니다.'
    return
  }

  // admin 아이디 금지
  if (trimmedId === 'admin') {
    errors.value.username = '아이디: 사용할 수 없는 아이디입니다. 다른 아이디를 입력해 주세요.'
    return
  }

  try {
    const response = await fetch(
      `http://localhost:8080/users/register/validate?id=${encodeURIComponent(trimmedId)}`
    )
    if (!response.ok) throw new Error('네트워크 오류')

    const data = await response.json()

    if (data.flag === 0) {
      errors.value.username = '아이디: 사용할 수 없는 아이디입니다. 다른 아이디를 입력해 주세요.'
    } else if (data.flag === 1) {
      
    }
  } catch (error) {
    console.error('아이디 검사 실패:', error)
    errors.value.username = '아이디: 서버 오류로 확인할 수 없습니다.'
  }
}

function validatePassword() {
  errors.value.password =
    password.value.length < 6
      ? '비밀번호: 6자 이상 입력해 주세요.'
      : ''
}

function validateDiscord() {
  errors.value.discord = discord.value.trim() ? '' : '디스코드 아이디: 필수 정보입니다.'
}

function validateCourse() {
  errors.value.course = selectedCourse.value ? '' : '훈련과정: 필수 선택 항목입니다.'
}

function handleSubmit() {
  validateName()
  validateUsername()
  validatePassword()
  validateDiscord()
  validateCourse()

  const hasError = Object.values(errors.value).some(error => error !== '')
  if (hasError) {
    return
  }

  // 여기서 회원가입 처리 진행
  console.log('회원가입 데이터 전송')
}
</script>


<style>
.register-wrapper {
  min-width: 1455px;
  min-height: 100%;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  justify-self: center;
  align-items: center;
  overflow-x: hidden;
}
.register-format {
  min-width: 458px;
  flex: 1;
}
.register-form {
  display: flex;
  flex-direction: column;
  border: 1px solid #e1e3e5;
  border-radius: 12px;
  padding: 24px;
}
.rule-message {
  height: fit-content;
  margin: 10px 0;
}
.login-format form {
  display: flex;
  flex-direction: column;
  width: 100%;
  border: 1px solid #e1e3e5;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}
.register-format input {
  border: 1px solid #c5ccd2;
}
.register-format input {
  padding: 16px 15px 16px;
  z-index: 1;
  outline-color: #00ab90;
}
#inputFormName {
  border-top-right-radius: 8px;
  border-top-left-radius: 8px;
}
#inputFormId {
  border-top: none;
  border-bottom: none;
}
#inputFormPw {
  border-bottom-right-radius: 8px;
  border-bottom-left-radius: 8px;
}
#inputFormDiscord {
  border-radius: 8px;
}
.rule-message {
  font-size: 13px;
  min-height: 39px;
  color: red;
  margin: 10px 0;
  padding-left: 4px;
}
.rule-message2 {
  font-size: 13px;
  min-height: 20px;
  color: red;
  margin: 10px 0;
  padding-left: 4px;
}
.rule-message3 {
  font-size: 13px;
  min-height: 20px;
  color: red;
  padding-left: 4px;
}
.input-error-name, .input-error-id, .input-error-pw, .input-error-discord, .input-error-course {
  border: 1px solid red !important;
}
li {
  list-style: none;
}
.register-discord {
  margin-bottom: 17px;
  border-radius: 8px;
}
.custom-select-wrapper {
  position: relative;
  width: 100%;
  max-width: 458px;
  margin-bottom: 16px;
}
.toggle-btn {
  width: 100%;
  padding: 16px;
  border: 1px solid #c5ccd2;
  border-radius: 12px;
  background-color: white;
  text-align: left;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toggle-btn .ico-down {
  width: 16px;
  height: 16px;
}
.ico-down {
    color: #00ab90;
}
.selectbox-option {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  border: 1px solid #c5ccd2;
  border-radius: 12px;
  background-color: white;
  box-shadow: 0px 4px 12px rgba(0, 0, 0, 0.1);
  margin-top: 8px;
  padding: 0;
  z-index: 10;
  max-height: 240px;
  overflow-y: auto;
}.selectbox-option.position-up {
  top: auto;
  bottom: 100%; /* 위로 뜨게 함 */
  margin-top: 0;
  margin-bottom: 8px;
}.selectbox-option::-webkit-scrollbar {
    width: 10px;
  }
  .selectbox-option::-webkit-scrollbar-thumb {
    background-color: #c5ccd2;
    border-radius: 10px;
    background-clip: padding-box;
    border: 2px solid transparent;
  }
  .selectbox-option::-webkit-scrollbar-track {
    background-color: #fff;
    border-radius: 10px;
    box-shadow: inset 0px 0px 5px white;
  }
.selectbox-option > li {
    margin: 8px 8px 8px 8px;
    border-radius: 8px;
}
.option-btn {
  width: 100%;
  padding: 14px 16px 14px 10px;
  text-align: left;
  background-color: transparent;
  border: none;
  font-size: 16px;
  cursor: pointer;
}
.toggle-btn:active,
.option-btn:hover,
.option-btn.selected {
  background-color: #fceeff; /* 연보라색 */
  border-radius: 8px;
}
input:focus {
  outline-color: #00ab90;
}
.input-success {
  border: 1px solid #00ab90 !important;
}
.submit-btn {
  border: none;
  background-color: #00ab90;
  color: #fff;
  padding: 13px 20px;
  margin-top: 24px;
  font-weight: 400;
  font-size: 17px;
  line-height: 24px;
  border-radius: 8px;
}
</style>
