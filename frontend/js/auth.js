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

  return "./index.html";
}

function setFormDisabled(form, disabled) {
  Array.from(form.elements).forEach((element) => {
    element.disabled = disabled;
  });
}

const next = resolveNextUrl();

const $loginForm = document.getElementById("loginForm");
const $registerForm = document.getElementById("registerForm");
const $loginError = document.getElementById("loginError");
const $registerError = document.getElementById("registerError");
const $sessionState = document.getElementById("sessionState");
const $authLink = document.getElementById("authLink");
const $userGreeting = document.getElementById("userGreeting");
const $cartCountBadge = document.getElementById("cartCountBadge");

function renderSessionState() {
  const session = getSession();

  syncNav({
    authLink: $authLink,
    userGreeting: $userGreeting,
    cartCountBadge: $cartCountBadge,
    logoutTarget: "./login.html",
  });

  if (!session?.token) {
    $sessionState.innerHTML = `
      <strong>Chưa đăng nhập.</strong>
      <p>Bạn có thể đăng nhập để tiếp tục thanh toán hoặc tạo tài khoản mới ngay bên dưới.</p>
    `;
    return;
  }

  $sessionState.innerHTML = `
    <strong>Đang đăng nhập${session.username ? ` với tài khoản ${escapeHtml(session.username)}` : ""}.</strong>
    <p>Bạn có thể quay lại trang trước hoặc tiếp tục mua hàng.</p>
    <div class="inline-actions">
      <a class="btn" href="${escapeHtml(next)}">Tiếp tục</a>
      <a class="btn btn--ghost" href="./index.html">Trang chủ</a>
    </div>
  `;
}

async function handleAuth(form, endpoint, statusElement, values) {
  setFormDisabled(form, true);
  setStatus(statusElement, "Đang gửi thông tin...", "info");

  try {
    const response = await apiPost(endpoint, values);
    saveSession(response);
    renderSessionState();
    setStatus(statusElement, "Thành công. Đang chuyển trang...", "success");
    window.setTimeout(() => {
      window.location.href = next;
    }, 250);
  } catch (error) {
    console.error(error);
    setStatus(statusElement, error.message || String(error), "error");
  } finally {
    setFormDisabled(form, false);
  }
}

$loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  setStatus($registerError, "", "info");

  const username = document.getElementById("loginUsername").value.trim();
  const password = document.getElementById("loginPassword").value;

  await handleAuth($loginForm, "/auth/login", $loginError, { username, password });
});

$registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  setStatus($loginError, "", "info");

  const username = document.getElementById("registerUsername").value.trim();
  const password = document.getElementById("registerPassword").value;

  await handleAuth($registerForm, "/auth/register", $registerError, { username, password });
});

window.addEventListener("session:updated", renderSessionState);
renderSessionState();
