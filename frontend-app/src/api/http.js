export const API_BASE = import.meta.env.VITE_API_BASE || '';

let unauthorizedHandler = null;

/** 注册 401 统一处理（在 main.js 中挂载，避免 router/store 循环依赖） */
export function onUnauthorized(fn) {
  unauthorizedHandler = fn;
}

function buildQuery(params) {
  if (!params) return '';
  const usp = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') usp.set(k, String(v));
  });
  const qs = usp.toString();
  return qs ? '?' + qs : '';
}

async function request(method, path, { body, params, headers } = {}) {
  const finalHeaders = { ...(headers || {}) };
  let payload = body;

  if (body !== undefined && !(body instanceof FormData)) {
    finalHeaders['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }

  const token = localStorage.getItem('qingshou_token');
  if (token) finalHeaders.Authorization = 'Bearer ' + token;

  const res = await fetch(API_BASE + path + buildQuery(params), {
    method,
    headers: finalHeaders,
    body: payload
  });

  if (res.status === 401) {
    unauthorizedHandler && unauthorizedHandler();
  }

  const json = await res.json().catch(() => ({}));
  if (!res.ok || json.code !== 200) {
    const err = new Error(json.message || `请求失败（HTTP ${res.status}）`);
    err.status = res.status;
    throw err;
  }
  return json.data;
}

export const api = {
  get: (path, params) => request('GET', path, { params }),
  post: (path, body) => request('POST', path, { body }),
  put: (path, body) => request('PUT', path, { body }),
  upload: (file) => {
    const fd = new FormData();
    fd.append('file', file);
    return request('POST', '/api/upload', { body: fd });
  }
};
