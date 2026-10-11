package gui;

import dao.CartDAO;
import model.Product;
import model.Cart;
import model.User;
import service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Public catalogue and customer shopping screen with offline product pictures. */
public class ProductFrame extends JFrame {
    private final User user;
    private final InventoryService inventoryService = new InventoryService();
    private final CartDAO cartDAO = new CartDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> categoryBox = new JComboBox<>();
    private final JLabel countLabel = new JLabel();
    private final JLabel preview = new JLabel("Select a product", SwingConstants.CENTER);
    private final JTextArea details = new JTextArea();
    private List<Product> currentProducts = new ArrayList<>();

    public ProductFrame(User user) {
        this.user = user;
        setTitle("Amar Dokan - Products (BDT)");
        setSize(1240, 760);
        setMinimumSize(new Dimension(900, 480));
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout(8, 8));

        JPanel top = new SoftPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(Navigation.backButton(this));
        top.add(new JLabel("AMAR DOKAN  |  "));
        top.add(new JLabel("Search:"));
        top.add(searchField);
        JButton searchBtn = new JButton("Search");
        top.add(searchBtn);
        top.add(new JLabel("Category:"));
        categoryBox.addItem("All Categories");
        categoryBox.setPrototypeDisplayValue("Personal & Home Care");
        top.add(categoryBox);
        JButton refresh = new JButton("Refresh");
        top.add(refresh);
        top.add(countLabel);
        add(top, BorderLayout.NORTH);

        String[] columns = {"Image", "Product / Pack", "Category", "Price (BDT)", "Stock", "Expires", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 0) return Icon.class;
                if (col == 3) return Double.class;
                if (col == 4) return Integer.class;
                return Object.class;
            }
        };
        table = new JTable(tableModel);
        ProductImages.configureTable(table);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {76, 205, 150, 95, 55, 100, 140};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JPanel side = new SoftPanel(new BorderLayout(4, 12));
        side.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        side.setPreferredSize(new Dimension(270, 400));
        preview.setPreferredSize(new Dimension(240, 240));
        side.add(preview, BorderLayout.NORTH);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new Font("SansSerif", Font.PLAIN, 14));
        details.setBackground(side.getBackground());
        side.add(new JScrollPane(details), BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), side);
        split.setResizeWeight(0.77);
        split.setDividerLocation(855);
        add(split, BorderLayout.CENTER);

        JPanel bottom = new SoftPanel(new FlowLayout(FlowLayout.RIGHT));
        if (user != null && "CUSTOMER".equals(user.getRole())) {
            JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            JButton addBtn = new JButton("Add to Cart");
            bottom.add(new JLabel("Quantity:"));
            bottom.add(quantity);
            bottom.add(addBtn);
            addBtn.addActionListener(e -> addSelectedToCart((int) quantity.getValue()));
            JButton cartBtn = new JButton("Go to Cart");
            cartBtn.addActionListener(e -> Navigation.open(this, new CartFrame(user)));
            bottom.add(cartBtn);
        }
        add(bottom, BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showSelected(); });
        searchBtn.addActionListener(e -> loadProducts());
        searchField.addActionListener(e -> loadProducts());
        categoryBox.addActionListener(e -> loadProducts());
        refresh.addActionListener(e -> { loadCategories(); loadProducts(); });
        loadCategories();
        loadProducts();
    }

    private void loadCategories() {
        String selected = (String) categoryBox.getSelectedItem();
        List<String> categories = new ArrayList<>();
        List<Product> allProducts;
        try {
            allProducts = inventoryService.getAllProducts();
        } catch (RuntimeException ex) {
            countLabel.setText("Database not connected. Check MySQL.");
            return;
        }
        for (Product p : allProducts) {
            String category = p.getCategory();
            if (category != null && !category.isBlank() && !categories.contains(category)) {
                categories.add(category);
            }
        }
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("All Categories");
        for (String category : categories) model.addElement(category);
        categoryBox.setModel(model);
        if (categories.contains(selected)) categoryBox.setSelectedItem(selected);
    }

    private void loadProducts() {
        String keyword = searchField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();
        List<Product> products;
        try {
            products = keyword.isEmpty() ? inventoryService.getAllProducts() : inventoryService.searchProducts(keyword);
        } catch (RuntimeException ex) {
            tableModel.setRowCount(0);
            countLabel.setText("Database not connected. Check MySQL.");
            return;
        }
        table.clearSelection();
        currentProducts = new ArrayList<>();
        for (Product product : products) {
            if (category == null || category.equals("All Categories") || category.equals(product.getCategory())) {
                currentProducts.add(product);
            }
        }
        tableModel.setRowCount(0);
        for (Product p : currentProducts) {
            tableModel.addRow(new Object[]{ProductImages.icon(p.getImagePath(), 64, 64), p.getProductName(),
                    p.getCategory(), p.getPrice(), p.getStockQuantity(), p.getExpirationDate(),
                    p.getStockQuantity() == 0 ? "OUT OF STOCK" : p.getExpiryStatus()});
        }
        countLabel.setText(currentProducts.size() + " products");
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
        else { preview.setIcon(null); preview.setText("No products found"); details.setText(""); }
    }

    private Product selectedProduct() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || viewRow >= table.getRowCount()) return null;
        int row = table.convertRowIndexToModel(viewRow);
        return row >= currentProducts.size() ? null : currentProducts.get(row);
    }

    private void showSelected() {
        Product p = selectedProduct();
        if (p == null) return;
        preview.setText("");
        preview.setIcon(ProductImages.icon(p.getImagePath(), 240, 240));
        details.setText(p.getProductName() + "\n\n" + p.getCategory() + "\nBDT " + String.format("%.2f", p.getPrice())
                + "\nStock: " + p.getStockQuantity() + " selling units\n\n"
                + (p.getDescription() == null ? "" : p.getDescription()) + "\n\nProduction / Packed: "
                + (p.getProductionDate() == null ? "Not specified" : p.getProductionDate()) + "\nExpires: "
                + (p.getExpirationDate() == null ? "Not specified" : p.getExpirationDate()) + "\nStatus: " + p.getExpiryStatus());
        details.setCaretPosition(0);
    }

    private void addSelectedToCart(int quantity) {
        Product product = selectedProduct();
        if (product == null) { JOptionPane.showMessageDialog(this, "Select a product first."); return; }
        if (!product.isPurchasable()) { JOptionPane.showMessageDialog(this, "This product is expired or out of stock."); return; }
        if (quantity > product.getStockQuantity()) { JOptionPane.showMessageDialog(this, "Insufficient stock."); return; }
        Cart cart = cartDAO.getOrCreateCart(user.getUserId());
        boolean ok = cartDAO.addItem(cart.getCartId(), product.getProductId(), quantity, product.getPrice());
        JOptionPane.showMessageDialog(this, ok ? "Added to cart." : "Failed to add to cart.");
    }
}
