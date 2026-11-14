<template>
  <div class="login-wrap">
    <div class="brand">
      <div class="logo">青售</div>
      <div class="slogan">校园二手 · 同校面交更安心</div>
    </div>

    <div class="card">
      <div class="tabs">
        <button :class="{ active: tab === 'login' }" @click="switchTab('login')">登录</button>
        <button :class="{ active: tab === 'register' }" @click="switchTab('register')">注册</button>
      </div>

      <div v-if="tab === 'login'">
        <div class="field">
          <label>用户名</label>
          <el-input v-model="loginForm.username" size="large" placeholder="请输入用户名" clearable @keyup.enter="doLogin"></el-input>
        </div>
        <div class="field">
          <label>密码</label>
          <el-input v-model="loginForm.password" size="large" type="password" show-password placeholder="请输入密码" @keyup.enter="doLogin"></el-input>
        </div>
        <button class="submit-btn" :disabled="loading" @click="doLogin">{{ loading ? '登录中…' : '登 录' }}</button>
        <div class="foot"><a class="reset-link" @click="resetVisible = true">忘记密码？</a></div>
        <div class="foot">还没有账号？点击上方「注册」，用学校邮箱完成验证</div>
      </div>

      <div v-else>
        <div class="field">
          <label>用户名</label>
          <el-input v-model="regForm.username" size="large" placeholder="4-20 位字母、数字或下划线" clearable></el-input>
        </div>
        <div class="field">
          <label>密码</label>
          <el-input v-model="regForm.password" size="large" type="password" show-password placeholder="6-32 位密码"></el-input>
        </div>
        <div class="field">
          <label>真实姓名</label>
          <el-input v-model="regForm.realName" size="large" placeholder="用于线下交易核验" clearable></el-input>
        </div>
        <div class="field">
          <label>学号</label>
          <el-input v-model="regForm.studentId" size="large" placeholder="8-9 位数字" maxlength="9" clearable></el-input>
        </div>
        <div class="field">
          <label>学校邮箱</label>
          <el-input v-model="regForm.schoolEmail" size="large" placeholder="如：20240001@xxx.edu.cn" clearable></el-input>
        </div>
        <div class="field">
          <label>邮箱验证码</label>
          <div class="code-row">
            <el-input v-model="regForm.emailCode" size="large" placeholder="6 位验证码" maxlength="6" clearable @keyup.enter="doRegister"></el-input>
            <el-button class="code-btn" size="large" :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? countdown + 's 后重发' : '获取验证码' }}
            </el-button>
          </div>
        </div>
        <button class="submit-btn" :disabled="loading" @click="doRegister">{{ loading ? '注册中…' : '注 册' }}</button>
        <div class="foot">注册即表示同意青售校园二手交易规范</div>
      </div>
    </div>

    <div class="foot" style="margin-top:14px">青售 · 只做校园里的好物</div>

    <el-dialog v-model="resetVisible" title="重置密码" width="380px" align-center>
      <div class="field">
        <label>学校邮箱</label>
        <el-input v-model="resetForm.email" size="large" placeholder="如：20240001@xxx.edu.cn" clearable></el-input>
      </div>
      <div class="field">
        <label>邮箱验证码</label>
        <div class="code-row">
          <el-input v-model="resetForm.emailCode" size="large" maxlength="6" placeholder="6 位验证码" clearable></el-input>
          <el-button class="code-btn" size="large" :disabled="resetCountdown > 0" @click="sendResetCode">
            {{ resetCountdown > 0 ? resetCountdown + 's 后重发' : '获取验证码' }}
          </el-button>
        </div>
      </div>
      <div class="field">
        <label>新密码</label>
        <el-input v-model="resetForm.newPassword" type="password" show-password size="large" placeholder="6-32 位新密码"></el-input>
      </div>
      <div class="field">
        <label>确认新密码</label>
        <el-input v-model="resetForm.confirmPassword" type="password" show-password size="large" placeholder="再次输入新密码"></el-input>
      </div>
      <template #footer>
        <el-button round @click="resetVisible = false">取消</el-button>
        <el-button type="primary" round :loading="resetLoading" @click="submitReset">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { api } from '@/api/http';
