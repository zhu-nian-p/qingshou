<template>
  <div class="app-router">
    <router-view v-slot="{ Component, route }">
      <transition name="page-fade" mode="out-in">
        <div :key="route.path" class="route-page">
          <component :is="Component" />
        </div>
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import { useOrderSocket } from '@/composables/useOrderSocket';

// 登录后自动建立 WebSocket，订单消息由 ElNotification 弹出
useOrderSocket();

// 移动端输入框聚焦：只在真正被键盘遮挡时轻微滚动，避免页面剧烈跳动
let focusTimer = null;

function onFocusIn(event) {
  const el = event.target;
  if (!(el instanceof HTMLElement)) return;
  if (!el.matches('input, textarea')) return;
  if (!el.closest('.el-dialog__body') && !el.closest('.publish-page')) return;

  clearTimeout(focusTimer);
  focusTimer = setTimeout(() => {
    if (document.activeElement !== el) return;
    const vv = window.visualViewport;
    const viewportHeight = vv ? vv.height : window.innerHeight;
    const rect = el.getBoundingClientRect();
    // 只处理被键盘/视口边缘挡住的输入框
    const obscured = rect.bottom > viewportHeight - 72 || rect.top < 12;
    if (!obscured) return;
    el.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
  }, 320);
}

onMounted(() => document.addEventListener('focusin', onFocusIn));
onUnmounted(() => {
  document.removeEventListener('focusin', onFocusIn);
  clearTimeout(focusTimer);
});
</script>
