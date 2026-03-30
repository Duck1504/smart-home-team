const CART_KEY = "smarthome.cart";

function loadCart() {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY) || "{}");
  } catch {
    return {};
  }
}

function emitCartUpdated() {
  window.dispatchEvent(new CustomEvent("cart:updated"));
}

function saveCart(cart) {
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
  emitCartUpdated();
}

export function getCartItems() {
  return Object.entries(loadCart())
    .map(([productId, quantity]) => ({
      productId: Number(productId),
      quantity: Number(quantity),
    }))
    .filter((item) => item.productId > 0 && item.quantity > 0);
}

export function getCartCount() {
  return getCartItems().reduce((sum, item) => sum + item.quantity, 0);
}

export function setQuantity(productId, quantity) {
  const cart = loadCart();
  const nextQuantity = Math.max(0, Number(quantity) || 0);

  if (nextQuantity <= 0) {
    delete cart[String(productId)];
  } else {
    cart[String(productId)] = Math.floor(nextQuantity);
  }

  saveCart(cart);
}

export function addToCart(productId, quantity = 1) {
  const cart = loadCart();
  const key = String(productId);
  const nextQuantity = Math.max(1, Number(quantity) || 1);
  cart[key] = (cart[key] || 0) + Math.floor(nextQuantity);
  saveCart(cart);
}

export function clearCart() {
  localStorage.removeItem(CART_KEY);
  emitCartUpdated();
}
