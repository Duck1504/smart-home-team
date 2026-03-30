import { apiPost, escapeHtml, getSession, saveSession, setStatus } from "./api.js";
import { buildLoginUrl, syncNav } from "./ui.js";

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
const $loginForm = document.getElementById("loginForm");
const $loginStatus = document.getElementById("loginStatus");
const $sessionState = document.getElementById("sessionState");

function renderSessionState() {
  const session = getSession();
  syncNav({ logoutTarget: "./login.html" });

  if (!session?.token) {
    $sessionState.innerHTML = `
      <strong>Chưa đăng nhập.</strong>
      <p>Sau khi đăng nhập, bạn sẽ được chuyển tiếp về trang trước đó.</p>
    `;
    return;
  }

  $sessionState.innerHTML = `
    <strong>Đang đăng nhập với tài khoản ${escapeHtml(session.username || "")}.</strong>
    <p>Nhấn tiếp tục để quay về đúng trang bạn đang cần.</p>
    <div class="inline-actions">
      <a class="btn" href="${escapeHtml(next)}">Tiếp tục</a>
      <a class="btn btn--ghost" href="./index.html">Trang chủ</a>
    </div>
  `;
}

$loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  setFormDisabled($loginForm, true);
  setStatus($loginStatus, "Đang xử lý đăng nhập...", "info");

  try {
    const username = document.getElementById("loginUsername").value.trim();
    const password = document.getElementById("loginPassword").value;
    const response = await apiPost("/auth/login", { username, password });
    saveSession(response);
    renderSessionState();
    setStatus($loginStatus, "Đăng nhập thành công. Đang chuyển trang...", "success");
    window.setTimeout(() => {
      window.location.href = next;
    }, 250);
  } catch (error) {
    console.error(error);
    setStatus($loginStatus, error.message || String(error), "error");
  } finally {
    setFormDisabled($loginForm, false);
  }
});

window.addEventListener("session:updated", renderSessionState);
renderSessionState();
