<template>
    <div class="titles">
        <h5>📄 [도서 대여 및 반납 알림 수신 동의서] (필수)</h5>
        <span>전체</span>
    </div>  
    <div class="terms-box">
        <div class="article">
            <h6>본인은 도서 대여 및 반납 기한과 관련된 알림 메시지를 수신하는 것에 동의합니다.</h6>
            <p>
            1. 수신 항목 및 수단 <br>
            수신 항목: 도서 대여 완료 안내, 반납 예정일 안내, 연체 발생 알림 등
            </p>
            <p>
            수신 수단: 디스코드 메시지
            </p>
            <p>
            2. 이용 목적 <br>
            도서 대여 및 반납 기한 안내
            </p>
            <p>
            연체 방지 및 원활한 서비스 운영을 위한 알림 발송
            </p>
            <p>
            3. 보유 및 이용 기간 <br>
            동의일로부터 회원 탈퇴 또는 수신 동의 철회 시까지
            </p>
            <p>
            4. 동의를 거부할 권리 및 불이익 <br>
            귀하는 개인정보 수집·이용에 동의하지 않을 수 있습니다.
            </p>
            <p>
            단, 필수 항목에 대한 동의를 거부할 경우, 서비스 이용이 제한될 수 있습니다.
            </p>
            ▶ [필수] 위 내용을 충분히 읽고 이해하였으며, 도서 대여 및 반납 알림 수신에 동의합니다.
        </div>
    </div>
    <label for="agreeDiscordUserCheck" class="agree_discord_user_check">
        도서 대여 및 반납 알림 수신에 동의합니다.
        <input type="checkbox" name="agree_discord_user_check" id="agreeDiscordUserCheck" class="agree_discord_user_input"
        :checked="isDiscordAgree" @change="$emit('update:isDiscordAgree', $event.target.checked)">
    </label>
    
</template>

<script setup>
defineProps({
  isDiscordAgree: {
    type: Boolean,
    required: true
  }
})
defineEmits(['update:isDiscordAgree'])
</script>

<style>
.agree_discord_user_check {
    display: flex;
    margin-top: 12px;
    align-self: flex-end;
    align-items: center;
}
.agree_discord_user_input {
    margin-left: 10px;
}
.agree_discord_user_check::after {
    content: " ";
}
</style>