import { apiPost, escapeHtml, getSession, saveSession, setStatus } from "./api.js";
import { syncNav } from "./ui.js";

function resolveNextUrl() {
  const url = new URL(window.location.href);
  const next = url.searchParams.get("next");

  if (!next) {
    return "./index.html";
  }

  if (next.startsWith("./") || next.startsWith("/")) {
    return next;
  }

  return `./${next}`;
}

function setFormDisabled(form, disabled) {
  Array.from(form.elements).forEach((element) => {
    element.disabled = disabled;
  });
}

const next = resolveNextUrl();
const $registerForm = document.getElementById("registerForm");
const $registerStatus = document.getElementById("registerStatus");
const $sessionState = document.getElementById("sessionState");

function renderSessionState() {
  const session = getSession();
  syncNav({ logoutTarget: "./register.html" });

  if (!session?.token) {
    $sessionState.innerHTML = `
      <strong>Sẵn sàng tạo tài khoản mới.</strong>
      <p>Tài khoản mới sẽ có quyền USER và vẫn có thể mua hàng, xem đơn hàng bình thường.</p>
    `;
    return;
  }

  $sessionState.innerHTML = `
    <strong>Bạn đang đăng nhập với tài khoản ${escapeHtml(session.username || "")}.</strong>
    <p>Nếu muốn tạo thêm tài khoản khác, hãy đăng xuất trước.</p>
    <div class="inline-actions">
      <a class="btn" href="${escapeHtml(next)}">Tiếp tục</a>
      <a class="btn btn--ghost" href="./index.html">Trang chủ</a>
    </div>
  `;
}

$registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  setFormDisabled($registerForm, true);
  setStatus($registerStatus, "Đang tạo tài khoản...", "info");

  try {
    const username = document.getElementById("registerUsername").value.trim();
    const password = document.getElementById("registerPassword").value;
    const response = await apiPost("/auth/register", { username, password });
    saveSession(response);
    renderSessionState();
    setStatus($registerStatus, "Đăng ký thành công. Đang chuyển trang...", "success");
    window.setTimeout(() => {
      window.location.href = next;
    }, 250);
  } catch (error) {
    console.error(error);
    setStatus($registerStatus, error.message || String(error), "error");
  } finally {
    setFormDisabled($registerForm, false);
  }
});

window.addEventListener("session:updated", renderSessionState);
renderSessionState();
