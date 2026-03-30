const AUTH_KEY = "smarthome.session";
const LEGACY_TOKEN_KEY = "token";

function resolveApiBase() {
  if (window.location.protocol === "file:") {
    return "http://localhost:8080/api";
  }

  const { hostname, origin, port, protocol } = window.location;
  if (port === "8080") {
    return `${origin}/api`;
  }

  if (hostname === "localhost" || hostname === "127.0.0.1") {
    return `${protocol}//${hostname}:8080/api`;
  }

  return `${origin}/api`;
}

export const API_BASE = resolveApiBase();

function emitSessionUpdated() {
  window.dispatchEvent(new CustomEvent("session:updated"));
}

export function getSession() {
  try {
    const raw = localStorage.getItem(AUTH_KEY);
    if (raw) {
      return JSON.parse(raw);
    }
  } catch {
  }

  const legacyToken = localStorage.getItem(LEGACY_TOKEN_KEY) || "";
  return legacyToken ? { token: legacyToken, username: "", role: "" } : null;
}

export function getToken() {
  return getSession()?.token || "";
}

export function getRole() {
  return getSession()?.role || "";
}

export function isAuthenticated() {
  return Boolean(getToken());
}

export function isAdmin() {
  return getRole() === "ADMIN";
}

export function saveSession(authResponse) {
  if (!authResponse?.token) {
    return null;
  }

  const session = {
    token: authResponse.token,
    username: authResponse.username || "",
    role: authResponse.role || "",
  };

  localStorage.setItem(AUTH_KEY, JSON.stringify(session));
  localStorage.setItem(LEGACY_TOKEN_KEY, session.token);
  emitSessionUpdated();
  return session;
}

export function setToken(token) {
  return saveSession({ token });
}

export function clearSession() {
  localStorage.removeItem(AUTH_KEY);
  localStorage.removeItem(LEGACY_TOKEN_KEY);
  emitSessionUpdated();
}

export const clearToken = clearSession;

export async function apiFetch(path, options = {}) {
  const token = getToken();
  const headers = new Headers(options.headers || {});

  if (options.body !== undefined && !(options.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json; charset=utf-8");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers,
    });
  } catch {
    throw new Error("Khong the ket noi toi backend. Hay kiem tra server Spring Boot dang chay o cong 8080.");
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    throw new Error(data?.message || data || `Request failed (${response.status})`);
  }

  return data;
}

export function resolveApiAsset(path) {
  if (!path) {
    return "";
  }

  if (/^(https?:|data:)/i.test(path)) {
    return path;
  }

  try {
    const origin = new URL(API_BASE).origin;
    return new URL(path, `${origin}/`).toString();
  } catch {
    return path;
  }
}

export async function apiGet(path) {
  return apiFetch(path, { method: "GET" });
}

export async function apiPost(path, body) {
  return apiFetch(path, { method: "POST", body: JSON.stringify(body) });
}

export async function apiPut(path, body) {
  return apiFetch(path, { method: "PUT", body: JSON.stringify(body) });
}

export async function apiDelete(path) {
  return apiFetch(path, { method: "DELETE" });
}

export function money(value) {
  const amount = typeof value === "string" ? Number(value) : value;
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(amount || 0);
}

export function formatDateTime(value) {
  if (!value) {
    return "--";
  }

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(value));
}

export function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

export function setStatus(element, message, tone = "info") {
  if (!element) {
    return;
  }

  element.textContent = message || "";
  element.dataset.tone = tone;
  element.hidden = !message;
}
