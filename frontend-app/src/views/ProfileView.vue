<template>
  <AppShell active="profile">
    <h2 class="page-title">我的</h2>

    <div class="profile-card">
      <div class="avatar big">{{ avatarText }}</div>
      <div class="p-info">
        <div class="p-name">
          {{ profile.realName || '青售用户' }}
          <span class="role-tag" v-if="profile.role === 2">管理员</span>
        </div>
        <div class="p-sub">@{{ profile.username || '—' }}</div>
        <div class="p-meta">
          <span>学号 {{ profile.studentId || '—' }}</span>
          <span>{{ profile.schoolEmail || '未绑定邮箱' }}</span>
        </div>
      </div>
      <div class="credit-box">
        <div class="credit-num">{{ profile.creditScore ?? '—' }}</div>
        <div class="credit-label">信用分</div>
      </div>
    </div>

    <div class="segments">
      <button v-for="s in segments" :key="s.key" :class="{ active: segment === s.key }" @click="segment = s.key">
        {{ s.label }}<span class="seg-count">{{ segmentCount(s.key) }}</span>
      </button>
    </div>

    <SkeletonGrid v-if="loadingProfile" :count="4" />

    <div v-else-if="segment === 'items'">
      <div v-if="myItems.length === 0" class="state-box small">
        <div class="emoji">📦</div>
        <div class="state-desc">还没发布过商品，去首页发第一个吧～</div>
      </div>
      <div v-else class="mini-list">
        <div class="mini-item" v-for="it in myItems" :key="it.id">
          <img :src="imgSrc(it.imageUrl)" class="mini-thumb" alt="" @error="onImgError">
          <div class="mini-body">
            <div class="mini-title">{{ it.title }}</div>
            <div class="mini-sub">¥{{ formatPrice(it.price) }} · {{ it.campus || '前湖' }}校区</div>
            <div class="mini-topup" v-if="topupMap[it.id]" :class="'tu' + topupMap[it.id].status">
              {{ topupText(topupMap[it.id]) }}
            </div>
          </div>
          <div class="mini-actions">
            <span class="top-chip" v-if="it.isTop === 1">🔥 置顶中</span>
            <span class="mini-flag" :class="'s' + it.status">{{ itemStatusText(it.status) }}</span>
            <el-button v-if="it.status === 0" size="small" round @click="openEdit(it)">编辑</el-button>
            <el-button v-if="it.status === 0" size="small" round @click="offlineItem(it)">下架</el-button>
            <el-button v-if="it.status === 0" size="small" round @click="openTopup(it)">申请置顶</el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-else>
      <div v-if="currentOrders.length === 0" class="state-box small">
        <div class="emoji">🧾</div>
        <div class="state-desc">{{ segment === 'bought' ? '还没有买到的宝贝' : '还没有卖出的宝贝' }}</div>
      </div>
      <div v-else class="order-list">
        <div class="order" v-for="o in currentOrders" :key="o.id">
          <div class="order-head">
            <span class="order-title">{{ o.itemTitle }}</span>
            <span class="order-status" :class="'os' + o.status">{{ orderStatusText(o) }}</span>
          </div>
          <div class="order-meta">
            <span>¥{{ formatPrice(o.itemPrice) }}</span>
            <span>📍 {{ o.tradeLocation }}</span>
            <span>{{ formatTime(o.createTime) }}</span>
          </div>
          <div class="order-actions" v-if="canAct(o)">
            <template v-if="segment === 'bought'">
              <el-button v-if="o.status === 0" size="small" type="primary" round @click="completeTrade(o)">确认完成交易</el-button>
              <el-button size="small" round @click="cancelOrder(o)">取消订单</el-button>
            </template>
            <template v-else-if="o.status === 3">
              <el-button size="small" type="primary" round @click="confirmTrade(o)">同意面交</el-button>
              <el-button size="small" round @click="rejectTrade(o)">拒绝接单</el-button>
            </template>
            <template v-else-if="o.status === 4">
              <el-button size="small" type="success" round @click="sellerConfirmComplete(o)">确认完成交易</el-button>
            </template>
          </div>
          <div class="order-actions" v-if="segment === 'bought' && o.status === 1">
            <el-button v-if="!reviewedMap[o.id]" size="small" type="success" round @click="openReview(o)">评价卖家</el-button>
            <span v-else class="reviewed-tag">已评价</span>
          </div>
        </div>
      </div>
    </div>

    <button class="logout-btn" @click="logout">退出登录</button>
    <el-button v-if="user.isAdmin" class="admin-entry" round @click="router.push('/admin')">🛠 置顶审核后台</el-button>
    <div class="version">青售 v0.2 · Vite + Vue3 工程化版本</div>

    <TopUpDialog v-model="topupVisible" :item="topupItem" @submitted="loadProfile" />
    <EditItemDialog v-model="editVisible" :item="editItem" @saved="loadProfile" />
    <ReviewDialog v-model="reviewVisible" :order="reviewOrder" @submitted="loadProfile" />
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import AppShell from '@/components/AppShell.vue';
import TopUpDialog from '@/components/TopUpDialog.vue';
import EditItemDialog from '@/components/EditItemDialog.vue';
import ReviewDialog from '@/components/ReviewDialog.vue';
import SkeletonGrid from '@/components/SkeletonGrid.vue';
import { api } from '@/api/http';
import { useUserStore } from '@/stores/user';
import { imgSrc, onImgError, formatPrice, formatTime, itemStatusText } from '@/utils/media';

const router = useRouter();
const user = useUserStore();

const profile = ref({});
const myItems = ref([]);
const bought = ref([]);
const sold = ref([]);
const loadingProfile = ref(true);
const topupMap = ref({});
const reviewedMap = ref({});
const segment = ref('items');

