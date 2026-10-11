package gui;

import dao.OrderDAO;
import model.Order;
import model.User;
import service.AuthenticationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerDashboard extends JFrame {

    private final OrderDAO orderDAO = new OrderDAO();

    public CustomerDashboard(User user, AuthenticationService authService) {
        setTitle("Customer Home - " + user.getName());
        setSize(1100, 730);
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout());

        add(NavBar.build(user, authService, this), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        JPanel homePanel = new ImagePanel(true);
        homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));
        homePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        JLabel welcome = new JLabel("Welcome, " + user.getName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 20));
        homePanel.add(welcome);
        homePanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton productsBtn = new JButton("Browse Products");
        JButton cartBtn = new JButton("My Cart");
        productsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cartBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        productsBtn.addActionListener(e -> Navigation.open(this, new ProductFrame(user)));
        cartBtn.addActionListener(e -> Navigation.open(this, new CartFrame(user)));
        homePanel.add(productsBtn);
        homePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        homePanel.add(cartBtn);

        tabs.addTab("Home", homePanel);

        JPanel ordersPanel = new SoftPanel(new BorderLayout());
        String[] columns = {"Invoice #", "Date", "Subtotal", "Discount", "VAT", "Grand Total", "Payment Method", "Payment Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable ordersTable = new JTable(model);
        ordersPanel.add(new JScrollPane(ordersTable), BorderLayout.CENTER);
        JButton refreshBtn = new JButton("Refresh");
        ordersPanel.add(refreshBtn, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<Order> orders = orderDAO.getOrdersByCustomer(user.getUserId());
            for (Order o : orders) {
                model.addRow(new Object[]{o.getInvoiceNumber(), o.getOrderDate(),
                        o.getSubtotal(), o.getDiscount(), o.getVat(), o.getGrandTotal(),
                        o.getPaymentMethod(), o.getPaymentStatus()});
            }
        });
        refreshBtn.doClick();

        tabs.addTab("My Orders", ordersPanel);

        add(tabs, BorderLayout.CENTER);
    }
}
