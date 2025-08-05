<template>

</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  localStorage.removeItem("jwtToken")
  localStorage.removeItem("userType")
  setTimeout(() => {
    router.push('/')
  }, 500)
})
</script>

<style>

</style>