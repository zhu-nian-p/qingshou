<template>
  <router-view />
</template>

<script setup>
import { useOrderSocket } from '@/composables/useOrderSocket';

// 登录后自动建立 WebSocket，订单消息由 ElNotification 弹出
useOrderSocket();
</script>
