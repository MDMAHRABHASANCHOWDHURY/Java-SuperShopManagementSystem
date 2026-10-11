package model;

import java.time.LocalDate;

public class Supply {
    private int supplyId;
    private int supplierId;
    private int productId;
    private String productName;
    private int quantity;
    private LocalDate supplyDate;

    public Supply() {}

    public Supply(int supplierId, int productId, int quantity, LocalDate supplyDate) {
        this.supplierId = supplierId;
        this.productId = productId;
        this.quantity = quantity;
        this.supplyDate = supplyDate;
    }

    public int getSupplyId() { return supplyId; }
    public void setSupplyId(int supplyId) { this.supplyId = supplyId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDate getSupplyDate() { return supplyDate; }
    public void setSupplyDate(LocalDate supplyDate) { this.supplyDate = supplyDate; }
}
