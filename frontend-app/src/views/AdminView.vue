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
      <!-- 数据看板 -->
      <div class="dash" v-loading="loadingStats">
        <div class="dash-card">
          <div class="dash-label">总用户数</div>
          <div class="dash-num"><AnimatedNumber :value="stats.totalUsers" /></div>
        </div>
        <div class="dash-card">
          <div class="dash-label">今日新增用户</div>
          <div class="dash-num accent"><AnimatedNumber :value="stats.todayNewUsers" /></div>
        </div>
        <div class="dash-card">
          <div class="dash-label">在售商品</div>
          <div class="dash-num"><AnimatedNumber :value="stats.onSaleItems" /></div>
        </div>
        <div class="dash-card">
          <div class="dash-label">今日发布商品</div>
          <div class="dash-num accent"><AnimatedNumber :value="stats.todayPublishedItems" /></div>
        </div>
        <div class="dash-card">
          <div class="dash-label">已完成交易流水</div>
          <div class="dash-num amount"><AnimatedNumber :value="stats.completedAmount" :decimals="2" prefix="¥" /></div>
        </div>
      </div>

      <!-- 近 7 天发布趋势 -->
      <div class="chart-panel">
        <div class="chart-head">
          <span>近 7 天发布商品趋势</span>
          <span class="chart-sub">待审核置顶 {{ applies.length }} 条 · 待处理举报 {{ reports.length }} 条 · 已封禁 {{ bannedCount }} 人</span>
        </div>
        <div ref="chartEl" class="chart"></div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <el-tabs v-model="tab">
            <el-tab-pane label="置顶审核" name="topup"></el-tab-pane>
            <el-tab-pane label="举报处理" name="reports"></el-tab-pane>
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
            <el-skeleton v-if="loadingApplies" :rows="5" animated />
            <el-table v-else :data="applies" empty-text="暂无待审核申请" style="width:100%">
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

          <div v-show="tab === 'reports'">
            <div class="toolbar">
              <span class="title">待处理举报</span>
              <span class="spacer"></span>
              <el-button size="small" round :loading="loadingReports" @click="loadReports">刷新</el-button>
            </div>
            <el-skeleton v-if="loadingReports" :rows="5" animated />
            <el-table v-else :data="reports" empty-text="暂无待处理举报" style="width:100%">
              <el-table-column label="举报商品" min-width="180">
                <template #default="{ row }">
                  <div>{{ row.itemTitle || ('商品 #' + row.itemId) }}</div>
                  <div class="muted">举报人：{{ row.reporterName || ('用户 #' + row.reporterId) }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="举报原因" min-width="220" show-overflow-tooltip></el-table-column>
              <el-table-column label="举报时间" width="170">
                <template #default="{ row }"><span class="muted">{{ formatTime(row.createTime) }}</span></template>
              </el-table-column>
              <el-table-column label="操作" width="200" align="right" fixed="right">
                <template #default="{ row }">
                  <el-button type="danger" size="small" round @click="handleReport(row, 'delist')">下架商品</el-button>
                  <el-button size="small" round @click="handleReport(row, 'dismiss')">忽略</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="muted" style="margin-top:10px">「下架商品」会把商品置为已下架并取消置顶，同时写入管理员审计日志。</div>
          </div>

          <div v-show="tab === 'users'">
            <div class="toolbar">
              <span class="title">全部用户</span>
              <span class="spacer"></span>
              <el-button size="small" round :loading="loadingUsers" @click="loadUsers">刷新</el-button>
            </div>
            <el-skeleton v-if="loadingUsers" :rows="5" animated />
            <el-table v-else :data="users" empty-text="暂无用户" style="width:100%">
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
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';
import { api } from '@/api/http';
import { useUserStore } from '@/stores/user';
import { formatTime } from '@/utils/media';
import AnimatedNumber from '@/components/AnimatedNumber.vue';

const router = useRouter();
const user = useUserStore();

const tab = ref('topup');
const applies = ref([]);
const reports = ref([]);
const users = ref([]);
const loadingApplies = ref(false);
const loadingReports = ref(false);
const loadingUsers = ref(false);

const stats = ref({
  totalUsers: 0,
  todayNewUsers: 0,
  onSaleItems: 0,
  todayPublishedItems: 0,
  completedAmount: 0,
  trend: []
});
const loadingStats = ref(false);
const chartEl = ref(null);
let chartInstance = null;

const bannedCount = computed(() => users.value.filter(u => u.status === 1).length);

async function loadStats() {
  loadingStats.value = true;
  try {
    stats.value = await api.get('/api/admin/stats');
    await nextTick();
    renderChart();
  } catch (e) {
    ElMessage.error(e.message || '加载数据看板失败');
  } finally {
    loadingStats.value = false;
  }
}

function renderChart() {
  if (!chartEl.value) return;
  if (!chartInstance) {
    chartInstance = echarts.init(chartEl.value);
  }
  const trend = stats.value.trend || [];
  const labels = trend.map(t => t.label);
  const counts = trend.map(t => t.count);
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 44, right: 24, top: 28, bottom: 32 },
    xAxis: {
      type: 'category',
      data: labels,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#cfe9e4' } },
      axisLabel: { color: '#8aa' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#eef7f4' } },
      axisLabel: { color: '#8aa' }
    },
    series: [{
      name: '发布商品',
      type: 'line',
      smooth: true,
      data: counts,
      symbolSize: 7,
      lineStyle: { width: 3, color: '#14b8a6' },
      itemStyle: { color: '#14b8a6' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(20,184,166,.35)' },
          { offset: 1, color: 'rgba(20,184,166,0)' }
        ])
      }
    }]
  });
}

