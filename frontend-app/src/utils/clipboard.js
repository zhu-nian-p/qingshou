/**
 * 复制文本：优先 Clipboard API；不可用 / 被拒绝 / 400ms 未返回时降级 execCommand。
 * 返回 Promise<boolean>。
 */
export function copyText(text) {
  return new Promise((resolve) => {
    let settled = false;
    const finish = (value) => {
      if (settled) return;
      settled = true;
      resolve(value);
    };
    const fallback = () => {
      const ta = document.createElement('textarea');
      ta.value = text;
      ta.style.position = 'fixed';
      ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select();
      let ok = false;
      try {
        ok = document.execCommand('copy');
      } catch (e) {
        ok = false;
      } finally {
        document.body.removeChild(ta);
      }
      finish(ok);
    };

    if (navigator.clipboard && window.isSecureContext) {
      const timer = setTimeout(fallback, 400);
      navigator.clipboard.writeText(text)
        .then(() => { clearTimeout(timer); finish(true); })
        .catch(() => { clearTimeout(timer); fallback(); });
    } else {
      fallback();
    }
  });
}
