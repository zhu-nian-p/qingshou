<template>
  <AppShell active="home">
    <div class="hero">
      <h1>把闲置，交给需要的人</h1>
      <p>同校面交 · 信用可见 · 置顶好物优先展示</p>
    </div>

    <div class="search-row">
      <el-input
        v-model="keyword"
        size="large"
        clearable
        placeholder="搜索：自行车、台灯、考研书…"
        @keyup.enter="reload"
        @clear="reload">
        <template #prefix><span>🔍</span></template>
      </el-input>
      <el-button class="search-btn" size="large" @click="reload">搜索</el-button>
    </div>

    <div class="campus-tabs">
      <button v-for="c in campuses" :key="c" :class="{ active: campus === c }" @click="pickCampus(c)">
        {{ c }}
      </button>
    </div>

    <div class="list-head">
      <span>共 <b>{{ total }}</b> 件好物</span>
      <span class="hint" v-if="campus !== '全部'">📍 {{ campus }}校区</span>
    </div>

    <div style="min-height: 260px">
      <div v-if="error" class="state-box">
        <div class="emoji">😥</div>
        <div class="state-title">加载失败了</div>
        <div class="state-desc">{{ error }}</div>
        <el-button type="primary" round @click="reload">重试</el-button>
      </div>

      <SkeletonGrid v-else-if="loading && items.length === 0" :count="8" />

      <div v-else-if="!loading && items.length === 0" class="state-box">
        <div class="emoji">🌱</div>
        <div class="state-title">这里空空如也</div>
        <div class="state-desc">快去发布第一个闲置吧～</div>
        <el-button type="primary" round @click="router.push('/publish')">发布闲置</el-button>
      </div>

      <div v-else class="grid">
        <ItemCard v-for="item in items" :key="item.id" :item="item" @click="goDetail(item)" />
      </div>

      <div class="load-footer" v-if="!error && items.length > 0">
        <span v-if="loadingMore"><span class="dot-spinner"></span>加载中…</span>
        <span v-else-if="noMore">没有更多啦，去发一个闲置吧~</span>
        <span v-else>上滑加载更多</span>
      </div>
    </div>
  </AppShell>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { api } from '@/api/http';
import AppShell from '@/components/AppShell.vue';
import ItemCard from '@/components/ItemCard.vue';
import SkeletonGrid from '@/components/SkeletonGrid.vue';

const router = useRouter();
const campuses = ['全部', '上海路', '前湖'];
const keyword = ref('');
const campus = ref('全部');
const items = ref([]);
const total = ref(0);
const page = ref(1);
const loading = ref(false);
const loadingMore = ref(false);
const noMore = ref(false);
const error = ref('');

const PAGE_SIZE = 10;
let requestSeq = 0;

function buildParams(pageNo) {
  return {
    keyword: keyword.value.trim(),
    campus: campus.value === '全部' ? '' : campus.value,
    page: pageNo,
    size: PAGE_SIZE
  };
}

async function loadItems(reset = true) {
  if (!reset) {
    if (loading.value || loadingMore.value || noMore.value) return;
    loadingMore.value = true;
  } else {
    requestSeq += 1;
    page.value = 1;
    items.value = [];
    total.value = 0;
    noMore.value = false;
    loadingMore.value = false;
    error.value = '';
    loading.value = true;
  }

  const seq = requestSeq;
  const targetPage = reset ? 1 : page.value;

  try {
    const data = await api.get('/api/item/list', buildParams(targetPage));
    if (seq !== requestSeq) return;
    const records = data.records || [];
    items.value = reset ? records : items.value.concat(records);
    total.value = data.total || 0;
    page.value = (data.page || targetPage) + 1;
    noMore.value = items.value.length >= total.value
      || records.length === 0
      || (data.pages && data.page >= data.pages);
  } catch (e) {
    if (seq !== requestSeq) return;
    if (reset) {
      items.value = [];
      error.value = e.message || String(e);
    } else {
      ElMessage.error('加载更多失败，请稍后重试');
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false;
      loadingMore.value = false;
    }
  }
}

function reload() {
  loadItems(true);
}

function pickCampus(c) {
  campus.value = c;
  reload();
}

function goDetail(item) {
  router.push('/detail/' + item.id);
}

function onScroll() {
  if (loading.value || loadingMore.value || noMore.value) return;
  const doc = document.documentElement;
  if (window.innerHeight + window.scrollY >= doc.scrollHeight - 140) {
    loadItems(false);
  }
}

onMounted(() => {
  reload();
  window.addEventListener('scroll', onScroll, { passive: true });
});
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll);
});
</script>

<style scoped>
@media (max-width: 767px) {
  .hero { display: none; }
}
</style>
