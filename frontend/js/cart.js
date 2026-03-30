import { apiGet, apiPost, escapeHtml, formatDateTime, getSession, money, setStatus } from "./api.js";
import { clearCart, getCartCount, getCartItems, setQuantity } from "./store.js";
import { createMediaElement, syncNav } from "./ui.js";

const QR_PAYMENT_CONFIG = Object.freeze({
  providerBaseUrl: "https://img.vietqr.io/image",
  bankId: "970436",
  bankName: "Vietcombank",
  accountNumber: "1023456789",
  accountName: "SMART HOME STORE",
  template: "compact2",
  notePrefix: "SMARTHOME",
});

const $list = document.getElementById("cartList");
const $empty = document.getElementById("cartEmpty");
const $customerName = document.getElementById("customerName");
const $phoneNumber = document.getElementById("phoneNumber");
const $shipping = document.getElementById("shippingAddress");
const $paymentNote = document.getElementById("checkoutPaymentNote");
const $itemCount = document.getElementById("itemCount");
const $total = document.getElementById("totalAmount");
const $checkoutBtn = document.getElementById("checkoutBtn");
const $orderMsg = document.getElementById("orderMsg");
const $authLink = document.getElementById("authLink");
const $userGreeting = document.getElementById("userGreeting");
const $cartCountBadge = document.getElementById("cartCountBadge");
const $ordersHint = document.getElementById("ordersHint");
const $ordersList = document.getElementById("ordersList");
const $loadOrdersBtn = document.getElementById("loadOrdersBtn");
const paymentMethodInputs = Array.from(document.querySelectorAll('input[name="paymentMethod"]'));

let renderCartVersion = 0;
let latestCartSummary = { totalQuantity: 0, totalAmount: 0 };
let lastQrOrder = null;

function updateNavigation() {
  syncNav({
    authLink: $authLink,
    userGreeting: $userGreeting,
    cartCountBadge: $cartCountBadge,
  });
}

function getSelectedPaymentMethod() {
  return paymentMethodInputs.find((input) => input.checked)?.value || "";
}

function getPaymentMethodLabel(paymentMethod) {
  switch (paymentMethod) {
    case "CASH_ON_DELIVERY":
      return "Thanh toan khi nhan hang";
    case "QR_PAYMENT":
      return "Quet ma thanh toan";
    default:
      return paymentMethod || "--";
  }
}

function resetCheckoutForm() {
  $customerName.value = "";
  $phoneNumber.value = "";
  $shipping.value = "";

  paymentMethodInputs.forEach((input) => {
    input.checked = input.value === "CASH_ON_DELIVERY";
  });

  renderPaymentNote();
}

function isValidPhoneNumber(phoneNumber) {
  return /^[0-9+][0-9\s.-]{7,19}$/.test(phoneNumber);
}

function slugifyQrToken(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^A-Za-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .toUpperCase()
    .slice(0, 24);
}

function buildQrReference({ orderId, customerName }) {
  const customerToken = slugifyQrToken(customerName || "KHACH");
  if (orderId) {
    return `${QR_PAYMENT_CONFIG.notePrefix}-DH${orderId}-${customerToken || "KHACH"}`;
  }

  return `${QR_PAYMENT_CONFIG.notePrefix}-TAMTINH-${customerToken || "KHACH"}`;
}

function buildQrImageUrl({ amount, reference }) {
  const safeAmount = Math.max(0, Math.round(Number(amount) || 0));
  const params = new URLSearchParams({
    amount: String(safeAmount),
    addInfo: reference,
    accountName: QR_PAYMENT_CONFIG.accountName,
  });
  return `${QR_PAYMENT_CONFIG.providerBaseUrl}/${QR_PAYMENT_CONFIG.bankId}-${QR_PAYMENT_CONFIG.accountNumber}-${QR_PAYMENT_CONFIG.template}.png?${params.toString()}`;
}

function buildQrContext(source) {
  return {
    mode: source.mode || "ready",
    amount: Math.max(0, Math.round(Number(source.amount) || 0)),
    customerName: source.customerName || $customerName.value.trim(),
    orderId: source.orderId || null,
    heading: source.heading,
    hint: source.hint,
  };
}

function hasQrPreviewInfo() {
  return Boolean(
    $customerName.value.trim()
    && isValidPhoneNumber($phoneNumber.value.trim())
    && $shipping.value.trim()
    && latestCartSummary.totalAmount > 0
  );
}