import { useUserStore } from '@/stores/user';

const EMAIL_RE = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.edu\.cn$/;
const router = useRouter();
const route = useRoute();
const user = useUserStore();

const tab = ref('login');
const loading = ref(false);
const countdown = ref(0);
const resetCountdown = ref(0);
let timer = null;
let resetTimer = null;

const loginForm = reactive({ username: '', password: '' });
const regForm = reactive({
  username: '', password: '', realName: '',
  studentId: '', schoolEmail: '', emailCode: ''
});

const resetVisible = ref(false);
const resetLoading = ref(false);
const resetForm = reactive({ email: '', emailCode: '', newPassword: '', confirmPassword: '' });

function switchTab(t) {
  tab.value = t;
  loading.value = false;
}

function startCountdown(target, timerRef) {
  target.value = 60;
  if (timerRef === 'timer' && timer) clearInterval(timer);
  if (timerRef === 'reset' && resetTimer) clearInterval(resetTimer);
  const run = setInterval(() => {
    target.value -= 1;
    if (target.value <= 0) {
      clearInterval(run);
      target.value = 0;
    }
  }, 1000);
  if (timerRef === 'timer') timer = run;
  else resetTimer = run;
}

onUnmounted(() => {
  if (timer) clearInterval(timer);
  if (resetTimer) clearInterval(resetTimer);
});

async function sendCode() {
  if (countdown.value > 0) return;
  const email = (regForm.schoolEmail || '').trim();
  if (!EMAIL_RE.test(email)) {
    ElMessage.warning('请输入 .edu.cn 结尾的学校邮箱');
    return;
  }
  try {
    await api.post('/api/user/send-code', { email });
    ElMessage.success('验证码已发送，请查收学校邮箱（5 分钟内有效）');
    startCountdown(countdown, 'timer');
  } catch (e) {
    ElMessage.error(e.message);
  }
}

