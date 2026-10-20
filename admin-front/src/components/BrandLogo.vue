<template>
  <span class="brand" :class="{ 'brand--stacked': stacked }">
    <span class="brand-mark" :style="{ width: size + 'px', height: size + 'px' }">
      <svg viewBox="0 0 40 40" aria-hidden="true">
        <defs>
          <linearGradient :id="gradientId" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0" stop-color="#0A84FF" />
            <stop offset="1" stop-color="#38BDF8" />
          </linearGradient>
        </defs>
        <rect width="40" height="40" rx="11" :fill="`url(#${gradientId})`" />
        <path
          d="M17.2 9.6h5.6v7.6h7.6v5.6h-7.6v7.6h-5.6v-7.6H9.6v-5.6h7.6z"
          fill="#fff"
          fill-opacity="0.96"
        />
      </svg>
    </span>
    <span v-if="showText" class="brand-text">
      <span class="brand-name">{{ name }}</span>
      <span v-if="subtitle" class="brand-sub">{{ subtitle }}</span>
    </span>
  </span>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 34 },
  name: { type: String, default: 'Aimin' },
  subtitle: { type: String, default: '' },
  showText: { type: Boolean, default: true },
  stacked: { type: Boolean, default: false }
})

// 同一页面可能放多个 logo，渐变 id 必须唯一，否则会互相引用
const gradientId = `aimin-brand-${Math.random().toString(36).slice(2, 9)}`
</script>

<style scoped>
.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  user-select: none;
}

.brand--stacked {
  flex-direction: column;
  gap: 14px;
}

.brand-mark {
  display: inline-flex;
  flex-shrink: 0;
  filter: drop-shadow(0 6px 14px rgba(10, 132, 255, 0.32));
}

.brand-mark svg {
  width: 100%;
  height: 100%;
  display: block;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  text-align: left;
}

.brand--stacked .brand-text {
  align-items: center;
  text-align: center;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.2px;
  color: var(--aimin-text, #1d1d1f);
}

.brand-sub {
  margin-top: 2px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.6px;
  color: var(--aimin-text-tertiary, #8e8e93);
}
</style>
