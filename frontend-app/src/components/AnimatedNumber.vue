<template>
  <span class="animated-number">{{ display }}</span>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  value: { type: [Number, String], default: 0 },
  decimals: { type: Number, default: 0 },
  duration: { type: Number, default: 900 },
  prefix: { type: String, default: '' }
});

const display = ref(format(0));
let raf = null;
let startTime = 0;
let fromValue = 0;

function format(v) {
  const n = Number(v) || 0;
  return props.prefix + n.toFixed(props.decimals);
}

function animate(to) {
  cancelAnimationFrame(raf);
  fromValue = Number(String(display.value).replace(/[^\d.-]/g, '')) || 0;
  startTime = performance.now();
  const step = (now) => {
    const progress = Math.min(1, (now - startTime) / props.duration);
    const eased = 1 - Math.pow(1 - progress, 3);
    display.value = format(fromValue + (to - fromValue) * eased);
    if (progress < 1) raf = requestAnimationFrame(step);
  };
  raf = requestAnimationFrame(step);
}

watch(() => Number(props.value) || 0, (v) => animate(v));
onMounted(() => animate(Number(props.value) || 0));
onUnmounted(() => cancelAnimationFrame(raf));
</script>

<style scoped>
.animated-number { font-variant-numeric: tabular-nums; }
</style>
