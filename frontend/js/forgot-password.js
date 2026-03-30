import { apiPost, setStatus } from "./api.js";
import { syncNav } from "./ui.js";

function setFormDisabled(form, disabled) {
  Array.from(form.elements).forEach((element) => {
    element.disabled = disabled;
  });
}

const $forgotForm = document.getElementById("forgotForm");
const $forgotStatus = document.getElementById("forgotStatus");

syncNav({ logoutTarget: "./forgot-password.html" });

$forgotForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  setFormDisabled($forgotForm, true);
  setStatus($forgotStatus, "", "info");

  const username = document.getElementById("forgotUsername").value.trim();
  const newPassword = document.getElementById("forgotPassword").value;
  const confirmPassword = document.getElementById("forgotPasswordConfirm").value;

  if (newPassword !== confirmPassword) {
    setStatus($forgotStatus, "Mật khẩu xác nhận không khớp.", "error");
    setFormDisabled($forgotForm, false);
    return;
  }

  try {
    const response = await apiPost("/auth/forgot-password", { username, newPassword });
    setStatus($forgotStatus, response.message || "Đã cập nhật mật khẩu. Bạn có thể đăng nhập lại.", "success");
    $forgotForm.reset();
  } catch (error) {
    console.error(error);
    setStatus($forgotStatus, error.message || String(error), "error");
  } finally {
    setFormDisabled($forgotForm, false);
  }
});
