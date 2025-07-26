<template>

</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  localStorage.removeItem("jwtToken")
  setTimeout(() => {
    router.push('/')
  }, 500)
})
</script>

<style>

</style>