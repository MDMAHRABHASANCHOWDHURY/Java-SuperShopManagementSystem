package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Product {
    private int productId;
    private String productName;
    private String category;
    private String description;
    private String imagePath;
    private double price;
    private int stockQuantity;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private Integer supplierId;
    private LocalDateTime createdAt;

    public Product() {}

    public Product(int productId, String productName, String category, String description,
                   double price, int stockQuantity, LocalDate productionDate,
                   LocalDate expirationDate, Integer supplierId, LocalDateTime createdAt) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
        this.supplierId = supplierId;
        this.createdAt = createdAt;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Returns VALID, EXPIRING_SOON, or EXPIRED based on expirationDate. */
    public String getExpiryStatus() {
        if (expirationDate == null) return "VALID";
        LocalDate today = LocalDate.now();
        if (expirationDate.isBefore(today)) return "EXPIRED";
        if (!expirationDate.isAfter(today.plusDays(7))) return "EXPIRING_SOON";
        return "VALID";
    }

    public boolean isPurchasable() {
        return !getExpiryStatus().equals("EXPIRED") && stockQuantity > 0;
    }

    @Override
    public String toString() {
        return productName + " (Stock: " + stockQuantity + ")";
    }
}
