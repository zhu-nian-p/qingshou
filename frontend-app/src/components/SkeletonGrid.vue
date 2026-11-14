<template>
  <div class="grid">
    <div class="skeleton-card" v-for="n in count" :key="n">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="image" style="width:100%; aspect-ratio:1/1"></el-skeleton-item>
          <div class="sk-body">
            <el-skeleton-item variant="text" style="width:85%"></el-skeleton-item>
            <el-skeleton-item variant="text" style="width:50%"></el-skeleton-item>
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>
</template>

<script setup>
defineProps({
  count: { type: Number, default: 8 }
});
</script>
