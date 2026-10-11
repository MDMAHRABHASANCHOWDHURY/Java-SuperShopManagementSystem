package gui;

import model.Product;
import model.Supply;
import model.User;
import service.AuthenticationService;
import service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class SupplierDashboard extends JFrame {

    private final User supplier;
    private final InventoryService inventoryService = new InventoryService();
    private List<Product> currentProducts;

    public SupplierDashboard(User supplier, AuthenticationService authService) {
        this.supplier = supplier;
        setTitle("Supplier Dashboard - " + supplier.getName());
        setSize(1160, 760);
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout());

        add(NavBar.build(supplier, authService, this), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("View Stock", buildStockPanel());
        tabs.addTab("Supply Product", buildSupplyPanel());
        tabs.addTab("Supply History", buildHistoryPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildStockPanel() {
        JPanel panel = new SoftPanel(new BorderLayout());
        String[] columns = {"Image", "Product", "Category", "Stock", "Production Date", "Expiration Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int c) { return c == 0 ? Icon.class : Object.class; }
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        ProductImages.configureTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refreshBtn = new JButton("Refresh");
        panel.add(refreshBtn, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            currentProducts = inventoryService.getAllProducts();
            for (Product p : currentProducts) {
                model.addRow(new Object[]{ProductImages.icon(p.getImagePath(), 64, 64), p.getProductName(), p.getCategory(), p.getStockQuantity(),
                        p.getProductionDate(), p.getExpirationDate()});
            }
        });
        refreshBtn.doClick();
        return panel;
    }

    private JPanel buildSupplyPanel() {
        JPanel panel = new SoftPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.gridx = 0; gc.gridy = 0;

        JComboBox<Product> productBox = new JComboBox<>();
        for (Product p : inventoryService.getAllProducts()) productBox.addItem(p);

        JLabel currentStockLabel = new JLabel("-");
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
        JTextField dateField = new JTextField(LocalDate.now().toString(), 12);

        productBox.addActionListener(e -> {
            Product p = (Product) productBox.getSelectedItem();
            currentStockLabel.setText(p != null ? String.valueOf(p.getStockQuantity()) : "-");
        });
        if (productBox.getItemCount() > 0) {
            productBox.setSelectedIndex(0);
            currentStockLabel.setText(String.valueOf(((Product) productBox.getSelectedItem()).getStockQuantity()));
        }

        panel.add(new JLabel("Product:"), gc);
        gc.gridx = 1;
        panel.add(productBox, gc);

        gc.gridx = 0; gc.gridy++;
        panel.add(new JLabel("Current Stock:"), gc);
        gc.gridx = 1;
        panel.add(currentStockLabel, gc);

        gc.gridx = 0; gc.gridy++;
        panel.add(new JLabel("Supply Quantity:"), gc);
        gc.gridx = 1;
        panel.add(quantitySpinner, gc);

        gc.gridx = 0; gc.gridy++;
        panel.add(new JLabel("Supply Date (YYYY-MM-DD):"), gc);
        gc.gridx = 1;
        panel.add(dateField, gc);

        JButton addSupplyBtn = new JButton("ADD SUPPLY");
        gc.gridx = 0; gc.gridy++;
        gc.gridwidth = 2;
        panel.add(addSupplyBtn, gc);

        addSupplyBtn.addActionListener(e -> {
            Product p = (Product) productBox.getSelectedItem();
            if (p == null) return;
            try {
                int qty = (int) quantitySpinner.getValue();
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                Supply supply = new Supply(supplier.getUserId(), p.getProductId(), qty, date);
                boolean ok = inventoryService.recordSupply(supply);
                if (ok) {
                    int newStock = p.getStockQuantity() + qty;
                    JOptionPane.showMessageDialog(this,
                            "Old Stock = " + p.getStockQuantity() + "\nNew Supply = " + qty +
                            "\nNew Stock = " + newStock);
                    p.setStockQuantity(newStock);
                    currentStockLabel.setText(String.valueOf(newStock));
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add supply.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new SoftPanel(new BorderLayout());
        String[] columns = {"Product", "Quantity", "Supply Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refreshBtn = new JButton("Refresh");
        panel.add(refreshBtn, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<Supply> history = inventoryService.getSupplyHistory(supplier.getUserId());
            for (Supply s : history) {
                model.addRow(new Object[]{s.getProductName(), s.getQuantity(), s.getSupplyDate()});
            }
        });
        refreshBtn.doClick();
        return panel;
    }
}
