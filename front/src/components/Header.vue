<template>
    <header class="main-header">
        <router-link to="/" custom v-slot="{ navigate }">
            <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" @click="navigate" />
        </router-link>
        <div class="login-button">
            <button type="button" class="btn btn-primary">로그인</button>
        </div>
    </header>
</template>

<script>

</script>

<style scoped>
.main-header {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: fixed;
    top: 0;
    left: 0;
    z-index: 1000;
    background-color: white;
    padding: 1rem 2rem;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.logo-img {
    max-width: 200px;
    height: auto;
    opacity: 1;
    -webkit-transition: .3s ease-in-out;
    transition: .3s ease-in-out;
}

.logo-img:hover {
    opacity: .5;
}
</style>