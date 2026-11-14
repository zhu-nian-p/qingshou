<template>
  <article class="card" @click="$emit('click', item)">
    <div class="thumb">
      <img :src="imgSrc(item.imageUrl)" :alt="item.title" loading="lazy" @error="onImgError">
      <div class="badges">
        <span v-if="item.isTop === 1" class="badge top">🔥 急售</span>
        <span v-if="(item.sellerCredit || 0) >= 110" class="badge credit">🏅 高信誉</span>
      </div>
      <span class="campus-tag" v-if="item.campus">{{ item.campus }}</span>
    </div>
    <div class="card-body">
      <h3 class="title">{{ item.title }}</h3>
      <div class="row">
        <span class="price"><i>¥</i>{{ formatPrice(item.price) }}</span>
        <span class="seller">👤 {{ item.sellerName || ('用户' + item.sellerId) }}</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { imgSrc, onImgError, formatPrice } from '@/utils/media';

defineProps({
  item: { type: Object, required: true }
});
defineEmits(['click']);
</script>
