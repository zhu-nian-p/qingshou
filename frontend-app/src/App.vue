<template>
  <router-view />
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import { useOrderSocket } from '@/composables/useOrderSocket';

// 登录后自动建立 WebSocket，订单消息由 ElNotification 弹出
useOrderSocket();

// 移动端输入框聚焦时，把字段滚动到键盘上方，避免被遮挡
function onFocusIn(event) {
  const el = event.target;
  if (!(el instanceof HTMLElement)) return;
  if (!el.matches('input, textarea')) return;
  const inDialog = !!el.closest('.el-dialog');
  const inPublish = !!el.closest('.publish-page');
  if (!inDialog && !inPublish) return;
  setTimeout(() => {
    el.scrollIntoView({ block: 'center', behavior: 'smooth' });
  }, 300);
}

onMounted(() => document.addEventListener('focusin', onFocusIn));
onUnmounted(() => document.removeEventListener('focusin', onFocusIn));
</script>
