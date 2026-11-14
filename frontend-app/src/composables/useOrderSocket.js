import { watch, onUnmounted } from 'vue';
import { ElNotification } from 'element-plus';
import { useUserStore } from '@/stores/user';

/**
 * 订单通知 WebSocket：
 * - 登录后自动连接 ws://host/ws?token=...
 * - 收到 NEW_ORDER 时弹出 ElNotification
 * - 断线 5 秒后自动重连；退出登录时关闭
 */
export function useOrderSocket() {
  const user = useUserStore();

  let ws = null;
  let retryTimer = null;
  let heartbeatTimer = null;
  let closedByUs = false;

  function buildUrl(token) {
    const base = import.meta.env.VITE_WS_BASE || '';
    if (base) {
      return base.replace(/\/$/, '') + '/ws?token=' + encodeURIComponent(token);
    }
    const proto = location.protocol === 'https:' ? 'wss' : 'ws';
    return `${proto}://${location.host}/ws?token=${encodeURIComponent(token)}`;
  }

  function connect() {
    if (!user.isLoggedIn) return;
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) return;

    closedByUs = false;
    try {
      ws = new WebSocket(buildUrl(user.token));
    } catch (e) {
      scheduleReconnect();
      return;
    }

    ws.onopen = () => {
      clearInterval(heartbeatTimer);
      heartbeatTimer = setInterval(() => {
        if (ws && ws.readyState === WebSocket.OPEN) ws.send('ping');
      }, 25000);
    };

    ws.onmessage = (event) => {
      if (event.data === 'pong') return;
      try {
        const data = JSON.parse(event.data);
        if (data.type === 'NEW_ORDER') {
          ElNotification({
            title: data.title || '青售提示',
            message: data.message || '有新订单啦，请及时确认！',
            type: 'success',
            duration: 6000,
            position: 'top-right'
          });
        }
      } catch (e) {
        // 非 JSON 消息忽略
      }
    };

    ws.onclose = () => {
      clearInterval(heartbeatTimer);
      heartbeatTimer = null;
      ws = null;
      if (!closedByUs && user.isLoggedIn) scheduleReconnect();
    };
  }

  function scheduleReconnect() {
    clearTimeout(retryTimer);
    retryTimer = setTimeout(connect, 5000);
  }

  function close() {
    closedByUs = true;
    clearTimeout(retryTimer);
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
    if (ws) {
      try { ws.close(); } catch (e) { /* ignore */ }
      ws = null;
    }
  }

  watch(
    () => user.token,
    (token) => {
      if (token) connect();
      else close();
    },
    { immediate: true }
  );

  onUnmounted(close);

  return { connect, close };
}
