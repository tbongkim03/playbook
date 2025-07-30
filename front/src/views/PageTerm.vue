<template>
    <div class="terms-wrapper">
        <div class="terms-format">
            <header>
                <router-link to="/" custom v-slot="{ navigate }">
                    <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" @click="navigate" />
                </router-link>
            </header>
            <label class="all-terms-agree">
                <input type="checkbox" id="allTermsAgree" :checked="allAgree" @change="onAllAgreeChange">전체 동의하기
            </label>
            <div class="agree_terms_list">
                <AgreeTermsUser v-model:isTermsAgree="termsAgree"/>
                <AgreeInfoUser v-model:isInfoAgree="infoAgree"/>
                <AgreeDiscordUser v-model:isDiscordAgree="discordAgree"/>
                <button id="agreeBtn" class="agree_btn" :disabled="!allChecked" :class="{ active: allChecked }" @click="goToRegister">다음</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AgreeTermsUser from '@/components/AgreeTermsUser.vue';
import AgreeInfoUser from '@/components/AgreeInfoUser.vue';
import AgreeDiscordUser from '@/components/AgreeDiscordUser.vue';

const termsAgree = ref(false)
const infoAgree = ref(false)
const discordAgree = ref(false)

const allAgree = ref(false)

const allChecked = computed(() => termsAgree.value && infoAgree.value && discordAgree.value)

const isManualTrigger = ref(false)

const route = useRoute()
const router = useRouter()

const fromLogin = window.history.state?.fromLogin

if (!fromLogin) {
  alert('잘못된 접근입니다.')
  router.replace('/')
}

watch(allAgree, (agree) => {
  if (!isManualTrigger.value) return

  termsAgree.value = agree
  infoAgree.value = agree
  discordAgree.value = agree

  isManualTrigger.value = false
})

watch([termsAgree, infoAgree, discordAgree], ([term, info, discord]) => {
  const newAllAgree = term && info && discord
  
  if (allAgree.value !== newAllAgree) {
    allAgree.value = newAllAgree
  }
})

function onAllAgreeChange(event) {
  isManualTrigger.value = true
  allAgree.value = event.target.checked
}

function goToRegister() {
  if (!allChecked.value) return

  router.push({
    path: '/register',
    name: 'PageRegister',
    state: {
      fromTerm: true
    }
  })
}
</script>

<style>
.logo-img {
    height: auto;
    opacity: 1;
    -webkit-transition: .3s ease-in-out;
    transition: .3s ease-in-out;
}

.logo-img:hover {
    opacity: .5;
}
.terms-wrapper {
    min-width: 1457px;
    min-height: 100%;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}
.terms-format {
    min-width: 458px;
    flex: 1;
}
.all-terms-agree {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-top: 21px;
    margin-bottom: 21px;
}
#allTermsAgree {
    margin-right: 10px;
}
.agree_terms_list {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
}
.agree_btn {
    margin-top: 50px;
    background: #8990a0;
    cursor: default;
    color: #fff;
    padding: 14px 0;
    border-radius: 6px;
    border: 1px solid rgba(0, 0, 0, .05);
    font-size: 18px;
    font-weight: 700;
    line-height: 22px;
}
.agree_btn.active {
  background: #00AB78; /* 활성 상태일 때 */
  cursor: pointer;
}
.agree_btn:hover {
    background: #00ab90;
}
.titles {
    display: flex;
    gap: 20px;
}
.titles span {
    color: gray;
}
.titles span::after {
    content: " >";
}
input[type="checkbox"] {
    width: 1rem;
    height: 1rem;
    border-radius: 50%;
    border: 1px solid #999;
    appearance: none;
    cursor: pointer;
    transition: background 0.2s;
  }

  input[type="checkbox"]:checked {
    background: #00AB78;
    border: none;
  }
.terms-format .terms-box {
    width: 458px;
    overflow: auto;
    box-sizing: border-box;
    max-height: 100px;
    margin: 9px 0 0 32px;
    padding: 15px;
    border-radius: 6px;
    border: 1px solid #d6d6d6;
}.terms-format .terms-box::-webkit-scrollbar {
    width: 10px;
  }
  .terms-format .terms-box::-webkit-scrollbar-thumb {
    background-color: #c5ccd2;
    border-radius: 10px;
    background-clip: padding-box;
    border: 2px solid transparent;
  }
  .terms-format .terms-box::-webkit-scrollbar-track {
    background-color: #fff;
    border-radius: 10px;
    box-shadow: inset 0px 0px 5px white;
  }
</style>