<template>
  <div class="detail-page">
    <header class="topbar">
      <button class="back" @click="goBack">‹</button>
      <h1>商品详情</h1>
      <div class="logo">青售</div>
    </header>

    <div class="wrap" v-if="loading">
      <el-skeleton :rows="6" animated style="margin-top:20px"></el-skeleton>
    </div>

    <div class="wrap" v-else-if="error">
      <div class="state-box">
        <div class="emoji">😥</div>
        <div class="state-title">商品加载失败</div>
        <div class="state-desc">{{ error }}</div>
        <el-button type="primary" round @click="router.push('/')">返回首页</el-button>
      </div>
    </div>

    <div class="wrap" v-else>
      <div class="gallery">
        <img :src="imgSrc(item.imageUrl)" :alt="item.title" @error="onImgError">
        <div class="badges">
          <span v-if="item.isTop === 1" class="badge top">🔥 急售</span>
          <span v-if="(item.sellerCredit || 0) >= 110" class="badge credit">🏅 高信誉</span>
        </div>
      </div>

      <div class="panel">
        <div class="price-row">
          <span class="price"><i>¥</i>{{ formatPrice(item.price) }}</span>
          <span class="tag" v-if="item.status !== 0">{{ statusText }}</span>
        </div>
        <h2 class="title">{{ item.title }}</h2>
        <div class="tags">
          <span class="tag" v-if="item.category">🏷 {{ item.category }}</span>
          <span class="tag" v-if="item.campus">📍 {{ item.campus }}校区</span>
          <span class="tag" v-if="item.isTop === 1">🔥 急售中</span>
        </div>

        <div class="section-label">商品描述</div>
        <div class="desc">{{ item.description || '卖家很懒，没有写描述～' }}</div>

        <div class="meta">
          <span>发布于 {{ formatTime(item.createTime) }}</span>
          <span v-if="item.topExpireTime">置顶至 {{ formatTime(item.topExpireTime) }}</span>
          <span class="report-link" @click="openReport">举报该商品</span>
        </div>
      </div>

      <div class="seller">
        <div class="avatar">{{ sellerAvatar }}</div>
        <div class="seller-info">
          <div class="seller-name">
            {{ item.sellerName || ('用户' + item.sellerId) }}
            <span class="credit-tag" v-if="(item.sellerCredit || 0) >= 110">🏅 高信誉</span>
          </div>
          <div class="seller-sub">校园认证卖家 · 支持面交</div>
        </div>
        <div class="credit-num">
          <b>{{ item.sellerCredit ?? '—' }}</b>
          <span>信用分</span>
        </div>
      </div>

      <div class="panel review-panel">
        <div class="section-label">
          卖家评价
          <span class="review-count" v-if="reviews.length">（{{ reviews.length }} 条）</span>
        </div>
        <div v-if="reviewLoading" class="no-review">评价加载中…</div>
        <div v-else-if="reviews.length === 0" class="no-review">暂无评价，完成交易后可以评价卖家</div>
        <template v-else>
          <div class="review-summary">
            <el-rate :model-value="avgRating" disabled show-score score-template="{value} 分"></el-rate>
          </div>
          <div class="review-item" v-for="r in reviews" :key="r.id">
            <div class="review-head">
              <span class="review-user">{{ r.reviewerName || '匿名同学' }}</span>
              <el-rate :model-value="r.rating" disabled size="small"></el-rate>
            </div>
            <div class="review-content">{{ r.content || '好评' }}</div>
            <div class="review-time">{{ formatTime(r.createTime) }}</div>
          </div>
        </template>
      </div>
    </div>

    <div class="action-bar" v-if="!loading && !error">
      <button class="want-btn" :disabled="item.status !== 0" @click="wantIt">{{ wantText }}</button>
    </div>

    <el-dialog v-model="contactVisible" title="联系卖家" width="400px" align-center>
      <div class="contact-head">
        <div class="avatar">{{ sellerAvatar }}</div>
        <div>
          <div class="seller-name">
            {{ item.sellerName || '卖家' }}
            <span class="credit-tag" v-if="(item.sellerCredit || 0) >= 110">🏅 高信誉</span>
          </div>
          <div class="seller-sub">信用分 {{ item.sellerCredit ?? '—' }}</div>
        </div>
      </div>
      <div class="contact-label">卖家联系方式</div>
      <div class="contact-box">
        <div class="contact-value">{{ contactInfo }}</div>
        <el-button type="primary" round @click="copyContact">{{ copyLabel }}</el-button>
      </div>
      <div class="contact-tip">
        当前仅展示掩码，点击「复制」后会获取完整联系方式并复制。<br>
        交易请选择校内公共场所面交，注意安全。
      </div>
      <template #footer>
        <el-button round @click="contactVisible = false">关闭</el-button>
        <el-button type="primary" round @click="openOrder">直接下单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="orderVisible" title="确认下单" width="400px" align-center>
      <div class="contact-label">期望交易地点</div>
      <el-input v-model="tradeLocation" size="large" maxlength="200" placeholder="如：一食堂门口 / 图书馆一楼"></el-input>
      <div class="contact-tip" style="margin-top:10px">提交后商品会被锁定为「已预订」，等待卖家确认面交。</div>
      <template #footer>
        <el-button round @click="orderVisible = false">取消</el-button>
        <el-button type="primary" round :loading="submitting" @click="submitOrder">提交订单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reportVisible" title="举报商品" width="420px" align-center>
      <div class="dialog-field">
        <label>举报原因</label>
        <el-select v-model="reportReason" placeholder="请选择举报原因" style="width:100%">
          <el-option label="违禁品 / 违法内容" value="违禁品"></el-option>
          <el-option label="诈骗 / 虚假信息" value="诈骗"></el-option>
          <el-option label="辱骂 / 骚扰" value="辱骂骚扰"></el-option>
          <el-option label="侵犯知识产权" value="侵权"></el-option>
          <el-option label="其他" value="其他"></el-option>
        </el-select>
      </div>
      <div class="dialog-field">
        <label>补充说明（可选）</label>
        <el-input v-model="reportDetail" type="textarea" :rows="3" maxlength="300" show-word-limit
                  placeholder="请描述具体问题，方便管理员核实"></el-input>
      </div>
      <template #footer>
        <el-button round @click="reportVisible = false">取消</el-button>
        <el-button type="danger" round :loading="reportSubmitting" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api } from '@/api/http';
