<template>
    <div class="register-wrapper">
        <div class="register-format">
            <header>
                <router-link to="/" custom v-slot="{ navigate }">
                    <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" @click="navigate" />
                </router-link>
            </header>
            <input type="checkbox" id="allTermsAgree" class="all-terms-agree"> <span>전체 동의하기</span>
            <div class="agree_terms_user">
                <div class="titles">
                    <h5>📜 플레이북 이용약관</h5>
                    <span>전체</span>
                </div>
                <div class="terms-box">
                    <div class="article">
                        <h6>본 약관은 도서 대여 서비스를 이용함에 있어 회사(또는 서비스 운영자)와 회원 간의 권리, 의무 및 책임 사항을 규정함을 목적으로 합니다.</h6>
                        <p>
                            제1조 (목적)<br><br>
                            이 약관은 회사가 제공하는 도서 대여 서비스(이하 “서비스”)의 이용 조건 및 절차, 회사와 회원 간의 권리·의무 및 책임사항을 규정함을 목적으로 합니다.
                        </p>
                        <p>
                            제2조 (정의) <br><br>
                            1. “회원”이라 함은 본 약관에 동의하고 서비스를 이용하는 자를 말합니다.<br><br>

                            2. “도서”라 함은 서비스에서 대여를 위해 제공하는 도서를 말합니다.<br><br>

                            3. “대여”라 함은 회사가 회원에게 일정 기간 도서를 빌려주는 행위를 말합니다.<br>
                        </p>
                        <p>
                            제3조 (약관의 게시 및 변경)<br><br>
                            1. 회사는 본 약관을 서비스 화면에 게시하거나 기타의 방법으로 회원에게 공지합니다.<br><br>

                            2. 회사는 관련 법령을 위배하지 않는 범위에서 약관을 개정할 수 있습니다.<br><br>

                            3. 개정 약관은 적용일자 및 개정 사유를 명시하여 공지하며, 회원이 변경된 약관에 동의하지 않을 경우 서비스 이용을 중단할 수 있습니다.<br>
                        </p>
                        <p>
                            제4조 (회원가입)<br><br>
                            1. 회원가입은 본 약관 및 개인정보 수집·이용 동의서에 동의하고, 회사가 정한 가입 양식에 정보를 기재하여 신청하면 회사가 이를 승낙함으로써 완료됩니다.<br><br>

                            2. 회사는 회원의 신청을 승낙함을 원칙으로 하나, 다음 각 호에 해당하는 경우 승낙을 거부할 수 있습니다.<br>

                            - 허위 정보를 기재한 경우<br>

                            - 타인의 정보를 도용한 경우<br>

                            - 기타 회사가 부적합하다고 판단한 경우<br>
                        </p>
                        <p>
                            제5조 (회원의 의무)<br><br>
                            1. 회원은 본 약관 및 서비스 이용안내, 공지사항을 준수해야 합니다.<br><br>

                            2. 회원은 타인의 권리나 명예를 침해하거나 공공질서를 해치는 행위를 해서는 안 됩니다.<br><br>
                            
                            3. 회원은 대여한 도서를 정해진 반납 기한까지 반환해야 합니다.<br>
                        </p>
                        <p>
                            제6조 (서비스의 제공 및 변경)<br><br>
                            1. 회사는 회원에게 도서 대여 서비스를 제공합니다.<br><br>

                            2. 회사는 서비스 제공을 위해 수집한 개인정보를 안전하게 관리·보관하며, 개인정보 처리방침에 따라 운영합니다.<br><br>

                            3. 회사는 서비스 운영상 필요한 경우 서비스의 내용을 변경하거나 중단할 수 있으며, 이 경우 회원에게 사전 공지합니다.<br>
                        </p>
                        <p>
                            제7조 (대여 기간 및 반납 의무)<br><br>
                            1. 회원은 회사가 정한 대여 기간 내에 대여한 도서를 반납해야 합니다.<br><br>

                            2. 대여 기간은 서비스 화면 또는 별도의 안내를 통해 공지합니다.<br>
                        </p>
                        <p>
                            제8조 (연체에 대한 제재)<br><br>
                            1. 회원이 반납 기한을 초과하여 도서를 연체할 경우, 연체일 수의 2배 기간 동안 추가 대여가 제한됩니다.<br><br>

                            - 예) 3일 연체 시 → 6일 대여 정지<br><br>

                            2. 회사는 연체 사실 및 대여 제한 기간을 회원에게 고지합니다.<br>
                        </p>
                        <p>
                            제9조 (도서의 분실 및 훼손)<br><br>
                            1. 회원이 대여한 도서를 분실하거나 심각하게 훼손한 경우, 동일 도서로 배상해야 합니다.<br><br>

                            2. 동일 도서로 배상이 어려운 경우, 회사가 인정하는 금액을 변상해야 할 수 있습니다.<br>
                        </p>
                        <p>
                            제10조 (서비스 이용의 제한)<br><br>
                            회사는 회원이 본 약관을 위반하거나 서비스 운영을 방해하는 경우, 사전 통보 후 서비스 이용을 제한할 수 있습니다.<br>
                        </p>
                        <p>
                            제11조 (면책조항)<br><br>
                            1. 회사는 천재지변, 불가항력적 사유로 인해 서비스를 제공할 수 없는 경우 책임을 지지 않습니다.<br><br>

                            2. 회사는 회원의 귀책사유로 발생한 손해에 대해 책임을 지지 않습니다.<br><br>

                            3. 회사는 회원 간, 회원과 제3자 간의 거래나 분쟁에 개입하지 않으며, 그로 인한 손해를 책임지지 않습니다.<br>
                        </p>
                        <p>
                            제12조 (준거법 및 관할)<br><br>
                            1. 본 약관의 해석 및 적용은 관련 법령에 따릅니다.<br><br>

                            2. 서비스 이용과 관련하여 분쟁이 발생할 경우, 관할 법원은 회사의 본사 소재지를 관할하는 법원으로 합니다.<br><br>

                            (부칙)<br><br>
                            본 약관은 20XX년 XX월 XX일부터 시행합니다.
                        </p>
                    </div>
                </div>
                <input type="checkbox" name="agree_terms_user_check" id="agreeTermsUserCheck" class="agree_terms_user_check">
            </div>
            
        </div>
        
        <footer>
            <span>Copyright © 2025 플레이데이터 All Rights Reserved.</span>
        </footer>
    </div>
</template>

<script setup>

</script>

<style scoped>
.register-wrapper {
    min-width: 1440px;
    min-height: 98%;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}
.register-format {
    min-width: 458px;
    flex: 1;
}
.all-terms-agree {
    margin-top: 28px;
    margin-bottom: 28px;
}
.agree_terms_user {
    display: flex;
    flex-direction: column;
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
.agree_terms_user_check {
    margin-top: 12px;
    align-self: flex-end;
}
.register-format .terms-box {
    width: 458px;
    overflow: auto;
    box-sizing: border-box;
    max-height: 100px;
    margin: 9px 0 0 32px;
    padding: 15px;
    border-radius: 6px;
    border: 1px solid #d6d6d6;
}.register-format .terms-box::-webkit-scrollbar {
    width: 10px;
  }
  .register-format .terms-box::-webkit-scrollbar-thumb {
    background-color: #2f3542;
    border-radius: 10px;
    background-clip: padding-box;
    border: 2px solid transparent;
  }
  .register-format .terms-box::-webkit-scrollbar-track {
    background-color: grey;
    border-radius: 10px;
    box-shadow: inset 0px 0px 5px white;
  }
footer {
    flex: 0 0 auto;
    bottom: 0;
}
</style>