import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

const routes = [
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { guest: true } },
  { path: '/', name: 'home', component: () => import('@/views/HomeView.vue'), meta: { requiresAuth: true } },
  { path: '/messages', name: 'messages', component: () => import('@/views/MessagesView.vue'), meta: { requiresAuth: true } },
  { path: '/profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { requiresAuth: true } },
  { path: '/publish', name: 'publish', component: () => import('@/views/PublishView.vue'), meta: { requiresAuth: true } },
  { path: '/detail/:id', name: 'detail', component: () => import('@/views/DetailView.vue') },
  { path: '/admin', name: 'admin', component: () => import('@/views/AdminView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

router.beforeEach((to) => {
  const user = useUserStore();

  if (to.meta.requiresAuth && !user.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresAdmin && !user.isAdmin) {
    ElMessage.warning('需要管理员账号才能访问该页面');
    return { path: '/' };
  }
  if (to.meta.guest && user.isLoggedIn) {
    return { path: '/' };
  }
  return true;
});

export default router;
