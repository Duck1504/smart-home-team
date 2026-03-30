import { apiDelete, apiGet, apiPost, apiPut, escapeHtml, setStatus } from "./api.js";
import { requireAdminPage, syncNav } from "./ui.js";

const $form = document.getElementById("categoryForm");
const $formTitle = document.getElementById("categoryFormTitle");
const $status = document.getElementById("categoryStatus");
const $tbody = document.getElementById("categoriesTableBody");
const $cancelEdit = document.getElementById("cancelCategoryEdit");
const $reloadBtn = document.getElementById("reloadCategoriesBtn");

let categories = [];

function resetForm() {
  $form.reset();
  document.getElementById("categoryId").value = "";
  $formTitle.textContent = "Thêm danh mục mới";
  $cancelEdit.hidden = true;
}

function renderCategories() {
  if (categories.length === 0) {
    $tbody.innerHTML = `<tr><td colspan="3" class="table-empty">Chưa có danh mục nào.</td></tr>`;
    return;
  }

  $tbody.innerHTML = categories.map((category) => `
    <tr>
      <td><strong>${escapeHtml(category.name)}</strong></td>
      <td>${category.productCount}</td>
      <td>
        <div class="table-actions">
          <button class="btn btn--ghost btn--small" type="button" data-action="edit" data-id="${category.id}">Sửa</button>
          <button class="btn btn--ghost btn--small" type="button" data-action="delete" data-id="${category.id}">Xóa</button>
        </div>
      </td>
    </tr>
  `).join("");
}

async function loadCategories() {
  setStatus($status, "Dang tai danh muc...", "info");
  categories = await apiGet("/admin/categories");
  renderCategories();
  setStatus($status, `Da tai ${categories.length} danh muc.`, "success");
}

function fillForm(category) {
  document.getElementById("categoryId").value = category.id;
  document.getElementById("categoryName").value = category.name;
  $formTitle.textContent = `Chỉnh sửa: ${category.name}`;
  $cancelEdit.hidden = false;
}

async function handleSubmit(event) {
  event.preventDefault();
  const categoryId = document.getElementById("categoryId").value;
  const payload = {
    name: document.getElementById("categoryName").value.trim(),
  };

  setStatus($status, categoryId ? "Dang cap nhat danh muc..." : "Dang tao danh muc...", "info");

  try {
    if (categoryId) {
      await apiPut(`/admin/categories/${categoryId}`, payload);
      setStatus($status, "Cap nhat danh muc thanh cong.", "success");
    } else {
      await apiPost("/admin/categories", payload);
      setStatus($status, "Tao danh muc thanh cong.", "success");
    }
    resetForm();
    await loadCategories();
  } catch (error) {
    console.error(error);
    setStatus($status, error.message || String(error), "error");
  }
}

async function handleTableClick(event) {
  const button = event.target.closest("button[data-action]");
  if (!button) {
    return;
  }

  const category = categories.find((item) => item.id === Number(button.dataset.id));
  if (!category) {
    return;
  }

  if (button.dataset.action === "edit") {
    fillForm(category);
    return;
  }

  if (!window.confirm(`Xoa danh muc "${category.name}"?`)) {
    return;
  }

  try {
    await apiDelete(`/admin/categories/${category.id}`);
    setStatus($status, "Da xoa danh muc.", "success");
    if (String(category.id) === document.getElementById("categoryId").value) {
      resetForm();
    }
    await loadCategories();
  } catch (error) {
    console.error(error);
    setStatus($status, error.message || String(error), "error");
  }
}

async function main() {
  requireAdminPage();
  syncNav({ logoutTarget: "./login.html" });

  $form.addEventListener("submit", handleSubmit);
  $tbody.addEventListener("click", handleTableClick);
  $reloadBtn.addEventListener("click", loadCategories);
  $cancelEdit.addEventListener("click", resetForm);

  resetForm();
  await loadCategories();
}

main().catch((error) => {
  console.error(error);
  setStatus($status, error.message || String(error), "error");
});
