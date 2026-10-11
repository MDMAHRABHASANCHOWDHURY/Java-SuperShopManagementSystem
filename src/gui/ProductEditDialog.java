package gui;

import dao.UserDAO;
import model.Product;
import model.User;
import service.InventoryService;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

/** A simple form for adding or editing a product. */
public class ProductEditDialog extends JDialog {
    private InventoryService inventoryService;
    private Product existing;
    private Runnable onSaved;
    private File selectedImage;
    private List<User> supplierList;

    private JTextField nameField = new JTextField(24);
    private JTextField categoryField = new JTextField(24);
    private JTextField descField = new JTextField(24);
    private JTextField priceField = new JTextField(24);
    private JTextField stockField = new JTextField(24);
    private JTextField prodDateField = new JTextField(24);
    private JTextField expDateField = new JTextField(24);
    private JTextField imageField = new JTextField(19);
    private JComboBox<String> supplierBox = new JComboBox<>();
    private JLabel preview = new JLabel();

    public ProductEditDialog(Frame owner, InventoryService inventoryService, Product existing, Runnable onSaved) {
        super(owner, "Product Details", true);
        this.inventoryService = inventoryService;
        this.existing = existing;
        this.onSaved = onSaved;
        setSize(870, 770);
        setLocationRelativeTo(owner);
        BackgroundUI.apply(this);
        setLayout(new GridBagLayout());

        supplierBox.addItem("No supplier");
        supplierList = new UserDAO().getUsersByRole("SUPPLIER");
        for (User supplier : supplierList) {
            supplierBox.addItem(supplier.getName());
        }
        preview.setPreferredSize(new Dimension(150, 150));
        preview.setText("No Picture");
        if (existing != null) {
            nameField.setText(existing.getProductName());
            categoryField.setText(existing.getCategory());
            descField.setText(existing.getDescription());
            priceField.setText(String.valueOf(existing.getPrice()));
            stockField.setText(String.valueOf(existing.getStockQuantity()));
            if (existing.getProductionDate() != null) {
                prodDateField.setText(existing.getProductionDate().toString());
            }
            if (existing.getExpirationDate() != null) {
                expDateField.setText(existing.getExpirationDate().toString());
            }
            imageField.setText(existing.getImagePath());
            showPreview();
            if (existing.getSupplierId() != null) {
                for (int i = 0; i < supplierList.size(); i++) {
                    if (supplierList.get(i).getUserId() == existing.getSupplierId()) {
                        supplierBox.setSelectedIndex(i + 1);
                    }
                }
            }
        }

        JPanel imageRow = new SoftPanel(new BorderLayout(6, 0));
        imageRow.add(imageField, BorderLayout.CENTER);
        JButton browse = new JButton("Choose Image");
        imageRow.add(browse, BorderLayout.EAST);
        browse.addActionListener(e -> chooseImage());
        imageField.addActionListener(e -> showPreview());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.gridy = 0;
        addRow(gc, "Product / Pack Name:", nameField);
        addRow(gc, "Category:", categoryField);
        addRow(gc, "Description:", descField);
        addRow(gc, "Price (BDT):", priceField);
        addRow(gc, "Stock:", stockField);
        addRow(gc, "Production Date (YYYY-MM-DD):", prodDateField);
        addRow(gc, "Expiration Date (YYYY-MM-DD):", expDateField);
        addRow(gc, "Supplier:", supplierBox);
        addRow(gc, "Product Image:", imageRow);
        addRow(gc, "Preview:", preview);
        JButton save = new JButton("Save Product");
        gc.gridx = 0;
        gc.gridwidth = 2;
        add(save, gc);
        JButton back = new JButton("< Back");
        gc.gridy++;
        add(back, gc);
        back.addActionListener(e -> dispose());
        save.addActionListener(e -> saveProduct());
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Pictures (PNG, JPG)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (ProductImages.read(file.getPath()) == null) {
                JOptionPane.showMessageDialog(this, "Please choose a valid picture.");
                return;
            }
            selectedImage = file;
            imageField.setText(file.getPath());
            showPreview();
        }
    }

    private void showPreview() {
        String path = imageField.getText().trim();
        preview.setIcon(ProductImages.icon(path, 150, 150));
        if (ProductImages.read(path) == null) {
            preview.setText("No Picture");
        } else {
            preview.setText("");
        }
    }

    private void saveProduct() {
        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            if (name.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and category are required.");
                return;
            }
            double price = Double.parseDouble(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());
            if (!Double.isFinite(price) || price <= 0 || stock < 0) {
                JOptionPane.showMessageDialog(this, "Enter a positive price and zero or more stock.");
                return;
            }

            LocalDate produced = null;
            LocalDate expires = null;
            if (!prodDateField.getText().trim().isEmpty()) {
                produced = LocalDate.parse(prodDateField.getText().trim());
            }
            if (!expDateField.getText().trim().isEmpty()) {
                expires = LocalDate.parse(expDateField.getText().trim());
            }
            if (produced != null && expires != null && expires.isBefore(produced)) {
                JOptionPane.showMessageDialog(this, "Expiration cannot be before production.");
                return;
            }

            String imagePath = imageField.getText().trim();
            if (!imagePath.isEmpty() && ProductImages.read(imagePath) == null) {
                JOptionPane.showMessageDialog(this, "The product picture could not be opened.");
                return;
            }
            if (selectedImage != null && imagePath.equals(selectedImage.getPath())) {
                imagePath = ProductImages.importImage(selectedImage);
            }

            Product product = new Product();
            if (existing != null) {
                product.setProductId(existing.getProductId());
            }
            product.setProductName(name);
            product.setCategory(category);
            product.setDescription(descField.getText().trim());
            product.setPrice(price);
            product.setStockQuantity(stock);
            product.setProductionDate(produced);
            product.setExpirationDate(expires);
            product.setImagePath(imagePath);
            int supplierIndex = supplierBox.getSelectedIndex();
            if (supplierIndex > 0) {
                product.setSupplierId(supplierList.get(supplierIndex - 1).getUserId());
            }

            boolean saved;
            if (existing == null) {
                saved = inventoryService.addProduct(product);
            } else {
                saved = inventoryService.updateProduct(product);
            }
            if (saved) {
                onSaved.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Could not save the product.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Check your input: " + ex.getMessage());
        }
    }

    private void addRow(GridBagConstraints gc, String label, Component field) {
        gc.gridwidth = 1;
        gc.gridx = 0;
        add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        add(field, gc);
        gc.fill = GridBagConstraints.NONE;
        gc.gridy++;
    }
}