function getCurrentQrContext() {
  if (getSelectedPaymentMethod() === "QR_PAYMENT" && latestCartSummary.totalAmount > 0) {
    if (!hasQrPreviewInfo()) {
      return buildQrContext({
        mode: "pending",
        heading: "Thanh toan QR",
        hint: "Nhap ho ten, so dien thoai hop le va dia chi nhan hang de hien ma QR tam tinh.",
      });
    }

    return buildQrContext({
      amount: latestCartSummary.totalAmount,
      heading: "Quet ma thanh toan cho don hang",
      hint: "Ma QR tam tinh se cap nhat lai theo ma don sau khi ban bam Dat hang.",
    });
  }

  if (lastQrOrder) {
    return buildQrContext({
      amount: lastQrOrder.amount,
      customerName: lastQrOrder.customerName,
      orderId: lastQrOrder.orderId,
      heading: `QR cho don #${lastQrOrder.orderId}`,
      hint: "Don hang da duoc tao. Vui long quet ma duoi day va ghi dung noi dung chuyen khoan.",
    });
  }

  return null;
}

function renderPaymentNote() {
  const qrContext = getCurrentQrContext();
  if (!qrContext) {
    $paymentNote.hidden = true;
    $paymentNote.innerHTML = "";
    return;
  }

  const reference = buildQrReference(qrContext);
  const qrImageUrl = buildQrImageUrl({ amount: qrContext.amount, reference });

  $paymentNote.hidden = false;
  if (qrContext.mode === "pending") {
    $paymentNote.innerHTML = `
      <div class="payment-qr__header">
        <div>
          <strong>${escapeHtml(qrContext.heading)}</strong>
          <p>${escapeHtml(qrContext.hint)}</p>
        </div>
        <span class="pill">QR Payment</span>
      </div>
    `;
    return;
  }

  $paymentNote.innerHTML = `
    <div class="payment-qr__header">
      <div>
        <strong>${escapeHtml(qrContext.heading)}</strong>
        <p>${escapeHtml(qrContext.hint)}</p>
      </div>
      <span class="pill">QR Payment</span>
    </div>
    <div class="payment-qr">
      <div class="payment-qr__media">
        <img data-role="payment-qr-image" class="payment-qr__image" src="${escapeHtml(qrImageUrl)}" alt="Ma QR thanh toan" />
        <div data-role="payment-qr-fallback" class="payment-qr__fallback" hidden>
          <strong>Khong tai duoc hinh QR.</strong>
          <p>Ban van co the chuyen khoan thu cong theo thong tin ben canh.</p>
        </div>
      </div>
      <div class="payment-qr__details">
        <div class="payment-qr__row">
          <span>Ngan hang</span>
          <strong>${escapeHtml(QR_PAYMENT_CONFIG.bankName)}</strong>
        </div>
        <div class="payment-qr__row">
          <span>So tai khoan</span>
          <strong>${escapeHtml(QR_PAYMENT_CONFIG.accountNumber)}</strong>
        </div>
        <div class="payment-qr__row">
          <span>Chu tai khoan</span>
          <strong>${escapeHtml(QR_PAYMENT_CONFIG.accountName)}</strong>
        </div>
        <div class="payment-qr__row">
          <span>Noi dung chuyen khoan</span>
          <strong>${escapeHtml(reference)}</strong>
        </div>
        <div class="payment-qr__row payment-qr__row--total">
          <span>So tien</span>
          <strong>${escapeHtml(money(qrContext.amount))}</strong>
        </div>
      </div>
    </div>
  `;

  const qrImage = $paymentNote.querySelector('[data-role="payment-qr-image"]');
  const qrFallback = $paymentNote.querySelector('[data-role="payment-qr-fallback"]');
  if (qrImage && qrFallback) {
    qrImage.addEventListener("error", () => {
      qrFallback.hidden = false;
      qrImage.hidden = true;
    }, { once: true });
  }
}

async function loadCartRows() {
  const cartItems = getCartItems();
  const rows = await Promise.all(
    cartItems.map(async (item) => {
      try {
        const product = await apiGet(`/products/${item.productId}`);
        return { ...item, product };
      } catch {
        setQuantity(item.productId, 0);
        return null;
      }
    })
  );

  return rows.filter(Boolean);
}

