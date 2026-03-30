import { apiDelete, apiGet, apiPost, apiPut, escapeHtml, money, setStatus } from "./api.js";
import { requireAdminPage, syncNav } from "./ui.js";

const $form = document.getElementById("productForm");
const $formTitle = document.getElementById("productFormTitle");
const $status = document.getElementById("productStatus");
const $tbody = document.getElementById("productsTableBody");
const $categorySelect = document.getElementById("productCategory");
const $cancelEdit = document.getElementById("cancelProductEdit");
const $reloadBtn = document.getElementById("reloadProductsBtn");

let products = [];
let categories = [];

function resetForm() {
  $form.reset();
  document.getElementById("productId").value = "";
  document.getElementById("productTechnicalSpecs").value = "";
  document.getElementById("productCostPrice").value = "";
  $formTitle.textContent = "Them san pham moi";
  $cancelEdit.hidden = true;
}

function renderCategoryOptions() {
  $categorySelect.innerHTML = categories.map((category) => `
    <option value="${category.id}">${escapeHtml(category.name)}</option>
  `).join("");
}

function fillForm(product) {
  document.getElementById("productId").value = product.id;
  document.getElementById("productName").value = product.name;
  document.getElementById("productDescription").value = product.description;
  document.getElementById("productTechnicalSpecs").value = product.technicalSpecs || "";
  document.getElementById("productCategory").value = String(product.categoryId);
  document.getElementById("productPrice").value = product.price;
  document.getElementById("productCostPrice").value = product.costPrice ?? 0;
  document.getElementById("productStock").value = product.stock;
  document.getElementById("productImageUrl").value = product.imageUrl || "";
  $formTitle.textContent = `Chinh sua: ${product.name}`;
  $cancelEdit.hidden = false;
}

function renderProducts() {
  if (products.length === 0) {
    $tbody.innerHTML = `<tr><td colspan="6" class="table-empty">Chua co san pham nao.</td></tr>`;
    return;
  }

  $tbody.innerHTML = products.map((product) => `
    <tr>
      <td>
        <strong>${escapeHtml(product.name)}</strong>
        <div class="table-subtext">${escapeHtml(product.description || "")}</div>
      </td>
      <td>${escapeHtml(product.categoryName || "--")}</td>
      <td>${money(product.price)}</td>
      <td>${money(product.costPrice || 0)}</td>
      <td>${product.stock}</td>
      <td>
        <div class="table-actions">
          <button class="btn btn--ghost btn--small" type="button" data-action="edit" data-id="${product.id}">Sua</button>
          <button class="btn btn--ghost btn--small" type="button" data-action="delete" data-id="${product.id}">Xoa</button>
        </div>
      </td>
    </tr>
  `).join("");
}

async function loadCategories() {
  categories = await apiGet("/categories");
  renderCategoryOptions();
}

async function loadProducts() {
  setStatus($status, "Dang tai du lieu san pham...", "info");
  products = await apiGet("/admin/products");
  renderProducts();
  setStatus($status, `Da tai ${products.length} san pham.`, "success");
}

async function handleSubmit(event) {
  event.preventDefault();
  const productId = document.getElementById("productId").value;

  const payload = {
    name: document.getElementById("productName").value.trim(),
    description: document.getElementById("productDescription").value.trim(),
    technicalSpecs: document.getElementById("productTechnicalSpecs").value.trim() || null,
    categoryId: Number(document.getElementById("productCategory").value),
    price: Number(document.getElementById("productPrice").value),
    costPrice: Number(document.getElementById("productCostPrice").value),
    stock: Number(document.getElementById("productStock").value),
    imageUrl: document.getElementById("productImageUrl").value.trim() || null,
  };

  setStatus($status, productId ? "Dang cap nhat san pham..." : "Dang tao san pham...", "info");

  try {
    if (productId) {
      await apiPut(`/admin/products/${productId}`, payload);
      setStatus($status, "Cap nhat san pham thanh cong.", "success");
    } else {
      await apiPost("/admin/products", payload);
      setStatus($status, "Tao san pham thanh cong.", "success");
    }

    resetForm();
    await loadProducts();
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

  const id = Number(button.dataset.id);
  const product = products.find((item) => item.id === id);
  if (!product) {
    return;
  }

  if (button.dataset.action === "edit") {
    fillForm(product);
    return;
  }

  if (!window.confirm(`Xoa san pham "${product.name}"?`)) {
    return;
  }

  try {
    await apiDelete(`/admin/products/${id}`);
    setStatus($status, "Da xoa san pham.", "success");
    if (String(id) === document.getElementById("productId").value) {
      resetForm();
    }
    await loadProducts();
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
  $reloadBtn.addEventListener("click", loadProducts);
  $cancelEdit.addEventListener("click", resetForm);

  await loadCategories();
  resetForm();
  await loadProducts();
}

main().catch((error) => {
  console.error(error);
  setStatus($status, error.message || String(error), "error");
});