import { useUserStore } from '@/stores/user';
import { copyText } from '@/utils/clipboard';
import { imgSrc, onImgError, formatPrice, formatTime } from '@/utils/media';

const route = useRoute();
const router = useRouter();
const user = useUserStore();
const itemId = Number(route.params.id);

const item = ref({});
const loading = ref(true);
const error = ref('');
const contactVisible = ref(false);
const contactInfo = ref('');
const orderVisible = ref(false);
const tradeLocation = ref('一食堂门口');
const submitting = ref(false);
const reviews = ref([]);
const reviewLoading = ref(false);

const reportVisible = ref(false);
const reportReason = ref('违禁品');
const reportDetail = ref('');
const reportSubmitting = ref(false);

const sellerAvatar = computed(() => String(item.value.sellerName || '青').slice(0, 1).toUpperCase());
const statusText = computed(() => ({ 0: '在售', 1: '已被预订', 2: '已售出', 3: '已下架' }[item.value.status] || ''));
const wantText = computed(() => {
  if (item.value.status === 0) return '我 想 要';
  if (item.value.status === 1) return '已被预订';
  if (item.value.status === 2) return '已售出';
  return '已下架';
});
const copyLabel = computed(() => {
  const c = contactInfo.value || '';
  if (/qq/i.test(c)) return '复制QQ号';
  if (/微信|wx/i.test(c)) return '复制微信号';
  return '一键复制';
});
const avgRating = computed(() => {
  if (!reviews.value.length) return 0;
  const sum = reviews.value.reduce((acc, r) => acc + (Number(r.rating) || 0), 0);
  return Math.round((sum / reviews.value.length) * 10) / 10;
});

async function fetchDetail() {
  return api.get('/api/item/' + itemId);
}

async function loadReviews(sellerId) {
  reviewLoading.value = true;
  try {
    reviews.value = await api.get('/api/review/user/' + sellerId);
  } catch (e) {
    // 评价加载失败不影响商品展示
  } finally {
    reviewLoading.value = false;
  }
}

