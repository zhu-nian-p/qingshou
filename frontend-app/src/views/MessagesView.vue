<template>
  <AppShell active="messages">
    <h2 class="page-title">消息</h2>
    <div class="msg-list">
      <div class="msg" v-for="m in messages" :key="m.id">
        <div class="msg-icon">{{ m.icon }}</div>
        <div class="msg-body">
          <div class="msg-head">
            <span class="msg-title">{{ m.title }}</span>
            <span class="msg-time">{{ m.time }}</span>
          </div>
          <div class="msg-desc">{{ m.desc }}</div>
        </div>
      </div>
    </div>
  </AppShell>
</template>

<script setup>
import AppShell from '@/components/AppShell.vue';

const messages = [
  { id: 1, icon: '🛒', title: '你的商品「iPad 第 9 代 64G」有新订单', desc: '买家 buyer01 已下单，请尽快在「我卖出的」里确认交易', time: '刚刚' },
  { id: 2, icon: '✅', title: '交易完成，信用分 +1', desc: '与 buyer01 的面交订单已完成，感谢使用青售', time: '2 小时前' },
  { id: 3, icon: '🔥', title: '置顶申请已通过', desc: '你的商品已置顶 7 天，首页曝光量提升中', time: '昨天' },
  { id: 4, icon: '🤖', title: '青售小助手', desc: '发布商品时可以使用 AI 生成描述，试试看～', time: '2 天前' }
];
</script>
