<template>
  <div class="app-container">
    <Header v-if="isLoginoutPage" />
    <router-view />
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue';
import { useRoute } from 'vue-router';
import { computed } from 'vue';

const route = useRoute()

const isLoginoutPage = computed(() => {
  return route.path !== '/logout' && route.path != '/login' && route.path != '/register'
})



</script>

<style scoped>
.app-container {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding-top: 6rem;
  box-sizing: border-box;
}
</style>
