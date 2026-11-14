<template>
  <el-dialog
    :model-value="modelValue"
    title="评价卖家"
    width="400px"
    align-center
    @update:model-value="$emit('update:modelValue', $event)">
    <div class="review-order" v-if="order">商品：{{ order.itemTitle }}</div>
    <el-rate v-model="rating" show-text :texts="['很差', '较差', '一般', '满意', '非常满意']"></el-rate>
    <el-input
      v-model="content"
      type="textarea"
      :rows="3"
      maxlength="500"
      show-word-limit
      placeholder="说说这次交易体验，帮助其他同学参考～"
      style="margin-top: 12px"></el-input>
    <template #footer>
      <el-button round @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" round :loading="submitting" @click="submit">提交评价</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { api } from '@/api/http';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null }
});
const emit = defineEmits(['update:modelValue', 'submitted']);

const rating = ref(5);
const content = ref('');
const submitting = ref(false);

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      rating.value = 5;
      content.value = '';
    }
  }
);

async function submit() {
  if (!props.order) return;
  submitting.value = true;
  try {
    await api.post('/api/review', {
      orderId: props.order.id,
      rating: rating.value,
      content: content.value.trim()
    });
    ElMessage.success('评价成功，感谢你的反馈');
    emit('update:modelValue', false);
    emit('submitted');
  } catch (e) {
    ElMessage.error(e.message || '评价失败');
  } finally {
    submitting.value = false;
  }
}
</script>
