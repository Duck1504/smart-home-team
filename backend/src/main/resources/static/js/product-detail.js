import { apiGet, apiPost, escapeHtml, formatDateTime, getSession, money, setStatus } from "./api.js";
import { addToCart } from "./store.js";
import { buildLoginUrl, createMediaElement, createRatingStarsMarkup, createRatingSummaryMarkup, syncNav } from "./ui.js";

const $pageStatus = document.getElementById("pageStatus");
const $productDetailPanel = document.getElementById("productDetailPanel");
const $productMedia = document.getElementById("productMedia");
const $productCategory = document.getElementById("productCategory");
const $productStock = document.getElementById("productStock");
const $productName = document.getElementById("productName");
const $productRating = document.getElementById("productRating");
const $productDescription = document.getElementById("productDescription");
const $productPrice = document.getElementById("productPrice");
const $productCategoryText = document.getElementById("productCategoryText");
const $productSpecsSection = document.getElementById("productSpecsSection");
const $productSpecsList = document.getElementById("productSpecsList");
const $addToCartBtn = document.getElementById("addToCartBtn");
const $reviewSummary = document.getElementById("reviewSummary");
const $reviewsList = document.getElementById("reviewsList");
const $reviewAuthHint = document.getElementById("reviewAuthHint");
const $reviewForm = document.getElementById("reviewForm");
const $reviewComment = document.getElementById("reviewComment");
const $submitReviewBtn = document.getElementById("submitReviewBtn");
const $reviewStatus = document.getElementById("reviewStatus");
const $authLink = document.getElementById("authLink");
const $userGreeting = document.getElementById("userGreeting");
const $cartCountBadge = document.getElementById("cartCountBadge");
const ratingInputs = Array.from(document.querySelectorAll('input[name="rating"]'));
const starLabels = Array.from(document.querySelectorAll(".star-picker__label"));

let currentProduct = null;

function resolveProductId() {
  const url = new URL(window.location.href);
  const value = Number(url.searchParams.get("id"));
  return Number.isInteger(value) && value > 0 ? value : null;
}

const productId = resolveProductId();

function updateNavigation() {
  syncNav({
    authLink: $authLink,
    userGreeting: $userGreeting,
    cartCountBadge: $cartCountBadge,
    logoutTarget: window.location.pathname.split("/").pop() + window.location.search,
  });
}

function setReviewFormDisabled(disabled) {
  Array.from($reviewForm.elements).forEach((element) => {
    element.disabled = disabled;
  });
}

function getSelectedRating() {
  return Number(ratingInputs.find((input) => input.checked)?.value || 0);
}

function setSelectedRating(rating) {
  const numericRating = Number(rating || 0);
  ratingInputs.forEach((input) => {
    input.checked = Number(input.value) === numericRating;
  });
  syncStarPicker();
}

function syncStarPicker() {
  const selectedRating = getSelectedRating();
  starLabels.forEach((label) => {
    label.classList.toggle("is-active", Number(label.dataset.value) <= selectedRating);
  });
}

function parseTechnicalSpecs(rawSpecs) {
  return String(rawSpecs || "")
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const separatorIndex = line.indexOf(":");
      if (separatorIndex === -1) {
        return { label: "Thong tin", value: line, wide: true };
      }

      return {
        label: line.slice(0, separatorIndex).trim(),
        value: line.slice(separatorIndex + 1).trim() || "--",
        wide: false,
      };
    });
}

function renderTechnicalSpecs(rawSpecs) {
  const specs = parseTechnicalSpecs(rawSpecs);
  if (specs.length === 0) {
    $productSpecsSection.hidden = true;
    $productSpecsList.innerHTML = "";
    return;
  }

  $productSpecsSection.hidden = false;
  $productSpecsList.innerHTML = specs.map((spec) => `
    <article class="spec-card${spec.wide ? " spec-card--wide" : ""}">
      <span>${escapeHtml(spec.label)}</span>
      <strong>${escapeHtml(spec.value)}</strong>
    </article>
  `).join("");
}

function renderProduct(product) {
  const media = createMediaElement({
    imageUrl: product.imageUrl,
    name: product.name,
    subtitle: product.categoryName || "Smart Home",
  });

  $productMedia.replaceChildren(media);
  $productCategory.textContent = product.categoryName || "Thiet bi";
  $productCategoryText.textContent = product.categoryName || "--";
  $productStock.textContent = product.stock > 0 ? `Con ${product.stock} san pham` : "San pham dang het hang";
  $productStock.className = product.stock > 0 ? "stock" : "stock stock--out";
  $productName.textContent = product.name || "San pham";
  $productDescription.textContent = product.description || "Khong co mo ta cho san pham nay.";
  $productPrice.textContent = money(product.price);
  $productRating.innerHTML = createRatingSummaryMarkup(product.averageRating, product.reviewCount, { large: true });
  $reviewSummary.innerHTML = createRatingSummaryMarkup(product.averageRating, product.reviewCount);
  renderTechnicalSpecs(product.technicalSpecs);
  $addToCartBtn.disabled = !(product.stock > 0);
  $addToCartBtn.textContent = product.stock > 0 ? "Them vao gio" : "Het hang";
  $productDetailPanel.hidden = false;
  document.title = `${product.name || "Chi tiet san pham"} | Smart Home Store`;
}

