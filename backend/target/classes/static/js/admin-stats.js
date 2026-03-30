import { apiGet, escapeHtml, formatDateTime, money, setStatus } from "./api.js";
import { requireAdminPage, syncNav } from "./ui.js";

const $status = document.getElementById("statsStatus");
const $cards = document.getElementById("statsCards");
const $productSalesBody = document.getElementById("productSalesBody");
const $categorySalesBody = document.getElementById("categorySalesBody");
const $recentOrdersBody = document.getElementById("recentOrdersBody");
const $reloadBtn = document.getElementById("reloadStatsBtn");
const $printBtn = document.getElementById("printStatsBtn");
const $reportMeta = document.getElementById("reportMeta");

function getPaymentMethodLabel(paymentMethod) {
  switch (paymentMethod) {
    case "CASH_ON_DELIVERY":
      return "Thanh toan khi nhan hang";
    case "QR_PAYMENT":
      return "Quet ma QR";
    default:
      return paymentMethod || "--";
  }
}

function renderCards(stats) {
  const cards = [
    { label: "Tong don hang", value: stats.totalOrders },
    { label: "Tong san pham da ban", value: stats.totalUnitsSold },
    { label: "Tong thu", value: money(stats.totalRevenue) },
    { label: "Tong chi", value: money(stats.totalExpense) },
    { label: "Loi nhuan tam tinh", value: money(stats.totalProfit) },
    { label: "San pham sap het hang", value: stats.lowStockProducts },
  ];

  $cards.innerHTML = cards.map((card) => `
    <article class="metric-card">
      <span>${escapeHtml(card.label)}</span>
      <strong>${escapeHtml(card.value)}</strong>
    </article>
  `).join("");
}

function renderProductSales(items) {
  if (!items.length) {
    $productSalesBody.innerHTML = `<tr><td colspan="7" class="table-empty">Chua co du lieu ban hang.</td></tr>`;
    return;
  }

  $productSalesBody.innerHTML = items.map((item) => `
    <tr>
      <td>${escapeHtml(item.productName)}</td>
      <td>${escapeHtml(item.categoryName || "--")}</td>
      <td>${item.soldQuantity}</td>
      <td>${item.stock}</td>
      <td>${money(item.revenue)}</td>
      <td>${money(item.expense)}</td>
      <td>${money(item.profit)}</td>
    </tr>
  `).join("");
}

function renderCategorySales(items) {
  if (!items.length) {
    $categorySalesBody.innerHTML = `<tr><td colspan="5" class="table-empty">Chua co du lieu danh muc.</td></tr>`;
    return;
  }

  $categorySalesBody.innerHTML = items.map((item) => `
    <tr>
      <td>${escapeHtml(item.categoryName)}</td>
      <td>${item.soldQuantity}</td>
      <td>${money(item.revenue)}</td>
      <td>${money(item.expense)}</td>
      <td>${money(item.profit)}</td>
    </tr>
  `).join("");
}

function renderRecentOrders(items) {
  if (!items.length) {
    $recentOrdersBody.innerHTML = `<tr><td colspan="6" class="table-empty">Chua co don hang nao.</td></tr>`;
    return;
  }

  $recentOrdersBody.innerHTML = items.map((item) => `
    <tr>
      <td>#${item.id}</td>
      <td>${escapeHtml(item.username)}</td>
      <td>${escapeHtml(getPaymentMethodLabel(item.paymentMethod))}</td>
      <td>${escapeHtml(item.status)}</td>
      <td>${escapeHtml(formatDateTime(item.createdAt))}</td>
      <td>${money(item.total)}</td>
    </tr>
  `).join("");
}

function renderReportMeta(stats) {
  $reportMeta.innerHTML = `
    <article class="report-meta__card">
      <span>Lan cap nhat</span>
      <strong>${escapeHtml(formatDateTime(new Date().toISOString()))}</strong>
    </article>
    <article class="report-meta__card">
      <span>Tong thu / chi</span>
      <strong>${escapeHtml(`${money(stats.totalRevenue)} / ${money(stats.totalExpense)}`)}</strong>
    </article>
    <article class="report-meta__card">
      <span>Loi nhuan tam tinh</span>
      <strong>${escapeHtml(money(stats.totalProfit))}</strong>
    </article>
  `;
}

async function loadStats() {
  setStatus($status, "Dang tai thong ke...", "info");
  const stats = await apiGet("/admin/stats");
  renderCards(stats);
  renderReportMeta(stats);
  renderProductSales(stats.productSales || []);
  renderCategorySales(stats.categorySales || []);
  renderRecentOrders(stats.recentOrders || []);
  setStatus($status, "Da cap nhat thong ke.", "success");
}

async function main() {
  requireAdminPage();
  syncNav({ logoutTarget: "./login.html" });
  $reloadBtn.addEventListener("click", loadStats);
  $printBtn.addEventListener("click", () => window.print());
  await loadStats();
}

main().catch((error) => {
  console.error(error);
  setStatus($status, error.message || String(error), "error");
});