function createCartRow(row) {
  const article = document.createElement("article");
  article.className = "cart-item";

  const media = createMediaElement({
    imageUrl: row.product.imageUrl,
    name: row.product.name,
    subtitle: row.product.categoryName || "Smart Home",
  });
  media.classList.add("cart-item__media");

  const subtotal = Number(row.product.price) * row.quantity;
  const stockText = row.product.stock > 0 ? `Con ${row.product.stock} san pham` : "San pham da het hang";

  const content = document.createElement("div");
  content.className = "cart-item__content";
  content.innerHTML = `
    <div class="cart-item__header">
      <div>
        <h2>${escapeHtml(row.product.name)}</h2>
        <p>${escapeHtml(row.product.categoryName || "Thiet bi thong minh")}</p>
      </div>
      <strong>${money(subtotal)}</strong>
    </div>
    <div class="cart-item__meta">
      <span>Don gia: ${money(row.product.price)}</span>
      <span>${escapeHtml(stockText)}</span>
    </div>
    <div class="cart-item__controls">
      <div class="stepper">
        <button class="btn btn--ghost btn--small" type="button" data-action="minus" data-product-id="${row.product.id}">-</button>
        <input class="qty-input" type="number" min="1" step="1" value="${row.quantity}" data-product-id="${row.product.id}" />
        <button class="btn btn--ghost btn--small" type="button" data-action="plus" data-product-id="${row.product.id}">+</button>
      </div>
      <button class="btn btn--ghost btn--small" type="button" data-action="remove" data-product-id="${row.product.id}">Xoa</button>
    </div>
  `;

  article.append(media, content);
  return article;
}

async function renderCart() {
  const version = ++renderCartVersion;
  updateNavigation();
  $list.innerHTML = "";
  setStatus($orderMsg, "", "info");

  const rows = await loadCartRows();
  if (version !== renderCartVersion) {
    return;
  }

  const totalQuantity = rows.reduce((sum, row) => sum + row.quantity, 0);
  const totalAmount = rows.reduce((sum, row) => sum + Number(row.product.price) * row.quantity, 0);
  latestCartSummary = { totalQuantity, totalAmount };

  $itemCount.textContent = String(totalQuantity);
  $total.textContent = money(totalAmount);
  renderPaymentNote();

  if (rows.length === 0) {
    $empty.hidden = false;
    $checkoutBtn.disabled = true;
    return;
  }

  $empty.hidden = true;
  rows.forEach((row) => {
    $list.appendChild(createCartRow(row));
  });

  $checkoutBtn.disabled = false;
}

function handleCartClick(event) {
  const button = event.target.closest("button[data-action]");
  if (!button) {
    return;
  }

  const productId = Number(button.getAttribute("data-product-id"));
  const action = button.getAttribute("data-action");
  const currentQuantity = getCartItems().find((item) => item.productId === productId)?.quantity || 0;

  if (action === "remove") {
    setQuantity(productId, 0);
  } else if (action === "plus") {
    setQuantity(productId, currentQuantity + 1);
  } else if (action === "minus") {
    setQuantity(productId, currentQuantity - 1);
  }
}

function handleQtyChange(event) {
  const input = event.target.closest("input.qty-input");
  if (!input) {
    return;
  }

  const productId = Number(input.getAttribute("data-product-id"));
  setQuantity(productId, Number(input.value));
}

function renderOrders(orders) {
  $ordersList.innerHTML = "";

  if (orders.length === 0) {
    setStatus($ordersHint, "Ban chua co don hang nao.", "info");
    return;
  }

  setStatus($ordersHint, "", "info");

  orders.forEach((order) => {
    const article = document.createElement("article");
    article.className = "order-card";
    article.innerHTML = `
      <div class="order-card__top">
        <div>
          <strong>Don #${order.id}</strong>
          <p>${escapeHtml(formatDateTime(order.createdAt))}</p>
        </div>
        <span class="pill">${escapeHtml(order.status)}</span>
      </div>
      <div class="order-card__summary">
        <div class="order-card__details">
          <span><strong>Nguoi nhan:</strong> ${escapeHtml(order.customerName || "--")}</span>
          <span><strong>So dien thoai:</strong> ${escapeHtml(order.phoneNumber || "--")}</span>
          <span><strong>Dia chi:</strong> ${escapeHtml(order.shippingAddress || "--")}</span>
          <span><strong>Thanh toan:</strong> ${escapeHtml(getPaymentMethodLabel(order.paymentMethod))}</span>
        </div>
        <strong>${money(order.total)}</strong>
      </div>
      <ul class="order-card__items">
        ${order.items.map((item) => `<li>${escapeHtml(item.productName)} x ${item.quantity} <strong>${money(item.unitPrice)}</strong></li>`).join("")}
      </ul>
    `;
    $ordersList.appendChild(article);
  });
}

