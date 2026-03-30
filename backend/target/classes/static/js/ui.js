import { clearSession, getSession, isAdmin, resolveApiAsset } from "./api.js";
import { getCartCount } from "./store.js";

function createFallback(name, subtitle) {
  const fallback = document.createElement("div");
  fallback.className = "media-frame__fallback";

  const label = document.createElement("span");
  label.textContent = subtitle || "Smart Home";

  const title = document.createElement("strong");
  title.textContent = String(name || "Thiet bi").split(" ").slice(0, 3).join(" ");

  fallback.append(label, title);
  return fallback;
}

export function createMediaElement({ imageUrl, name, subtitle }) {
  const wrapper = document.createElement("div");
  wrapper.className = "media-frame";

  const showFallback = () => {
    wrapper.replaceChildren(createFallback(name, subtitle));
  };

  if (!imageUrl) {
    showFallback();
    return wrapper;
  }

  const image = document.createElement("img");
  image.className = "media-frame__image";
  image.src = resolveApiAsset(imageUrl);
  image.alt = name || "Product image";
  image.loading = "lazy";
  image.addEventListener("error", showFallback, { once: true });

  wrapper.append(image);
  return wrapper;
}

function clampRating(value) {
  const numericValue = Number(value || 0);
  if (!Number.isFinite(numericValue)) {
    return 0;
  }

  return Math.max(0, Math.min(5, numericValue));
}

export function createRatingStarsMarkup(rating) {
  const roundedRating = Math.round(clampRating(rating));
  return `
    <span class="rating-stars" aria-hidden="true">
      ${Array.from({ length: 5 }, (_, index) => `<span class="rating-stars__star${index < roundedRating ? " is-active" : ""}">${index < roundedRating ? "&#9733;" : "&#9734;"}</span>`).join("")}
    </span>
  `;
}

export function createRatingSummaryMarkup(averageRating, reviewCount, { large = false } = {}) {
  const totalReviews = Number(reviewCount || 0);
  const numericRating = clampRating(averageRating);
  const classes = ["rating-summary"];

  if (large) {
    classes.push("rating-summary--large");
  }

  if (totalReviews === 0) {
    return `
      <div class="${classes.join(" ")}">
        ${createRatingStarsMarkup(0)}
        <div class="rating-summary__copy">
          <strong>Chua co danh gia</strong>
          <small>Hay la nguoi dau tien gui nhan xet.</small>
        </div>
      </div>
    `;
  }

  return `
    <div class="${classes.join(" ")}">
      ${createRatingStarsMarkup(numericRating)}
      <div class="rating-summary__copy">
        <strong>${numericRating.toFixed(1)}/5</strong>
        <small>${totalReviews} danh gia</small>
      </div>
    </div>
  `;
}

function currentPageRef() {
  const url = new URL(window.location.href);
  const page = url.pathname.split("/").pop() || "index.html";
  return `${page}${url.search}`;
}

export function getProductDetailUrl(productId) {
  return `./product-detail.html?id=${encodeURIComponent(productId)}`;
}

export function buildLoginUrl(next = currentPageRef()) {
  return `./login.html?next=${encodeURIComponent(next)}`;
}

export function syncNav({
  authLink = document.getElementById("authLink"),
  userGreeting = document.getElementById("userGreeting"),
  cartCountBadge = document.getElementById("cartCountBadge"),
  adminLink = document.getElementById("adminLink"),
  logoutTarget = "./index.html",
} = {}) {
  const session = getSession();

  if (cartCountBadge) {
    cartCountBadge.textContent = String(getCartCount());
  }

  if (userGreeting) {
    userGreeting.textContent = session?.username ? `Xin chao, ${session.username}` : "Mua sam nhanh";
  }

  if (adminLink) {
    adminLink.hidden = !isAdmin();
  }

  if (!authLink) {
    return;
  }

  if (session?.token) {
    authLink.textContent = "Dang xuat";
    authLink.href = "#";
    authLink.onclick = (event) => {
      event.preventDefault();
      clearSession();
      window.location.href = logoutTarget;
    };
    return;
  }

  authLink.textContent = "Dang nhap";
  authLink.href = buildLoginUrl();
  authLink.onclick = null;
}

export function requireAdminPage() {
  const session = getSession();

  if (!session?.token) {
    window.location.href = buildLoginUrl(currentPageRef());
    throw new Error("Authentication required");
  }

  if (session.role !== "ADMIN") {
    window.location.href = "./index.html";
    throw new Error("Admin role required");
  }

  return session;
}
