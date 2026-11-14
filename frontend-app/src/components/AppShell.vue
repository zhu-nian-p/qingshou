<template>
  <aside class="sidebar" v-if="!isMobile">
    <div class="side-brand">
      <div class="logo">青售</div>
      <div class="tagline">校园二手 · 清爽交易</div>
    </div>
    <button class="publish-btn" @click="router.push('/publish')">＋ 发布闲置</button>
    <nav class="side-nav">
      <button v-for="n in navs" :key="n.key" :class="{ active: active === n.key }" @click="router.push(n.path)">
        <span class="ico">{{ n.icon }}</span>{{ n.label }}
      </button>
    </nav>
    <div class="side-user" @click="router.push('/profile')">
      <div class="avatar">{{ avatarText }}</div>
      <div class="su-info">
        <div class="su-name">{{ displayName }}</div>
        <div class="su-credit">信用分 {{ credit }}</div>
      </div>
      <button class="su-logout" title="退出登录" @click.stop="logout">⏏</button>
    </div>
  </aside>

  <header class="mobile-header" v-if="isMobile">
    <div class="logo">青售</div>
    <div class="mh-right">
      <span class="campus-chip">校园二手</span>
      <div class="avatar small" @click="router.push('/profile')">{{ avatarText }}</div>
    </div>
  </header>

  <main :class="['main', { mobile: isMobile }]">
    <slot />
    <div class="site-footer">{{ disclaimer }}</div>
  </main>

  <nav class="bottom-nav" v-if="isMobile">
    <button v-for="n in navs" :key="n.key" :class="{ active: active === n.key }" @click="router.push(n.path)">
      <span class="ico">{{ n.icon }}</span><span class="lbl">{{ n.label }}</span>
    </button>
  </nav>

  <button class="fab" v-if="isMobile && active === 'home'" @click="router.push('/publish')">＋</button>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { DISCLAIMER } from '@/constants/legal';

defineProps({
  active: { type: String, default: 'home' }
});

const disclaimer = DISCLAIMER;

const router = useRouter();
const user = useUserStore();
const isMobile = ref(window.innerWidth < 768);

const navs = [
  { key: 'home', label: '主页', icon: '🏠', path: '/' },
  { key: 'messages', label: '消息', icon: '💬', path: '/messages' },
  { key: 'profile', label: '我的', icon: '👤', path: '/profile' }
];

const displayName = computed(() => user.displayName);
const avatarText = computed(() => user.avatarText);
const credit = computed(() => user.profile?.creditScore ?? user.user?.creditScore ?? '—');

function logout() {
  user.logout();
  router.replace('/login');
}

function onResize() {
  isMobile.value = window.innerWidth < 768;
}

onMounted(() => {
  window.addEventListener('resize', onResize);
  if (user.isLoggedIn && !user.profile) {
    user.fetchMe().catch(() => {});
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', onResize);
});
</script>
