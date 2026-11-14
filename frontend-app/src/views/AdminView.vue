<template>
  <div class="admin-page">
    <header class="topbar">
      <button class="back" @click="router.push('/')">‹</button>
      <div class="brand">青售</div>
      <span class="admin-badge">管理后台</span>
      <div class="top-right">
        <span class="admin-name">{{ user.profile?.username || user.user?.username }}</span>
        <el-button size="small" round @click="logout">退出</el-button>
      </div>
    </header>

    <div class="wrap">
      <div class="stats">
        <div class="stat warn">
          <div class="num">{{ applies.length }}</div>
          <div class="label">待审核置顶申请</div>
        </div>
        <div class="stat">
          <div class="num">{{ users.length }}</div>
          <div class="label">用户总数</div>
        </div>
        <div class="stat danger">
          <div class="num">{{ bannedCount }}</div>
          <div class="label">已封禁用户</div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <el-tabs v-model="tab">
            <el-tab-pane label="置顶审核" name="topup"></el-tab-pane>
            <el-tab-pane label="用户封禁" name="users"></el-tab-pane>
          </el-tabs>
          <span class="hint">仅管理员可见 · 所有操作均会立即生效</span>
        </div>

        <div class="panel-body">
          <div v-show="tab === 'topup'">
            <div class="toolbar">
              <span class="title">待审核申请</span>
              <span class="spacer"></span>
              <el-button size="small" round :loading="loadingApplies" @click="loadApplies">刷新</el-button>
            </div>
            <el-table :data="applies" v-loading="loadingApplies" empty-text="暂无待审核申请" style="width:100%">
              <el-table-column label="商品名称" min-width="180">
                <template #default="{ row }">
                  <div>{{ row.itemTitle || ('商品 #' + row.itemId) }}</div>
                  <div class="muted">商品 ID：{{ row.itemId }}</div>
                </template>
              </el-table-column>
              <el-table-column label="申请人" min-width="150">
                <template #default="{ row }">
                  <div>{{ row.userName || ('用户 #' + row.userId) }}</div>
                  <div class="muted">UID：{{ row.userId }}</div>
                </template>
              </el-table-column>
              <el-table-column label="天数" width="90" align="center">
                <template #default="{ row }">
                  <el-tag type="info" effect="plain" round>{{ row.days }} 天</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="金额" width="110" align="right">
                <template #default="{ row }"><span class="amount">¥{{ formatAmount(row.amount) }}</span></template>
              </el-table-column>
              <el-table-column label="申请时间" width="170">
                <template #default="{ row }"><span class="muted">{{ formatTime(row.createTime) }}</span></template>
              </el-table-column>
              <el-table-column label="操作" width="240" align="right" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" round @click="approve(row)">确认收款并置顶</el-button>
                  <el-button size="small" round @click="reject(row)">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-show="tab === 'users'">
            <div class="toolbar">
              <span class="title">全部用户</span>
              <span class="spacer"></span>
              <el-button size="small" round :loading="loadingUsers" @click="loadUsers">刷新</el-button>
            </div>
            <el-table :data="users" v-loading="loadingUsers" empty-text="暂无用户" style="width:100%">
              <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
              <el-table-column prop="username" label="用户名" min-width="120"></el-table-column>
              <el-table-column prop="realName" label="真实姓名" min-width="100"></el-table-column>
              <el-table-column label="学号" min-width="110">
                <template #default="{ row }"><span class="muted">{{ row.studentId || '—' }}</span></template>
              </el-table-column>
              <el-table-column label="角色" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.role === 2 ? 'warning' : 'info'" effect="light" round>
                    {{ row.role === 2 ? '管理员' : '普通用户' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="creditScore" label="信用分" width="90" align="center"></el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'danger' : 'success'" effect="light" round>
                    {{ row.status === 1 ? '已封禁' : '正常' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="130" align="right" fixed="right">
                <template #default="{ row }">
                  <el-button v-if="row.status !== 1" type="danger" size="small" round :disabled="row.role === 2" @click="ban(row)">封禁</el-button>
                  <el-button v-else type="success" size="small" round @click="unban(row)">解封</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="muted" style="margin-top:10px">管理员账号不可封禁；封禁后该用户将无法登录和交易。</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api } from '@/api/http';
import { useUserStore } from '@/stores/user';
import { formatTime } from '@/utils/media';

const router = useRouter();
const user = useUserStore();

const tab = ref('topup');
const applies = ref([]);
const users = ref([]);
const loadingApplies = ref(false);
const loadingUsers = ref(false);

const bannedCount = computed(() => users.value.filter(u => u.status === 1).length);

function formatAmount(v) {
  const n = Number(v);
  return Number.isFinite(n) ? n.toFixed(2) : v;
}

async function loadApplies() {
  loadingApplies.value = true;
  try {
    applies.value = await api.get('/api/admin/topup/pending');
  } catch (e) {
    ElMessage.error(e.message || '加载待审核申请失败');
  } finally {
    loadingApplies.value = false;
  }
}

async function loadUsers() {
  loadingUsers.value = true;
  try {
    users.value = await api.get('/api/admin/users');
  } catch (e) {
    ElMessage.error(e.message || '加载用户列表失败');
  } finally {
    loadingUsers.value = false;
  }
}

async function approve(row) {
  try {
    await ElMessageBox.confirm(
      `确认已收到 ¥${formatAmount(row.amount)}？通过后商品将置顶 ${row.days} 天。`,
      '确认收款并置顶',
      { confirmButtonText: '确认通过', cancelButtonText: '取消', type: 'warning' }
    );
  } catch (e) {
    return;
  }
  try {
    await api.post('/api/admin/topup/' + row.id + '/approve');
    ElMessage.success('审核通过，商品已置顶');
    await loadApplies();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  }
}

async function reject(row) {
  let reason;
  try {
    const { value } = await ElMessageBox.prompt(
      '请填写拒绝原因（如：金额不符），用户会在「我发布的」看到。',
      '拒绝置顶申请',
      {
        confirmButtonText: '确认拒绝',
        cancelButtonText: '取消',
        inputPlaceholder: '如：金额不符',
        inputValidator: (v) => (v && v.trim() ? true : '拒绝原因不能为空')
      }
    );
    reason = (value || '').trim();
  } catch (e) {
    return;
  }
  try {
    await api.post('/api/admin/topup/' + row.id + '/reject', { reason });
    ElMessage.success('已拒绝申请');
    await loadApplies();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  }
}

async function postUserAction(id, action, successMsg) {
  try {
    await api.post('/api/admin/users/' + id + '/' + action);
    ElMessage.success(successMsg);
    await loadUsers();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  }
}

async function ban(row) {
  if (row.role === 2) return ElMessage.warning('管理员账号不可封禁');
  const name = row.realName || row.username;
  try {
    await ElMessageBox.confirm(
      `确认封禁用户「${name}（${row.username}）」？封禁后该用户无法登录和交易。`,
      '封禁用户',
      { confirmButtonText: '确认封禁', cancelButtonText: '取消', type: 'warning' }
    );
  } catch (e) {
    return;
  }
  postUserAction(row.id, 'ban', '已封禁该用户');
}

async function unban(row) {
  const name = row.realName || row.username;
  try {
    await ElMessageBox.confirm(
      `确认解封用户「${name}（${row.username}）」？`,
      '解封用户',
      { confirmButtonText: '确认解封', cancelButtonText: '取消', type: 'success' }
    );
  } catch (e) {
    return;
  }
  postUserAction(row.id, 'unban', '已解除封禁');
}

function logout() {
  user.logout();
  router.replace('/login');
}

onMounted(() => {
  loadApplies();
  loadUsers();
});
</script>

<style scoped>
.topbar {
  position: sticky; top: 0; z-index: 20; display: flex; align-items: center; gap: 12px;
  padding: 12px 18px; background: rgba(255,255,255,.92); backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--line);
}
.topbar .back { border: none; background: #e6fffa; color: var(--teal-dark); width: 34px; height: 34px; border-radius: 10px; font-size: 16px; cursor: pointer; }
.topbar .brand { font-weight: 800; font-size: 19px; letter-spacing: 2px; }
.admin-badge { font-size: 11px; color: #c2410c; background: #fff7ed; border: 1px solid #fed7aa; border-radius: 999px; padding: 2px 9px; }
.top-right { margin-left: auto; display: flex; align-items: center; gap: 8px; }
.admin-name { font-size: 12px; color: var(--gray); }
.wrap { max-width: 1180px; margin: 0 auto; padding: 18px 16px 48px; }
.stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 18px; }
.stat { background: #fff; border: 1px solid var(--line); border-radius: 16px; padding: 16px 18px; box-shadow: 0 8px 22px rgba(13,148,136,.05); }
.stat .num { font-size: 26px; font-weight: 800; color: var(--teal-dark); line-height: 1.1; }
.stat .label { font-size: 12px; color: #8aa; margin-top: 4px; }
.stat.warn .num { color: #d97706; }
.stat.danger .num { color: #dc2626; }
.panel { background: #fff; border: 1px solid var(--line); border-radius: 18px; box-shadow: 0 10px 30px rgba(13,148,136,.06); overflow: hidden; }
.panel-head { display: flex; align-items: center; gap: 12px; padding: 8px 18px 0; border-bottom: 1px solid var(--line); }
.panel-head .hint { margin-left: auto; font-size: 12px; color: #9bb5b0; }
.panel-body { padding: 14px 18px 18px; }
.toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.toolbar .title { font-size: 14px; font-weight: 700; color: #164e45; }
.toolbar .spacer { margin-left: auto; }
.amount { font-weight: 800; color: var(--teal-dark); }
.muted { color: #8aa; font-size: 12px; }
@media (max-width: 640px) {
  .wrap { padding: 12px 10px 32px; }
  .stats { gap: 8px; }
  .stat { padding: 12px; }
  .stat .num { font-size: 21px; }
  .panel-head { padding: 4px 12px 0; }
  .panel-body { padding: 12px; }
}
</style>
