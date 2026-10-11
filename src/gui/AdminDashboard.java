package gui;

import model.Product;
import model.User;
import service.AuthenticationService;
import service.InventoryService;
import service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AdminDashboard extends JFrame {

    private final UserService userService = new UserService();
    private final InventoryService inventoryService = new InventoryService();

    private DefaultTableModel usersModel;
    private JTable usersTable;
    private List<User> currentUsers;

    private DefaultTableModel productsModel;
    private JTable productsTable;
    private List<Product> currentProducts;

    public AdminDashboard(User admin, AuthenticationService authService) {
        setTitle("Admin Dashboard - " + admin.getName());
        setSize(1200, 780);
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout());

        add(NavBar.build(admin, authService, this), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Users", buildUsersPanel(admin));
        tabs.addTab("Products", buildProductsPanel());
        tabs.addTab("Reports", buildReportsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ---------------- USERS ----------------

    private JPanel buildUsersPanel(User admin) {
        JPanel panel = new SoftPanel(new BorderLayout());

        String[] columns = {"ID", "Name", "Username", "Email", "Role", "Status"};
        usersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersModel);
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        JPanel actions = new SoftPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approveBtn = new JButton("Approve Request");
        JButton rejectBtn = new JButton("Reject Request");
        JButton addUserBtn = new JButton("Add User");
        JButton removeUserBtn = new JButton("Remove User");
        JButton changeRoleBtn = new JButton("Change Role");
        JButton refreshBtn = new JButton("Refresh");

        actions.add(approveBtn);
        actions.add(rejectBtn);
        actions.add(addUserBtn);
        actions.add(removeUserBtn);
        actions.add(changeRoleBtn);
        actions.add(refreshBtn);
        panel.add(actions, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> withSelectedUser(id -> {
            userService.approveUser(id);
            loadUsers();
        }));
        rejectBtn.addActionListener(e -> withSelectedUser(id -> {
            userService.rejectUser(id);
            loadUsers();
        }));
        removeUserBtn.addActionListener(e -> withSelectedUser(id -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Remove this user permanently?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                userService.removeUser(id);
                loadUsers();
            }
        }));
        changeRoleBtn.addActionListener(e -> withSelectedUser(id -> {
            String[] roles = {"ADMIN", "CUSTOMER", "CASHIER", "MANAGER", "SUPPLIER"};
            String role = (String) JOptionPane.showInputDialog(this, "New role:", "Change Role",
                    JOptionPane.PLAIN_MESSAGE, null, roles, roles[0]);
            if (role != null) {
                userService.changeRole(id, role);
                loadUsers();
            }
        }));
        addUserBtn.addActionListener(e -> {
            new AddUserDialog(this, userService, this::loadUsers).setVisible(true);
        });
        refreshBtn.addActionListener(e -> loadUsers());

        loadUsers();
        return panel;
    }

    private void loadUsers() {
        usersModel.setRowCount(0);
        currentUsers = userService.getAllUsers();
        for (User u : currentUsers) {
            usersModel.addRow(new Object[]{u.getUserId(), u.getName(), u.getUsername(),
                    u.getEmail(), u.getRole(), u.getStatus()});
        }
    }

    private interface IntConsumer { void accept(int id); }

    private void withSelectedUser(IntConsumer action) {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        action.accept(currentUsers.get(row).getUserId());
    }

    // ---------------- PRODUCTS ----------------

    private JPanel buildProductsPanel() {
        JPanel panel = new SoftPanel(new BorderLayout());
        String[] columns = {"Image", "ID", "Name", "Category", "Price", "Stock", "Expiration"};
        productsModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int c) { return c == 0 ? Icon.class : Object.class; }
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        productsTable = new JTable(productsModel);
        ProductImages.configureTable(productsTable);
        panel.add(new JScrollPane(productsTable), BorderLayout.CENTER);

        JPanel actions = new SoftPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Product");
        JButton editBtn = new JButton("Edit Product");
        JButton removeBtn = new JButton("Remove Product");
        JButton refreshBtn = new JButton("Refresh");
        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(removeBtn);
        actions.add(refreshBtn);
        panel.add(actions, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> new ProductEditDialog(this, inventoryService, null, this::loadProducts).setVisible(true));
        editBtn.addActionListener(e -> {
            int row = productsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a product first.");
                return;
            }
            new ProductEditDialog(this, inventoryService, currentProducts.get(row), this::loadProducts).setVisible(true);
        });
        removeBtn.addActionListener(e -> {
            int row = productsTable.getSelectedRow();
            if (row < 0) return;
            int confirm = JOptionPane.showConfirmDialog(this, "Remove this product?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                inventoryService.removeProduct(currentProducts.get(row).getProductId());
                loadProducts();
            }
        });
        refreshBtn.addActionListener(e -> loadProducts());

        loadProducts();
        return panel;
    }

    private void loadProducts() {
        productsModel.setRowCount(0);
        currentProducts = inventoryService.getAllProducts();
        for (Product p : currentProducts) {
            productsModel.addRow(new Object[]{ProductImages.icon(p.getImagePath(), 64, 64), p.getProductId(), p.getProductName(), p.getCategory(),
                    p.getPrice(), p.getStockQuantity(), p.getExpirationDate()});
        }
    }

    // ---------------- REPORTS ----------------

    private JPanel buildReportsPanel() {
        JPanel panel = new SoftPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton salesBtn = new JButton("View Sales");
        JButton stockBtn = new JButton("View Stock");
        JButton usersBtn = new JButton("View Users Summary");

        salesBtn.addActionListener(e -> new SalesReportDialog(this).setVisible(true));
        stockBtn.addActionListener(e -> Navigation.open(this, new ProductFrame(null)));
        usersBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Total Users: " + userService.getAllUsers().size()));

        panel.add(salesBtn);
        panel.add(stockBtn);
        panel.add(usersBtn);
        return panel;
    }
}
