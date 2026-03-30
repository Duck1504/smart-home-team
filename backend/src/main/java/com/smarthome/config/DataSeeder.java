package com.smarthome.config;

import com.smarthome.entity.Category;
import com.smarthome.entity.Product;
import com.smarthome.entity.User;
import com.smarthome.entity.UserRole;
import com.smarthome.repository.CategoryRepository;
import com.smarthome.repository.ProductRepository;
import com.smarthome.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Configuration
public class DataSeeder {
  @Bean
  public CommandLineRunner seedData(
    CategoryRepository categoryRepository,
    ProductRepository productRepository,
    UserRepository userRepository,
    PasswordEncoder passwordEncoder
  ) {
    return args -> {
      if (categoryRepository.count() == 0) {
        Category c1 = new Category();
        c1.setName("Smart Light");
        categoryRepository.save(c1);

        Category c2 = new Category();
        c2.setName("Smart Lock");
        categoryRepository.save(c2);

        Category c3 = new Category();
        c3.setName("Smart Camera");
        categoryRepository.save(c3);

        Product p1 = new Product();
        p1.setName("WiFi Smart Bulb A19");
        p1.setDescription("Bong den WiFi doi mau, hen gio va dieu khien bang dien thoai.");
        p1.setTechnicalSpecs("""
          Ket noi: WiFi 2.4GHz
          Cong suat: 9W
          Nhiet do mau: 2700K - 6500K
          Dieu khien: App va hen gio
          Tuong thich: Google Assistant, Alexa
          """.trim());
        p1.setPrice(new BigDecimal("159000"));
        p1.setCostPrice(new BigDecimal("99000"));
        p1.setImageUrl("/assets/smart-light.svg");
        p1.setStock(100);
        p1.setCategory(c1);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("RGB Smart LED Strip");
        p2.setDescription("Day LED RGB cho phong khach, tao ngu canh sang theo lich hen.");
        p2.setTechnicalSpecs("""
          Chieu dai: 5m
          Mau sac: RGB 16 trieu mau
          Ket noi: WiFi + Bluetooth
          Che do: Theo nhac, hen gio, ngu canh
          Nguon cap: 12V
          """.trim());
        p2.setPrice(new BigDecimal("299000"));
        p2.setCostPrice(new BigDecimal("185000"));
        p2.setImageUrl("/assets/smart-light.svg");
        p2.setStock(50);
        p2.setCategory(c1);
        productRepository.save(p2);

        Product p3 = new Product();
        p3.setName("Fingerprint Smart Lock");
        p3.setDescription("Khoa thong minh mo bang van tay, the tu va ma so.");
        p3.setTechnicalSpecs("""
          Chat lieu: Hop kim nhom
          Mo khoa: Van tay, the tu, ma so, chia khoa co
          Luu tru: 100 van tay
          Nguon: 4 pin AA
          Canh bao: Het pin va mo sai nhieu lan
          """.trim());
        p3.setPrice(new BigDecimal("1499000"));
        p3.setCostPrice(new BigDecimal("1120000"));
        p3.setImageUrl("/assets/smart-lock.svg");
        p3.setStock(15);
        p3.setCategory(c2);
        productRepository.save(p3);

        Product p4 = new Product();
        p4.setName("WiFi Video Doorbell");
        p4.setDescription("Chuong cua video ket noi WiFi, thong bao khi co chuyen dong.");
        p4.setTechnicalSpecs("""
          Do phan giai: Full HD 1080p
          Goc nhin: 160 do
          Ket noi: WiFi 2.4GHz
          Tinh nang: Dam thoai 2 chieu, phat hien chuyen dong
          Luu tru: The nho va cloud
          """.trim());
        p4.setPrice(new BigDecimal("999000"));
        p4.setCostPrice(new BigDecimal("730000"));
        p4.setImageUrl("/assets/smart-camera.svg");
        p4.setStock(20);
        p4.setCategory(c3);
        productRepository.save(p4);

        Product p5 = new Product();
        p5.setName("Indoor Security Camera 2K");
        p5.setDescription("Camera trong nha 2K, xem truc tiep va luu lai su kien quan trong.");
        p5.setTechnicalSpecs("""
          Do phan giai: 2K
          Xoay quet: 360 do
          Tinh nang: Theo doi chuyen dong, dam thoai 2 chieu
          Nhin dem: Hong ngoai
          Luu tru: The nho toi da 256GB
          """.trim());
        p5.setPrice(new BigDecimal("799000"));
        p5.setCostPrice(new BigDecimal("560000"));
        p5.setImageUrl("/assets/smart-camera.svg");
        p5.setStock(30);
        p5.setCategory(c3);
        productRepository.save(p5);
      }

      normalizeDemoCatalog(productRepository);

      if (!userRepository.existsByUsername("admin")) {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123!"));
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);
      }
    };
  }

  private void normalizeDemoCatalog(ProductRepository productRepository) {
    List<Product> products = productRepository.findAll();
    boolean changed = false;

    for (Product product : products) {
      String previousDescription = product.getDescription();
      String previousImageUrl = product.getImageUrl();
      String previousTechnicalSpecs = product.getTechnicalSpecs();
      BigDecimal previousCostPrice = product.getCostPrice();

      applyDemoContent(product);

      if (!Objects.equals(previousDescription, product.getDescription())
        || !Objects.equals(previousImageUrl, product.getImageUrl())
        || !Objects.equals(previousTechnicalSpecs, product.getTechnicalSpecs())
        || !sameAmount(previousCostPrice, product.getCostPrice())) {
        changed = true;
      }
    }

    if (changed) {
      productRepository.saveAll(products);
    }
  }

  private void applyDemoContent(Product product) {
    switch (product.getName()) {
      case "WiFi Smart Bulb A19" -> {
        product.setDescription("Bong den WiFi doi mau, hen gio va dieu khien bang dien thoai.");
        product.setTechnicalSpecs("""
          Ket noi: WiFi 2.4GHz
          Cong suat: 9W
          Nhiet do mau: 2700K - 6500K
          Dieu khien: App va hen gio
          Tuong thich: Google Assistant, Alexa
          """.trim());
        product.setCostPrice(new BigDecimal("99000"));
        product.setImageUrl("/assets/smart-light.svg");
      }
      case "RGB Smart LED Strip" -> {
        product.setDescription("Day LED RGB cho phong khach, tao ngu canh sang theo lich hen.");
        product.setTechnicalSpecs("""
          Chieu dai: 5m
          Mau sac: RGB 16 trieu mau
          Ket noi: WiFi + Bluetooth
          Che do: Theo nhac, hen gio, ngu canh
          Nguon cap: 12V
          """.trim());
        product.setCostPrice(new BigDecimal("185000"));
        product.setImageUrl("/assets/smart-light.svg");
      }
      case "Fingerprint Smart Lock" -> {
        product.setDescription("Khoa thong minh mo bang van tay, the tu va ma so.");
        product.setTechnicalSpecs("""
          Chat lieu: Hop kim nhom
          Mo khoa: Van tay, the tu, ma so, chia khoa co
          Luu tru: 100 van tay
          Nguon: 4 pin AA
          Canh bao: Het pin va mo sai nhieu lan
          """.trim());
        product.setCostPrice(new BigDecimal("1120000"));
        product.setImageUrl("/assets/smart-lock.svg");
      }
      case "WiFi Video Doorbell" -> {
        product.setDescription("Chuong cua video ket noi WiFi, thong bao khi co chuyen dong.");
        product.setTechnicalSpecs("""
          Do phan giai: Full HD 1080p
          Goc nhin: 160 do
          Ket noi: WiFi 2.4GHz
          Tinh nang: Dam thoai 2 chieu, phat hien chuyen dong
          Luu tru: The nho va cloud
          """.trim());
        product.setCostPrice(new BigDecimal("730000"));
        product.setImageUrl("/assets/smart-camera.svg");
      }
      case "Indoor Security Camera 2K" -> {
        product.setDescription("Camera trong nha 2K, xem truc tiep va luu lai su kien quan trong.");
        product.setTechnicalSpecs("""
          Do phan giai: 2K
          Xoay quet: 360 do
          Tinh nang: Theo doi chuyen dong, dam thoai 2 chieu
          Nhin dem: Hong ngoai
          Luu tru: The nho toi da 256GB
          """.trim());
        product.setCostPrice(new BigDecimal("560000"));
        product.setImageUrl("/assets/smart-camera.svg");
      }
      default -> {
      }
    }
  }

  private boolean sameAmount(BigDecimal left, BigDecimal right) {
    if (left == null && right == null) {
      return true;
    }

    if (left == null || right == null) {
      return false;
    }

    return left.compareTo(right) == 0;
  }
}
