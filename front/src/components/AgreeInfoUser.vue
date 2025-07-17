<template>
    <div class="titles">
        <h5>📄 [개인정보 수집·이용 동의서] (필수)</h5>
        <span>전체</span>
    </div>  
    <div class="terms-box">
        <div class="article">
            <h6>본인은 도서 대여 서비스 제공을 위하여 아래와 같은 개인정보를 수집·이용하는 것에 동의합니다.</h6>
            <p>
            1. 수집 항목 <br>
            이름, 아이디(ID), 비밀번호, 디스코드 아이디
            </p>
            <p>
            2. 수집·이용 목적 <br>
            회원 식별 및 관리
            </p>
            <p>
            도서 대여 및 반납 서비스 제공
            </p>
            <p>
            디스코드 아이디를 통한 반납 기한 알림 메시지 발송
            </p>
            <p>
            3. 보유 및 이용 기간 <br>
            회원 탈퇴 시까지 보유 및 이용
            </p>
            <p>
            관련 법령에 따라 보존할 필요가 있는 경우 해당 법령에 따름
            </p>
            <p>
            4. 동의를 거부할 권리 및 불이익 <br>
            귀하는 개인정보 수집·이용에 동의하지 않을 수 있습니다.
            </p>
            <p>
            단, 필수 항목에 대한 동의를 거부할 경우, 서비스 이용이 제한될 수 있습니다.
            </p>
            <p>
            ▶ [필수] 위 내용을 충분히 읽고 이해하였으며, 개인정보 수집·이용에 동의합니다.
            </p>
        </div>
    </div>
    <label for="agreeInfoUserCheck" class="agree_info_user_check">
        개인정보 수집·이용에 동의합니다.
        <input type="checkbox" name="agree_info_user_check" id="agreeInfoUserCheck" class="agree_info_user_input"
        :checked="isInfoAgree" @change="$emit('update:isInfoAgree', $event.target.checked)">
    </label>
</template>

<script setup>
defineProps({
  isInfoAgree: {
    type: Boolean,
    required: true
  }
})
defineEmits(['update:isInfoAgree'])
</script>

<style>
.agree_info_user_check {
    display: flex;
    margin-top: 12px;
    margin-bottom: 20px;
    align-self: flex-end;
    align-items: center;
}
.agree_info_user_input {
    margin-left: 10px;
}
.agree_info_user_check::after {
    content: " ";
}
</style>