async function doLogin() {
  if (!loginForm.username.trim() || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  loading.value = true;
  try {
    await user.login(loginForm.username.trim(), loginForm.password);
    ElMessage.success('登录成功，正在进入青售…');
    router.push(route.query.redirect || '/');
  } catch (e) {
    ElMessage.error(e.message);
  } finally {
    loading.value = false;
  }
}

async function doRegister() {
  const f = regForm;
  if (!/^[A-Za-z0-9_]{4,20}$/.test(f.username.trim())) {
    return ElMessage.warning('用户名需为 4-20 位字母、数字或下划线');
  }
  if (!f.password || f.password.length < 6 || f.password.length > 32) {
    return ElMessage.warning('密码长度需为 6-32 位');
  }
  if (!f.realName.trim()) return ElMessage.warning('请填写真实姓名');
  if (!/^\d{8,9}$/.test(f.studentId.trim())) return ElMessage.warning('学号必须是 8-9 位数字');
  if (!EMAIL_RE.test(f.schoolEmail.trim())) return ElMessage.warning('请使用 .edu.cn 结尾的学校邮箱');
  if (!/^\d{6}$/.test(f.emailCode.trim())) return ElMessage.warning('请输入 6 位邮箱验证码');

  loading.value = true;
  try {
    await api.post('/api/user/register', {
      username: f.username.trim(),
      password: f.password,
      realName: f.realName.trim(),
      studentId: f.studentId.trim(),
      schoolEmail: f.schoolEmail.trim(),
      emailCode: f.emailCode.trim()
    });
    await user.login(f.username.trim(), f.password);
    ElMessage.success('注册成功，正在进入青售…');
    router.push('/');
  } catch (e) {
    ElMessage.error(e.message);
  } finally {
    loading.value = false;
  }
}

async function sendResetCode() {
  if (resetCountdown.value > 0) return;
  const email = (resetForm.email || '').trim();
  if (!EMAIL_RE.test(email)) {
    ElMessage.warning('请输入 .edu.cn 结尾的学校邮箱');
    return;
  }
  try {
    await api.post('/api/user/send-reset-code', { email });
    ElMessage.success('验证码已发送，请查收学校邮箱（5 分钟内有效）');
    startCountdown(resetCountdown, 'reset');
  } catch (e) {
    ElMessage.error(e.message);
  }
}

async function submitReset() {
  const f = resetForm;
  if (!EMAIL_RE.test(f.email.trim())) return ElMessage.warning('请输入 .edu.cn 结尾的学校邮箱');
  if (!/^\d{6}$/.test(f.emailCode.trim())) return ElMessage.warning('请输入 6 位邮箱验证码');
  if (!f.newPassword || f.newPassword.length < 6 || f.newPassword.length > 32) {
    return ElMessage.warning('新密码长度需为 6-32 位');
  }
  if (f.newPassword !== f.confirmPassword) return ElMessage.warning('两次输入的新密码不一致');

  resetLoading.value = true;
  try {
    await api.post('/api/user/reset-password', {
      email: f.email.trim(),
      emailCode: f.emailCode.trim(),
      newPassword: f.newPassword
    });
    ElMessage.success('密码重置成功，请用新密码登录');
    resetVisible.value = false;
    tab.value = 'login';
    loginForm.password = '';
  } catch (e) {
    ElMessage.error(e.message);
  } finally {
    resetLoading.value = false;
  }
}
</script>

<style scoped>
.login-wrap {
  width: 100%; max-width: 430px; margin: 0 auto;
  padding: 24px 16px; min-height: 100vh; display: flex; flex-direction: column; justify-content: center;
}
.brand { text-align: center; margin-bottom: 18px; }
.brand .logo { font-size: 38px; letter-spacing: 4px; }
.slogan { margin-top: 6px; font-size: 13px; color: var(--gray); letter-spacing: 1px; }
.card {
  background: rgba(255,255,255,.94); border: 1px solid #d9f3ee; border-radius: 22px;
  box-shadow: 0 18px 50px rgba(13,148,136,.12); padding: 22px 22px 24px;
}
.tabs { display: flex; background: #ecfdf9; border-radius: 14px; padding: 5px; margin-bottom: 20px; }
.tabs button {
  flex: 1; border: none; background: transparent; color: var(--gray);
  font-size: 15px; font-weight: 600; padding: 10px 0; border-radius: 10px; cursor: pointer; transition: all .18s ease;
}
.tabs button.active { background: #fff; color: var(--teal-dark); box-shadow: 0 4px 12px rgba(13,148,136,.15); }
.field { margin-bottom: 14px; }
.field label { display: block; font-size: 13px; color: #36635c; margin-bottom: 6px; font-weight: 600; }
.code-row { display: flex; gap: 10px; }
.code-row .el-input { flex: 1 1 auto; }
.code-btn {
  flex: 0 0 auto; min-width: 112px; border-radius: 12px; border: none; color: #fff;
  background: linear-gradient(135deg, #14b8a6, #0d9488); font-weight: 600;
}
.submit-btn {
  width: 100%; height: 46px; margin-top: 6px; border: none; border-radius: 14px;
  background: linear-gradient(135deg, #14b8a6, #0d9488); color: #fff;
  font-size: 16px; font-weight: 700; letter-spacing: 1px; cursor: pointer;
  box-shadow: 0 10px 22px rgba(13,148,136,.25);
}
.submit-btn[disabled] { opacity: .65; cursor: not-allowed; }
.foot { margin-top: 18px; text-align: center; font-size: 12px; color: #9bb5b0; }
.reset-link { color: var(--teal-dark); font-size: 13px; cursor: pointer; text-decoration: none; }
.reset-link:hover { text-decoration: underline; }
@media (max-width: 480px) {
  .card { padding: 18px 16px 20px; border-radius: 18px; }
  .brand .logo { font-size: 32px; }
}
</style>
