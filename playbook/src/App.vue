<template>
  <div class="app-container">
    <Header />
    <router-view />
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue';
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