function onResize() {
  if (chartInstance) chartInstance.resize();
}

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

async function loadReports() {
  loadingReports.value = true;
  try {
    reports.value = await api.get('/api/admin/reports');
  } catch (e) {
    ElMessage.error(e.message || '加载举报列表失败');
  } finally {
    loadingReports.value = false;
  }
}

async function handleReport(row, action) {
  if (action === 'delist') {
    try {
      await ElMessageBox.confirm(
        `确认下架「${row.itemTitle || '该商品'}」？该商品将变为已下架并取消置顶。`,
        '下架违规商品',
        { confirmButtonText: '确认下架', cancelButtonText: '取消', type: 'warning' }
      );
    } catch (e) {
      return;
    }
  }
  try {
    await api.post('/api/admin/reports/' + row.id + '/handle', { action });
    ElMessage.success(action === 'delist' ? '已下架违规商品' : '已忽略该举报');
    await loadReports();
    if (action === 'delist') {
      await Promise.all([loadApplies(), loadUsers(), loadStats()]);
    }
  } catch (e) {
    ElMessage.error(e.message || '处理失败');
  }
}

async function loadUsers() {  loadingUsers.value = true;
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
  loadStats();
  loadApplies();
  loadReports();
  loadUsers();
  window.addEventListener('resize', onResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', onResize);
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
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
.dash {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.dash-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 16px 18px;
  box-shadow: 0 8px 22px rgba(13,148,136,.05);
}
.dash-label { font-size: 12px; color: #8aa; }
.dash-num {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 800;
  color: var(--teal-dark);
  line-height: 1.1;
}
.dash-num.accent { color: #0d9488; }
.dash-num.amount { color: #d97706; font-size: 23px; }
.chart-panel {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(13,148,136,.06);
  margin-bottom: 18px;
  padding: 16px 18px 8px;
}
.chart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #164e45;
}
.chart-sub { font-size: 12px; font-weight: 400; color: #9bb5b0; }
.chart { width: 100%; height: 240px; }
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
  .dash { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
  .dash-card { padding: 12px; }
  .dash-num { font-size: 21px; }
  .dash-card:last-child { grid-column: span 2; }
  .panel-head { padding: 4px 12px 0; }
  .panel-body { padding: 12px; }
}
</style>
