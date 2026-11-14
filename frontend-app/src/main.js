import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import './styles/base.css';

import App from './App.vue';
import router from './router';
import { onUnauthorized } from './api/http';
import { useUserStore } from './stores/user';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(ElementPlus);

// 任何接口返回 401 时统一清理登录态并跳转登录页
onUnauthorized(() => {
  const user = useUserStore(pinia);
  user.logout();
  if (router.currentRoute.value.path !== '/login') {
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } });
  }
});

app.mount('#app');