const topupVisible = ref(false);
const topupItem = ref(null);
const editVisible = ref(false);
const editItem = ref(null);
const reviewVisible = ref(false);
const reviewOrder = ref(null);

const segments = [
  { key: 'items', label: '我发布的' },
  { key: 'bought', label: '我买到的' },
  { key: 'sold', label: '我卖出的' }
];

const avatarText = computed(() => String(profile.value.realName || profile.value.username || '青').slice(0, 1).toUpperCase());
const currentOrders = computed(() => (segment.value === 'bought' ? bought.value : sold.value));

function orderGroup(status) {
  return status === 3 || status === 0 ? 0 : 1;
}
function orderTime(order) {
  const t = order && order.createTime ? String(order.createTime) : '';
  const ts = new Date(t.replace('T', ' ')).getTime();
  return Number.isFinite(ts) ? ts : 0;
}
function sortOrders(list) {
  return [...list].sort((a, b) => {
    const diff = orderGroup(a.status) - orderGroup(b.status);
    return diff !== 0 ? diff : orderTime(b) - orderTime(a);
  });
}

function orderStatusText(o) {
  if (segment.value === 'sold' && o.status === 3) return '待确认';
  return o.statusText || '';
}
function canAct(o) {
  if (segment.value === 'bought') return o.status === 0 || o.status === 3;
  if (segment.value === 'sold') return o.status === 3 || o.status === 4;
  return false;
}
function segmentCount(key) {
  if (key === 'items') return myItems.value.length;
  if (key === 'bought') return bought.value.length;
  return sold.value.length;
}

async function loadProfile() {
  loadingProfile.value = true;
  try {
    const [me, mine, trades] = await Promise.all([
      api.get('/api/user/me'),
      api.get('/api/user/me/items'),
      api.get('/api/user/me/trades')
    ]);
    profile.value = me || {};
    user.profile = me || {};
    myItems.value = mine || [];
    bought.value = sortOrders((trades && trades.bought) || []);
    sold.value = sortOrders((trades && trades.sold) || []);

    try {
      const applies = await api.get('/api/topup/my');
      const map = {};
      (applies || []).forEach((a) => { if (!map[a.itemId]) map[a.itemId] = a; });
      topupMap.value = map;
    } catch (e) { /* 置顶状态失败不影响主流程 */ }

    try {
      const reviews = await api.get('/api/review/mine');
      const reviewed = {};
      (reviews || []).forEach((r) => { reviewed[r.orderId] = true; });
      reviewedMap.value = reviewed;
    } catch (e) { /* 评价状态失败不影响主流程 */ }
  } catch (e) {
    ElMessage.error(e.message || '加载失败');
  } finally {
    loadingProfile.value = false;
  }
}

function topupText(apply) {
  if (!apply) return '';
  if (apply.status === 0) return '置顶申请：待审核';
  if (apply.status === 1) return '置顶申请：已通过';
  return '置顶申请已拒绝：' + (apply.rejectReason || '未填写原因');
}

function openTopup(item) {
  topupItem.value = item;
  topupVisible.value = true;
}

function openEdit(item) {
  editItem.value = item;
  editVisible.value = true;
}

function openReview(order) {
  reviewOrder.value = order;
  reviewVisible.value = true;
}

async function offlineItem(item) {
  try {
    await ElMessageBox.confirm(
      `确认下架「${item.title}」？下架后买家将看不到该商品。`,
      '下架商品',
      { confirmButtonText: '确认下架', cancelButtonText: '取消', type: 'warning' }
    );
  } catch (e) {
    return;
  }
  try {
    await api.post('/api/item/' + item.id + '/offline');
    ElMessage.success('商品已下架');
    await loadProfile();
  } catch (e) {
    ElMessage.error(e.message || '下架失败');
  }
}

async function postOrderAction(order, action, successMsg) {
  try {
    await api.post('/api/order/' + order.id + '/' + action);
    ElMessage.success(successMsg);
    await loadProfile();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  }
}

function cancelOrder(o) {
  ElMessageBox.confirm('确定取消这笔订单吗？取消后商品会恢复在售。', '取消订单', {
    confirmButtonText: '确认取消',
    cancelButtonText: '再想想',
    type: 'warning'
  }).then(() => postOrderAction(o, 'buyer-cancel', '订单已取消，商品已恢复在售')).catch(() => {});
}

function confirmTrade(o) {
  postOrderAction(o, 'seller-confirm', '已同意面交，等待买家确认');
}

function rejectTrade(o) {
  ElMessageBox.confirm('拒绝后商品会恢复在售，确定拒绝这笔订单吗？', '拒绝接单', {
    confirmButtonText: '确定拒绝',
    cancelButtonText: '再想想',
    type: 'warning'
  }).then(() => postOrderAction(o, 'seller-reject', '已拒绝接单，商品已恢复在售')).catch(() => {});
}

function completeTrade(o) {
  ElMessageBox.confirm('确认已完成面交？确认后需卖家确认完成，交易才正式生效、双方信用分 +1。', '确认面交', {
    confirmButtonText: '确认已面交',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => postOrderAction(o, 'complete', '已确认面交，等待卖家确认完成')).catch(() => {});
}

function sellerConfirmComplete(o) {
  ElMessageBox.confirm('确认与买家完成交易？确认后商品售出，双方信用分 +1。', '确认完成交易', {
    confirmButtonText: '确认完成',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => postOrderAction(o, 'seller-complete', '交易完成，双方信用分 +1')).catch(() => {});
}

function logout() {
  user.logout();
  router.replace('/login');
}

onMounted(loadProfile);
</script>
