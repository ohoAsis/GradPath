<template>
  <div>
    <RoleSwitcher />
    <router-view />
  </div>
</template>

<script setup lang="ts">
import RoleSwitcher from './components/RoleSwitcher.vue'
</script>
