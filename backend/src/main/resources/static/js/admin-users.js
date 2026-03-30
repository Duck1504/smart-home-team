import { apiDelete, apiGet, apiPost, apiPut, escapeHtml, getSession, setStatus } from "./api.js";
import { requireAdminPage, syncNav } from "./ui.js";

const $tbody = document.getElementById("usersTableBody");
const $status = document.getElementById("userStatus");

let users = [];

function renderUsers() {
  if (users.length === 0) {
    $tbody.innerHTML = `<tr><td colspan="5" class="table-empty">Chưa có người dùng nào.</td></tr>`;
    return;
  }

  const session = getSession();

  $tbody.innerHTML = users.map((user) => `
    <tr>
      <td>
        <strong>${escapeHtml(user.username)}</strong>
        ${user.username === session?.username ? '<div class="table-subtext">Tai khoan hien tai</div>' : ""}
      </td>
      <td>
        <div class="inline-field">
          <select data-role-select="${user.id}">
            <option value="USER" ${user.role === "USER" ? "selected" : ""}>USER</option>
            <option value="ADMIN" ${user.role === "ADMIN" ? "selected" : ""}>ADMIN</option>
          </select>
          <button class="btn btn--ghost btn--small" type="button" data-action="save-role" data-id="${user.id}">Lưu role</button>
        </div>
      </td>
      <td>${user.orderCount}</td>
      <td>
        <div class="inline-field">
          <input type="password" minlength="6" maxlength="100" placeholder="Mat khau moi" data-password-input="${user.id}" />
          <button class="btn btn--ghost btn--small" type="button" data-action="reset-password" data-id="${user.id}">Đặt lại</button>
        </div>
      </td>
      <td>
        <button class="btn btn--ghost btn--small" type="button" data-action="delete" data-id="${user.id}" ${user.username === session?.username ? "disabled" : ""}>Xóa</button>
      </td>
    </tr>
  `).join("");
}

async function loadUsers() {
  setStatus($status, "Dang tai danh sach nguoi dung...", "info");
  users = await apiGet("/admin/users");
  renderUsers();
  setStatus($status, `Da tai ${users.length} nguoi dung.`, "success");
}

function getRoleValue(userId) {
  return $tbody.querySelector(`[data-role-select="${userId}"]`)?.value || "USER";
}

function getPasswordValue(userId) {
  return $tbody.querySelector(`[data-password-input="${userId}"]`)?.value || "";
}

async function handleTableClick(event) {
  const button = event.target.closest("button[data-action]");
  if (!button) {
    return;
  }

  const userId = Number(button.dataset.id);
  const user = users.find((item) => item.id === userId);
  if (!user) {
    return;
  }

  try {
    if (button.dataset.action === "save-role") {
      await apiPut(`/admin/users/${userId}/role`, { role: getRoleValue(userId) });
      setStatus($status, `Da cap nhat role cho ${user.username}.`, "success");
      await loadUsers();
      return;
    }

    if (button.dataset.action === "reset-password") {
      const newPassword = getPasswordValue(userId).trim();
      if (newPassword.length < 6) {
        setStatus($status, "Mat khau moi phai co it nhat 6 ky tu.", "error");
        return;
      }

      await apiPost(`/admin/users/${userId}/password`, { newPassword });
      setStatus($status, `Da dat lai mat khau cho ${user.username}.`, "success");
      await loadUsers();
      return;
    }

    if (!window.confirm(`Xoa user "${user.username}"?`)) {
      return;
    }

    await apiDelete(`/admin/users/${userId}`);
    setStatus($status, `Da xoa user ${user.username}.`, "success");
    await loadUsers();
  } catch (error) {
    console.error(error);
    setStatus($status, error.message || String(error), "error");
  }
}

async function main() {
  requireAdminPage();
  syncNav({ logoutTarget: "./login.html" });

  $tbody.addEventListener("click", handleTableClick);
  await loadUsers();
}

main().catch((error) => {
  console.error(error);
  setStatus($status, error.message || String(error), "error");
});
