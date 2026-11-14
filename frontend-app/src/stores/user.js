import { defineStore } from 'pinia';
import { api } from '@/api/http';

const TOKEN_KEY = 'qingshou_token';
const USER_KEY = 'qingshou_user';

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null');
  } catch (e) {
    return null;
  }
}

function persistUser(user) {
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
  else localStorage.removeItem(USER_KEY);
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readUser(),
    profile: null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => (state.profile?.role ?? state.user?.role) === 2,
    displayName: (state) =>
      state.profile?.realName || state.user?.realName || state.user?.username || '青售用户',
    avatarText(state) {
      return String(this.displayName).slice(0, 1).toUpperCase();
    }
  },

  actions: {
    setSession(data) {
      this.token = data.token;
      this.user = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        role: data.role
      };
      localStorage.setItem(TOKEN_KEY, this.token);
      persistUser(this.user);
    },

    async login(username, password) {
      const data = await api.post('/api/user/login', { username, password });
      this.setSession(data);
      return data;
    },

    async fetchMe() {
      const profile = await api.get('/api/user/me');
      this.profile = profile;
      if (profile) {
        this.user = {
          ...(this.user || {}),
          userId: profile.id,
          username: profile.username,
          realName: profile.realName,
          role: profile.role
        };
        persistUser(this.user);
      }
      return profile;
    },

    logout() {
      this.token = '';
      this.user = null;
      this.profile = null;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  }
});
