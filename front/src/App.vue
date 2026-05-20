<template>
  <div class="app-container">
    <Header v-if="isLoginoutPage" />
    <router-view />
    <Footer />
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue';
import Footer from './components/Footer.vue';
import { useRoute } from 'vue-router';
import { computed } from 'vue';

const route = useRoute()

const isLoginoutPage = computed(() => {
  return route.path !== '/logout' && route.path != '/login' && route.path!='/terms' && route.path != '/register'
})

</script>

<style scoped>
.app-container {
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding-top: var(--pb-header-height);
  box-sizing: border-box;
  background: var(--pb-color-canvas);
}
</style>
