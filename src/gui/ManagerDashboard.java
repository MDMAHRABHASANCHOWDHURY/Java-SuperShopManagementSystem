package gui;

import model.LoginActivity;
import model.Product;
import model.User;
import service.AuthenticationService;
import service.InventoryService;
import service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManagerDashboard extends JFrame {

    private final UserService userService = new UserService();
    private final InventoryService inventoryService = new InventoryService();

    private DefaultTableModel usersModel;
    private JTable usersTable;
    private List<User> currentUsers;

    public ManagerDashboard(User manager, AuthenticationService authService) {
        setTitle("Manager Dashboard - " + manager.getName());
        setSize(1200, 780);
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout());

        add(NavBar.build(manager, authService, this), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("User Management", buildUserManagementPanel());
        tabs.addTab("Employee Activity", buildActivityPanel());
        tabs.addTab("Inventory", buildInventoryPanel());
        tabs.addTab("Customers", buildCustomersPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ---------------- USER MANAGEMENT ----------------

    private JPanel buildUserManagementPanel() {
        JPanel panel = new SoftPanel(new BorderLayout());
        String[] columns = {"ID", "Name", "Username", "Role", "Status"};
        usersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersModel);
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        JPanel actions = new SoftPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approveBtn = new JButton("Approve User");
        JButton removeBtn = new JButton("Remove User");
        JButton refreshBtn = new JButton("Refresh");
        actions.add(approveBtn);
        actions.add(removeBtn);
        actions.add(refreshBtn);
        panel.add(actions, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row < 0) return;
            userService.approveUser(currentUsers.get(row).getUserId());
            loadUsers();
        });
        removeBtn.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row < 0) return;
            int confirm = JOptionPane.showConfirmDialog(this, "Remove this user?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                userService.removeUser(currentUsers.get(row).getUserId());
                loadUsers();
            }
        });
        refreshBtn.addActionListener(e -> loadUsers());

        loadUsers();
        return panel;
    }

    private void loadUsers() {
        usersModel.setRowCount(0);
        currentUsers = userService.getAllUsers();
        for (User u : currentUsers) {
            usersModel.addRow(new Object[]{u.getUserId(), u.getName(), u.getUsername(), u.getRole(), u.getStatus()});
        }
    }

    // ---------------- EMPLOYEE ACTIVITY ----------------

    private JPanel buildActivityPanel() {
        JPanel panel = new SoftPanel(new BorderLayout());
        String[] columns = {"Employee", "Role", "Login", "Logout"};
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
            List<LoginActivity> activity = userService.getLoginActivity();
            for (LoginActivity a : activity) {
                model.addRow(new Object[]{a.getEmployeeName(), a.getRole(), a.getLoginTime(),
                        a.isActive() ? "Active" : a.getLogoutTime()});
            }
        });
        refreshBtn.doClick();
        return panel;
    }

    // ---------------- INVENTORY ----------------

    private JPanel buildInventoryPanel() {
        JPanel panel = new SoftPanel(new BorderLayout());
        String[] columns = {"Image", "Product", "Category", "Stock", "Production Date", "Expiration Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int c) { return c == 0 ? Icon.class : Object.class; }
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        ProductImages.configureTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new SoftPanel(new FlowLayout(FlowLayout.LEFT));
        JButton allBtn = new JButton("View All Stock");
        JButton lowStockBtn = new JButton("View Low Stock");
        actions.add(allBtn);
        actions.add(lowStockBtn);
        panel.add(actions, BorderLayout.SOUTH);

        Runnable loadAll = () -> {
            model.setRowCount(0);
            for (Product p : inventoryService.getAllProducts()) {
                model.addRow(new Object[]{ProductImages.icon(p.getImagePath(), 64, 64), p.getProductName(), p.getCategory(), p.getStockQuantity(),
                        p.getProductionDate(), p.getExpirationDate(), p.getExpiryStatus()});
            }
        };
        allBtn.addActionListener(e -> loadAll.run());
        lowStockBtn.addActionListener(e -> {
            model.setRowCount(0);
            for (Product p : inventoryService.getLowStockProducts()) {
                model.addRow(new Object[]{ProductImages.icon(p.getImagePath(), 64, 64), p.getProductName(), p.getCategory(), p.getStockQuantity(),
                        p.getProductionDate(), p.getExpirationDate(), p.getExpiryStatus()});
            }
        });
        loadAll.run();
        return panel;
    }

    // ---------------- CUSTOMERS ----------------

    private JPanel buildCustomersPanel() {
        JPanel panel = new SoftPanel(new BorderLayout());
        String[] columns = {"ID", "Name", "Username", "Email", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new SoftPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton removeBtn = new JButton("Remove Customer");
        actions.add(refreshBtn);
        actions.add(removeBtn);
        panel.add(actions, BorderLayout.SOUTH);

        Runnable[] loadRef = new Runnable[1];
        loadRef[0] = () -> {
            model.setRowCount(0);
            for (User u : userService.getUsersByRole("CUSTOMER")) {
                model.addRow(new Object[]{u.getUserId(), u.getName(), u.getUsername(), u.getEmail(), u.getStatus()});
            }
        };
        refreshBtn.addActionListener(e -> loadRef[0].run());
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int id = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Remove this customer?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                userService.removeUser(id);
                loadRef[0].run();
            }
        });
        loadRef[0].run();
        return panel;
    }
}
