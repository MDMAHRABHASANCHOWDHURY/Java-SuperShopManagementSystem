package service;

import dao.ProductDAO;
import dao.SupplyDAO;
import model.Product;
import model.Supply;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryService {

    private final ProductDAO productDAO = new ProductDAO();
    private final SupplyDAO supplyDAO = new SupplyDAO();

    public static final int LOW_STOCK_THRESHOLD = 10;

    public boolean addProduct(Product product) {
        return productDAO.addProduct(product);
    }

    public boolean updateProduct(Product product) {
        return productDAO.updateProduct(product);
    }

    public boolean removeProduct(int productId) {
        return productDAO.removeProduct(productId);
    }

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    /** Products available for customer purchase: in stock and not expired. */
    public List<Product> getPurchasableProducts() {
        return productDAO.getAllProducts().stream()
                .filter(Product::isPurchasable)
                .collect(Collectors.toList());
    }

    public List<Product> searchProducts(String keyword) {
        return productDAO.searchProducts(keyword);
    }

    public List<Product> getLowStockProducts() {
        return productDAO.getLowStockProducts(LOW_STOCK_THRESHOLD);
    }

    /** Records a supplier's delivery and increases stock accordingly. */
    public boolean recordSupply(Supply supply) {
        return supplyDAO.addSupply(supply);
    }

    public List<Supply> getSupplyHistory(int supplierId) {
        return supplyDAO.getSupplyHistory(supplierId);
    }

    public List<Supply> getAllSupplyHistory() {
        return supplyDAO.getAllSupplies();
    }
}