function renderReviews(reviews) {
  if (!Array.isArray(reviews) || reviews.length === 0) {
    $reviewsList.innerHTML = `
      <article class="panel panel--soft empty-state">
        <strong>Chua co danh gia nao.</strong>
        <p>Hay la nguoi dau tien chia se trai nghiem ve san pham nay.</p>
      </article>
    `;
    return;
  }

  $reviewsList.innerHTML = reviews.map((review) => `
    <article class="review-card">
      <div class="review-card__header">
        <div>
          <strong>${escapeHtml(review.username || "Nguoi dung")}</strong>
          <p>${escapeHtml(formatDateTime(review.submittedAt))}</p>
        </div>
        <div class="review-card__rating">
          ${createRatingStarsMarkup(review.rating)}
          <strong>${Number(review.rating || 0)}/5</strong>
        </div>
      </div>
      <p class="review-card__comment">${escapeHtml(review.comment || "Khach hang khong de lai nhan xet.")}</p>
    </article>
  `).join("");
}

function renderReviewForm(product) {
  const session = getSession();

  if (!session?.token) {
    $reviewAuthHint.hidden = false;
    $reviewAuthHint.innerHTML = `
      <strong>Can dang nhap de danh gia.</strong>
      <p>Dang nhap de cham diem tu 1 den 5 sao va gui nhan xet cho san pham nay.</p>
      <a class="btn btn--ghost" href="${escapeHtml(buildLoginUrl())}">Dang nhap ngay</a>
    `;
    setReviewFormDisabled(true);
    setSelectedRating(0);
    $reviewComment.value = "";
    $submitReviewBtn.textContent = "Gui danh gia";
    return;
  }

  const existingReview = (product.reviews || []).find((review) => review.username === session.username);

  $reviewAuthHint.hidden = true;
  $reviewAuthHint.innerHTML = "";
  setReviewFormDisabled(false);
  setSelectedRating(existingReview?.rating || 0);
  $reviewComment.value = existingReview?.comment || "";
  $submitReviewBtn.textContent = existingReview ? "Cap nhat danh gia" : "Gui danh gia";
}

async function loadProduct() {
  if (!productId) {
    throw new Error("Thieu ma san pham hop le trong duong dan.");
  }

  setStatus($pageStatus, "Dang tai chi tiet san pham...", "info");
  const product = await apiGet(`/products/${productId}`);
  currentProduct = product;
  renderProduct(product);
  renderReviews(product.reviews || []);
  renderReviewForm(product);
  setStatus($pageStatus, "", "info");
}

function handleAddToCart() {
  if (!currentProduct || !(currentProduct.stock > 0)) {
    return;
  }

  addToCart(currentProduct.id, 1);
  updateNavigation();
  setStatus($pageStatus, "Da them san pham vao gio hang.", "success");

  const originalText = $addToCartBtn.textContent;
  $addToCartBtn.textContent = "Da them";
  $addToCartBtn.disabled = true;

  window.setTimeout(() => {
    if (!currentProduct) {
      return;
    }

    $addToCartBtn.textContent = originalText;
    $addToCartBtn.disabled = !(currentProduct.stock > 0);
  }, 900);
}

async function submitReview(event) {
  event.preventDefault();

  const session = getSession();
  if (!session?.token) {
    window.location.href = buildLoginUrl();
    return;
  }

  const rating = getSelectedRating();
  if (rating < 1 || rating > 5) {
    setStatus($reviewStatus, "Vui long chon so sao tu 1 den 5.", "error");
    return;
  }

  setReviewFormDisabled(true);
  setStatus($reviewStatus, "Dang gui danh gia...", "info");

  try {
    await apiPost(`/products/${productId}/reviews`, {
      rating,
      comment: $reviewComment.value.trim(),
    });
    await loadProduct();
    setStatus($reviewStatus, "Da luu danh gia cua ban.", "success");
  } catch (error) {
    console.error(error);
    setStatus($reviewStatus, error.message || String(error), "error");
    if (currentProduct) {
      renderReviewForm(currentProduct);
    }
  }
}

async function main() {
  updateNavigation();
  syncStarPicker();

  if (!productId) {
    setStatus($pageStatus, "Khong tim thay ma san pham trong duong dan.", "error");
    setReviewFormDisabled(true);
    return;
  }

  window.addEventListener("cart:updated", updateNavigation);
  window.addEventListener("session:updated", () => {
    updateNavigation();
    if (currentProduct) {
      renderReviewForm(currentProduct);
    }
  });

  ratingInputs.forEach((input) => {
    input.addEventListener("change", syncStarPicker);
  });
  $addToCartBtn.addEventListener("click", handleAddToCart);
  $reviewForm.addEventListener("submit", submitReview);

  await loadProduct();
}

main().catch((error) => {
  console.error(error);
  setStatus($pageStatus, error.message || String(error), "error");
  renderReviews([]);
  setReviewFormDisabled(true);
});
