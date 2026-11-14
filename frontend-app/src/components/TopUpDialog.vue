<template>
  <el-dialog
    :model-value="modelValue"
    title="申请置顶"
    width="420px"
    align-center
    @update:model-value="$emit('update:modelValue', $event)">
    <div class="topup-item-name" v-if="item">商品：{{ item.title }}</div>

    <div class="plan-list">
      <div class="plan" v-for="p in plans" :key="p.days"
           :class="{ active: selectedDays === p.days }" @click="selectedDays = p.days">
        <div class="plan-days">{{ p.days }} 天</div>
        <div class="plan-price">¥{{ p.price }}</div>
        <div class="plan-sub" v-if="p.days === 7">最受欢迎</div>
      </div>
    </div>

    <div class="topup-total">合计：<b>¥{{ planPrice }}</b></div>

    <div class="qr-wrap">
      <img :src="qrSrc" alt="收款码" @error="onQrError">
      <div class="qr-tip">扫码支付 {{ planPrice }} 元</div>
    </div>

    <div class="topup-note">
      扫码支付后点击下方按钮提交。由于是人工审核，请添加管理员微信：
      <span class="wechat-id">{{ adminWechat }}</span>
      <el-button class="copy-wx-btn" link type="primary" size="small" @click="copyWechat">复制微信号</el-button>
      发送截图，通常在 1 小时内完成。
    </div>

    <template #footer>
      <div class="topup-footer">
        <el-button round @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button class="topup-submit" type="primary" round :loading="submitting" @click="submit">
          我已支付，提交申请
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { api } from '@/api/http';
import { copyText } from '@/utils/clipboard';
import { QR_PLACEHOLDER } from '@/utils/media';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  item: { type: Object, default: null }
});
const emit = defineEmits(['update:modelValue', 'submitted']);

const plans = [
  { days: 1, price: '1.00' },
  { days: 7, price: '5.00' },
  { days: 30, price: '12.00' }
];
// 换成你自己的真实管理员微信号
const adminWechat = ref('qingshou-admin');
const selectedDays = ref(1);
const submitting = ref(false);
const qrFailed = ref(false);
const QR_URL = 'https://img.example.com/qr.png';

const planPrice = computed(() => {
  const plan = plans.find(p => p.days === selectedDays.value) || plans[0];
  return plan.price;
});
const qrSrc = computed(() => (qrFailed.value ? QR_PLACEHOLDER : QR_URL));

function onQrError(e) {
  qrFailed.value = true;
  e.target.src = QR_PLACEHOLDER;
  e.target.onerror = null;
}

async function copyWechat() {
  const ok = await copyText(adminWechat.value);
  if (ok) ElMessage.success('微信号已复制');
  else ElMessage.error('复制失败，请手动长按复制');
}

async function submit() {
  if (!props.item) return;
  submitting.value = true;
  try {
    await api.post('/api/topup/apply', {
      itemId: props.item.id,
      days: selectedDays.value
    });
    ElMessage.success('申请已提交，管理员审核通过后会为你置顶');
    emit('update:modelValue', false);
    emit('submitted');
  } catch (e) {
    ElMessage.error(e.message || '提交失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
}
</script>
