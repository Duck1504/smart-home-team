package com.smarthome.config;

import com.smarthome.entity.Product;

import java.math.BigDecimal;

public final class DemoCatalogDefaults {
  private DemoCatalogDefaults() {
  }

  public static String resolveTechnicalSpecs(Product product) {
    if (product == null) {
      return null;
    }

    if (product.getTechnicalSpecs() != null && !product.getTechnicalSpecs().isBlank()) {
      return product.getTechnicalSpecs();
    }

    return defaultTechnicalSpecs(product.getName());
  }

  public static BigDecimal resolveCostPrice(Product product) {
    if (product == null) {
      return BigDecimal.ZERO;
    }

    if (product.getCostPrice() != null) {
      return product.getCostPrice();
    }

    BigDecimal fallback = defaultCostPrice(product.getName());
    return fallback == null ? BigDecimal.ZERO : fallback;
  }

  private static String defaultTechnicalSpecs(String productName) {
    return switch (productName) {
      case "WiFi Smart Bulb A19" -> """
        Ket noi: WiFi 2.4GHz
        Cong suat: 9W
        Nhiet do mau: 2700K - 6500K
        Dieu khien: App va hen gio
        Tuong thich: Google Assistant, Alexa
        """.trim();
      case "RGB Smart LED Strip" -> """
        Chieu dai: 5m
        Mau sac: RGB 16 trieu mau
        Ket noi: WiFi + Bluetooth
        Che do: Theo nhac, hen gio, ngu canh
        Nguon cap: 12V
        """.trim();
      case "Fingerprint Smart Lock" -> """
        Chat lieu: Hop kim nhom
        Mo khoa: Van tay, the tu, ma so, chia khoa co
        Luu tru: 100 van tay
        Nguon: 4 pin AA
        Canh bao: Het pin va mo sai nhieu lan
        """.trim();
      case "WiFi Video Doorbell" -> """
        Do phan giai: Full HD 1080p
        Goc nhin: 160 do
        Ket noi: WiFi 2.4GHz
        Tinh nang: Dam thoai 2 chieu, phat hien chuyen dong
        Luu tru: The nho va cloud
        """.trim();
      case "Indoor Security Camera 2K" -> """
        Do phan giai: 2K
        Xoay quet: 360 do
        Tinh nang: Theo doi chuyen dong, dam thoai 2 chieu
        Nhin dem: Hong ngoai
        Luu tru: The nho toi da 256GB
        """.trim();
      default -> null;
    };
  }

  private static BigDecimal defaultCostPrice(String productName) {
    return switch (productName) {
      case "WiFi Smart Bulb A19" -> new BigDecimal("99000");
      case "RGB Smart LED Strip" -> new BigDecimal("185000");
      case "Fingerprint Smart Lock" -> new BigDecimal("1120000");
      case "WiFi Video Doorbell" -> new BigDecimal("730000");
      case "Indoor Security Camera 2K" -> new BigDecimal("560000");
      default -> null;
    };
  }
}