async function loadOrders() {
  const session = getSession();
  $ordersList.innerHTML = "";

  if (!session?.token) {
    setStatus($ordersHint, "Dang nhap de xem lich su don hang cua ban.", "info");
    $loadOrdersBtn.disabled = true;
    return;
  }

  $loadOrdersBtn.disabled = false;
  setStatus($ordersHint, "Dang tai don hang...", "info");

  try {
    const orders = await apiGet("/orders");
    orders.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    renderOrders(orders);
  } catch (error) {
    console.error(error);
    setStatus($ordersHint, error.message || String(error), "error");
  }
}

async function checkout() {
  const session = getSession();
  if (!session?.token) {
    window.location.href = "./login.html?next=./cart.html";
    return;
  }

  const customerName = $customerName.value.trim();
  const phoneNumber = $phoneNumber.value.trim();
  const shippingAddress = $shipping.value.trim();
  const paymentMethod = getSelectedPaymentMethod();

  if (!customerName) {
    setStatus($orderMsg, "Vui long nhap ho va ten nguoi nhan.", "error");
    return;
  }

  if (!phoneNumber) {
    setStatus($orderMsg, "Vui long nhap so dien thoai giao hang.", "error");
    return;
  }

  if (!isValidPhoneNumber(phoneNumber)) {
    setStatus($orderMsg, "So dien thoai khong hop le.", "error");
    return;
  }

  if (!shippingAddress) {
    setStatus($orderMsg, "Vui long nhap dia chi nhan hang.", "error");
    return;
  }

  if (!paymentMethod) {
    setStatus($orderMsg, "Vui long chon phuong thuc thanh toan.", "error");
    return;
  }

  const items = getCartItems();
  if (items.length === 0 || getCartCount() === 0) {
    setStatus($orderMsg, "Gio hang dang trong.", "error");
    return;
  }

  $checkoutBtn.disabled = true;
  setStatus($orderMsg, "Dang tao don hang...", "info");

  try {
    const order = await apiPost("/orders", {
      customerName,
      phoneNumber,
      shippingAddress,
      paymentMethod,
      items: items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      })),
    });

    lastQrOrder = order.paymentMethod === "QR_PAYMENT"
      ? {
        orderId: order.id,
        amount: Number(order.total) || 0,
        customerName,
      }
      : null;

    clearCart();
    resetCheckoutForm();
    await renderCart();
    await loadOrders();
    setStatus(
      $orderMsg,
      order.paymentMethod === "QR_PAYMENT"
        ? `Da tao don hang #${order.id}. Ma QR thanh toan da duoc hien ben duoi.`
        : `Da tao don hang thanh cong. Ma don: #${order.id}. Phuong thuc: ${getPaymentMethodLabel(order.paymentMethod)}.`,
      "success"
    );
  } catch (error) {
    console.error(error);
    setStatus($orderMsg, error.message || String(error), "error");
  } finally {
    $checkoutBtn.disabled = false;
  }
}

async function main() {
  updateNavigation();
  renderPaymentNote();

  window.addEventListener("cart:updated", () => {
    renderCart().catch((error) => {
      console.error(error);
      setStatus($orderMsg, error.message || String(error), "error");
    });
  });

  window.addEventListener("session:updated", () => {
    updateNavigation();
    loadOrders().catch((error) => {
      console.error(error);
      setStatus($ordersHint, error.message || String(error), "error");
    });
  });

  $list.addEventListener("click", handleCartClick);
  $list.addEventListener("change", handleQtyChange);
  $checkoutBtn.addEventListener("click", checkout);
  $loadOrdersBtn.addEventListener("click", loadOrders);
  paymentMethodInputs.forEach((input) => {
    input.addEventListener("change", renderPaymentNote);
  });
  [$customerName, $phoneNumber, $shipping].forEach((input) => {
    input.addEventListener("input", renderPaymentNote);
  });

  await renderCart();
  await loadOrders();
}

main().catch((error) => {
  console.error(error);
  setStatus($orderMsg, error.message || String(error), "error");
  setStatus($ordersHint, error.message || String(error), "error");
});