onMounted(async () => {
  try {
    item.value = await fetchDetail();
    if (item.value && item.value.sellerId) loadReviews(item.value.sellerId);
  } catch (e) {
    error.value = e.message || '商品加载失败';
  } finally {
    loading.value = false;
  }
});

function goBack() {
  if (history.length > 1) history.back();
  else router.push('/');
}

function promptLogin() {
  ElMessageBox.confirm('登录后才能查看卖家联系方式，是否现在去登录？', '需要登录', {
    confirmButtonText: '去登录',
    cancelButtonText: '再逛逛',
    type: 'warning'
  }).then(() => router.push({ path: '/login', query: { redirect: route.fullPath } })).catch(() => {});
}

async function wantIt() {
  if (!user.isLoggedIn) {
    promptLogin();
    return;
  }
  try {
    const data = await fetchDetail();
    item.value = data;
    if (!data.contactMasked) {
      ElMessage.warning('卖家暂未填写联系方式');
      return;
    }
    contactInfo.value = data.contactMasked;
    contactVisible.value = true;
  } catch (e) {
    ElMessage.error(e.message || '获取联系方式失败');
  }
}

function openReport() {
  if (!user.isLoggedIn) {
    promptLogin();
    return;
  }
  reportReason.value = '违禁品';
  reportDetail.value = '';
  reportVisible.value = true;
}

async function submitReport() {
  if (!reportReason.value) return ElMessage.warning('请选择举报原因');
  reportSubmitting.value = true;
  try {
    const reason = reportDetail.value.trim()
      ? reportReason.value + '：' + reportDetail.value.trim()
      : reportReason.value;
    await api.post('/api/report', { itemId, reason });
    ElMessage.success('举报已提交，管理员会尽快处理');
    reportVisible.value = false;
  } catch (e) {
    ElMessage.error(e.message || '举报失败，请稍后再试');
  } finally {
    reportSubmitting.value = false;
  }
}

async function copyContact() {
  try {
    // 完整联系方式只在点击复制时获取，且后端限制 1 分钟 3 次
    const data = await api.get('/api/item/' + itemId + '/contact');
    const ok = await copyText(data.contactInfo || '');
    if (ok) ElMessage.success('复制成功，快去微信添加好友吧！');
    else ElMessage.error('复制失败，请手动长按复制');
  } catch (e) {
    ElMessage.error(e.message || '获取完整联系方式失败，请稍后再试');
  }
}

function openOrder() {
  contactVisible.value = false;
  orderVisible.value = true;
}

