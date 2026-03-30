import { apiGet, escapeHtml, money, setStatus } from "./api.js";
import { addToCart, getCartCount } from "./store.js";
import { createMediaElement, createRatingSummaryMarkup, getProductDetailUrl, syncNav } from "./ui.js";

const state = {
  products: [],
  categories: [],
};

const $grid = document.getElementById("productGrid");
const $cat = document.getElementById("categorySelect");
const $search = document.getElementById("searchInput");
const $refreshBtn = document.getElementById("refreshBtn");
const $statusMsg = document.getElementById("statusMsg");
const $authLink = document.getElementById("authLink");
const $userGreeting = document.getElementById("userGreeting");
const $cartCountBadge = document.getElementById("cartCountBadge");
const $categoryCount = document.getElementById("categoryCount");
const $productCount = document.getElementById("productCount");
const $cartCount = document.getElementById("cartCount");

function normalizeText(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
}

function updateHeaderSummary() {
  syncNav({
    authLink: $authLink,
    userGreeting: $userGreeting,
    cartCountBadge: $cartCountBadge,
  });
  $cartCount.textContent = String(getCartCount());
}

function renderEmptyState(message) {
  $grid.innerHTML = `
    <article class="panel empty-state">
      <strong>Khong co san pham phu hop.</strong>
      <p>${escapeHtml(message)}</p>
    </article>
  `;
  $productCount.textContent = "0";
}

function renderProductCard(product) {
  const article = document.createElement("article");
  article.className = "product-card";

  const media = createMediaElement({
    imageUrl: product.imageUrl,
    name: product.name,
    subtitle: product.categoryName || "Smart Home",
  });
  media.classList.add("product-card__media");

  const stockText = product.stock > 0 ? `Con ${product.stock}` : "Het hang";
  const stockClass = product.stock > 0 ? "stock" : "stock stock--out";

  const body = document.createElement("div");
  body.className = "product-card__body";
  body.innerHTML = `
    <div class="product-card__top">
      <span class="pill">${escapeHtml(product.categoryName || "Thiet bi")}</span>
      <span class="${stockClass}">${escapeHtml(stockText)}</span>
    </div>
    <h2 class="product-card__name">${escapeHtml(product.name)}</h2>
    <p class="product-card__desc">${escapeHtml(product.description || "Khong co mo ta.")}</p>
    ${createRatingSummaryMarkup(product.averageRating, product.reviewCount)}
    <div class="product-card__footer">
      <strong class="price">${money(product.price)}</strong>
      <div class="product-card__actions">
        <a class="btn btn--ghost btn--small" href="${escapeHtml(getProductDetailUrl(product.id))}">Chi tiet</a>
        <button class="btn btn--small" type="button" data-product-id="${product.id}" ${product.stock > 0 ? "" : "disabled"}>
          Them vao gio
        </button>
      </div>
    </div>
  `;

  article.append(media, body);
  return article;
}

function renderProducts() {
  const keyword = normalizeText($search.value);
  const filtered = state.products.filter((product) => {
    if (!keyword) {
      return true;
    }

    const haystack = normalizeText([product.name, product.description, product.categoryName].join(" "));
    return haystack.includes(keyword);
  });

  if (filtered.length === 0) {
    renderEmptyState("Thu doi danh muc hoac tu khoa tim kiem.");
    setStatus($statusMsg, "Khong tim thay san pham phu hop.", "info");
    return;
  }

  $grid.innerHTML = "";
  filtered.forEach((product) => {
    $grid.appendChild(renderProductCard(product));
  });

  $productCount.textContent = String(filtered.length);
  setStatus($statusMsg, `Hien thi ${filtered.length} san pham.`, "success");
}

async function loadCategories() {
  state.categories = await apiGet("/categories");
  state.categories.forEach((category) => {
    const option = document.createElement("option");
    option.value = category.id;
    option.textContent = category.name;
    $cat.appendChild(option);
  });

  $categoryCount.textContent = String(state.categories.length);
}

async function loadProducts() {
  const categoryId = $cat.value ? Number($cat.value) : null;
  const path = categoryId ? `/products?categoryId=${categoryId}` : "/products";

  $refreshBtn.disabled = true;
  setStatus($statusMsg, "Dang tai san pham...", "info");
  $grid.innerHTML = `<article class="panel empty-state"><strong>Dang tai du lieu...</strong><p>Vui long doi trong giay lat.</p></article>`;

  try {
    state.products = await apiGet(path);

    if (state.products.length === 0) {
      renderEmptyState("Danh muc nay hien chua co san pham.");
      setStatus($statusMsg, "Danh muc nay chua co du lieu.", "info");
      return;
    }

    renderProducts();
  } finally {
    $refreshBtn.disabled = false;
  }
}

function handleAddToCart(event) {
  const button = event.target.closest("button[data-product-id]");
  if (!button) {
    return;
  }

  const productId = Number(button.getAttribute("data-product-id"));
  addToCart(productId, 1);
  updateHeaderSummary();
  setStatus($statusMsg, "Da them san pham vao gio hang.", "success");

  const originalText = button.textContent;
  button.textContent = "Da them";
  button.disabled = true;

  window.setTimeout(() => {
    button.textContent = originalText;
    button.disabled = false;
  }, 900);
}

async function main() {
  updateHeaderSummary();

  window.addEventListener("cart:updated", updateHeaderSummary);
  window.addEventListener("session:updated", updateHeaderSummary);

  $grid.addEventListener("click", handleAddToCart);
  $search.addEventListener("input", renderProducts);
  $cat.addEventListener("change", loadProducts);
  $refreshBtn.addEventListener("click", loadProducts);

  await loadCategories();
  await loadProducts();
}

main().catch((error) => {
  console.error(error);
  renderEmptyState(error.message || String(error));
  setStatus($statusMsg, error.message || String(error), "error");
});
