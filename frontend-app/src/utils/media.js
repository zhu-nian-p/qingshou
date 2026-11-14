import { API_BASE } from '@/api/http';

export const PLACEHOLDER =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400">' +
    '<rect width="400" height="400" fill="#e6fffa"/>' +
    '<text x="200" y="205" font-size="18" fill="#14b8a6" text-anchor="middle" ' +
    'font-family="sans-serif">青售 · 暂无图片</text></svg>'
  );

/** 置顶弹窗收款码占位图（外链失效时降级） */
export const QR_PLACEHOLDER = 'data:image/svg+xml;utf8,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240">' +
  '<rect width="240" height="240" fill="#ffffff"/>' +
  '<rect x="18" y="18" width="60" height="60" fill="#0f2e2a"/>' +
  '<rect x="30" y="30" width="36" height="36" fill="#ffffff"/>' +
  '<rect x="40" y="40" width="16" height="16" fill="#0f2e2a"/>' +
  '<rect x="162" y="18" width="60" height="60" fill="#0f2e2a"/>' +
  '<rect x="174" y="30" width="36" height="36" fill="#ffffff"/>' +
  '<rect x="184" y="40" width="16" height="16" fill="#0f2e2a"/>' +
  '<rect x="18" y="162" width="60" height="60" fill="#0f2e2a"/>' +
  '<rect x="30" y="174" width="36" height="36" fill="#ffffff"/>' +
  '<rect x="40" y="184" width="16" height="16" fill="#0f2e2a"/>' +
  '<rect x="96" y="24" width="14" height="14" fill="#0f2e2a"/>' +
  '<rect x="120" y="48" width="14" height="14" fill="#0f2e2a"/>' +
  '<rect x="96" y="72" width="14" height="14" fill="#0f2e2a"/>' +
  '<rect x="132" y="96" width="14" height="14" fill="#0f2e2a"/>' +
  '<rect x="168" y="120" width="14" height="14" fill="#0f2e2a"/>' +
  '<rect x="96" y="168" width="14" height="14" fill="#0f2e2a"/>' +
  '<rect x="120" y="192" width="14" height="14" fill="#0f2e2a"/>' +
  '<rect x="150" y="168" width="14" height="14" fill="#0f2e2a"/>' +
  '<rect x="180" y="192" width="14" height="14" fill="#0f2e2a"/>' +
  '<text x="120" y="225" font-size="12" fill="#14b8a6" text-anchor="middle" font-family="sans-serif">示例收款码</text>' +
  '</svg>'
);

export function imgSrc(url) {
  if (!url) return PLACEHOLDER;
  if (/^(https?:|data:)/i.test(url)) return url;
  return API_BASE + url;
}

export function onImgError(e) {
  e.target.src = PLACEHOLDER;
  e.target.onerror = null;
}

export function formatPrice(v) {
  const n = Number(v);
  return Number.isFinite(n) ? n.toFixed(2) : v;
}

export function formatTime(t) {
  if (!t) return '—';
  return String(t).replace('T', ' ').slice(0, 16);
}

export const ITEM_STATUS_TEXT = { 0: '在售', 1: '已预订', 2: '已售出', 3: '已下架' };

export function itemStatusText(status) {
  return ITEM_STATUS_TEXT[status] || '未知';
}