async function submitOrder() {
  if (!tradeLocation.value.trim()) return ElMessage.warning('请填写期望交易地点');
  submitting.value = true;
  try {
    await api.post('/api/order', {
      itemId: itemId,
      tradeLocation: tradeLocation.value.trim()
    });
    orderVisible.value = false;
    ElMessage.success('下单成功，等待卖家确认');
    item.value = await fetchDetail();
  } catch (e) {
    ElMessage.error(e.message || '下单失败');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.detail-page { padding-bottom: 96px; }
.topbar {
  position: sticky; top: 0; z-index: 10; display: flex; align-items: center; gap: 10px;
  padding: 12px 16px; background: rgba(255,255,255,.92); backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line);
}
.topbar .back { border: none; background: #e6fffa; color: var(--teal-dark); width: 34px; height: 34px; border-radius: 10px; font-size: 16px; cursor: pointer; }
.topbar h1 { font-size: 16px; margin: 0; }
.topbar .logo { margin-left: auto; font-size: 19px; }
.wrap { max-width: 780px; margin: 0 auto; padding: 14px; }
.gallery { position: relative; width: 100%; aspect-ratio: 1/1; max-height: 520px; background: #e6fffa; border-radius: 18px; overflow: hidden; border: 1px solid var(--line); }
.gallery img { width: 100%; height: 100%; object-fit: cover; display: block; }
.gallery .badges { position: absolute; top: 12px; left: 12px; display: flex; gap: 6px; }
.badge { font-size: 11px; font-weight: 700; color: #fff; padding: 4px 10px; border-radius: 999px; }
.badge.top { background: linear-gradient(135deg, #ef4444, #dc2626); }
.badge.credit { background: linear-gradient(135deg, #f59e0b, #d97706); }
.panel { background: #fff; border: 1px solid var(--line); border-radius: 18px; padding: 18px 16px; margin-top: 14px; box-shadow: 0 10px 26px rgba(13,148,136,.06); }
.price-row { display: flex; align-items: baseline; gap: 8px; }
.price { font-size: 30px; font-weight: 800; color: var(--teal-dark); }
.price i { font-style: normal; font-size: 16px; margin-right: 2px; }
.title { margin: 8px 0 10px; font-size: 20px; line-height: 1.45; font-weight: 700; }
.tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 14px; }
.tag { font-size: 12px; color: var(--teal-dark); background: var(--mint); border: 1px solid #b8f0e6; padding: 3px 10px; border-radius: 999px; }
.section-label { font-size: 13px; font-weight: 700; color: #36635c; margin: 4px 0 8px; }
.desc { font-size: 14px; line-height: 1.8; color: #46655f; white-space: pre-wrap; background: #f8fffd; border-radius: 12px; padding: 12px 14px; min-height: 60px; }
.meta { display: flex; gap: 14px; flex-wrap: wrap; font-size: 12px; color: #9bb5b0; margin-top: 12px; }
.report-link { margin-left: auto; color: #b7c9c5; cursor: pointer; text-decoration: underline; }
.report-link:hover { color: #dc2626; }
.dialog-field { margin-bottom: 14px; }
.dialog-field > label { display: block; font-size: 12px; font-weight: 600; color: #36635c; margin-bottom: 6px; }
.seller { display: flex; align-items: center; gap: 12px; background: #fff; border: 1px solid var(--line); border-radius: 18px; padding: 14px 16px; margin-top: 14px; }
.seller-info { min-width: 0; flex: 1; }
.seller-name { font-size: 15px; font-weight: 700; display: flex; align-items: center; gap: 8px; }
.credit-tag { font-size: 10px; padding: 2px 8px; border-radius: 999px; background: #fff7ed; color: #b45309; border: 1px solid #fed7aa; }
.seller-sub { font-size: 12px; color: #8aa; margin-top: 3px; }
.credit-num { text-align: center; flex: 0 0 auto; }
.credit-num b { display: block; font-size: 20px; color: var(--teal-dark); }
.credit-num span { font-size: 11px; color: #8aa; }
.review-count { font-weight: normal; color: #9bb5b0; font-size: 12px; }
.no-review { font-size: 13px; color: #9bb5b0; padding: 6px 0 2px; }
.review-summary { margin-bottom: 8px; }
.review-item { padding: 12px 0; border-top: 1px dashed #e3f6f1; }
.review-item:first-of-type { border-top: none; }
.review-head { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.review-user { font-size: 13px; font-weight: 600; color: #164e45; }
.review-content { font-size: 13px; color: #46655f; margin-top: 6px; line-height: 1.6; }
.review-time { font-size: 11px; color: #b7c9c5; margin-top: 4px; }
.action-bar { position: fixed; left: 0; right: 0; bottom: 0; z-index: 20; padding: 12px 16px calc(12px + env(safe-area-inset-bottom)); background: rgba(255,255,255,.96); border-top: 1px solid var(--line); backdrop-filter: blur(10px); }
.want-btn { display: block; width: 100%; max-width: 780px; margin: 0 auto; height: 54px; border: none; border-radius: 18px; cursor: pointer; background: linear-gradient(135deg, #14b8a6, #0d9488); color: #fff; font-size: 18px; font-weight: 800; letter-spacing: 3px; box-shadow: 0 12px 26px rgba(13,148,136,.3); }
.want-btn[disabled] { background: #cfe9e4; box-shadow: none; cursor: not-allowed; }
.contact-head { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.contact-label { font-size: 12px; color: #8aa; margin-bottom: 6px; }
.contact-box { display: flex; align-items: center; justify-content: space-between; gap: 12px; background: #f4fffd; border: 1px dashed #9fe6d9; border-radius: 14px; padding: 14px 16px; margin-bottom: 10px; }
.contact-value { font-size: 18px; font-weight: 800; color: var(--teal-dark); word-break: break-all; }
.contact-tip { font-size: 12px; color: #9bb5b0; line-height: 1.7; }
@media (max-width: 520px) {
  .wrap { padding: 10px; }
  .panel { padding: 14px; }
  .price { font-size: 26px; }
  .title { font-size: 17px; }
}
</style>
