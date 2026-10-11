package gui;

import dao.CartDAO;
import model.Cart;
import model.CartItem;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CartFrame extends JFrame {

    private final User user;
    private final CartDAO cartDAO = new CartDAO();

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel totalLabel = new JLabel("Total: 0.00");

    private Cart cart;

    public CartFrame(User user) {
        this.user = user;
        setTitle("Amar Dokan - My Cart");
        setSize(1060, 670);
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout());

        JPanel top = new SoftPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(Navigation.backButton(this));
        top.add(new JLabel("AMAR DOKAN - MY CART"));
        add(top, BorderLayout.NORTH);

        String[] columns = {"Product", "Quantity", "Unit Price", "Subtotal"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 1; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new SoftPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateQtyBtn = new JButton("Update Qty");
        JButton removeBtn = new JButton("Remove");
        JButton checkoutBtn = new JButton("Proceed to Payment");
        bottom.add(totalLabel);
        bottom.add(updateQtyBtn);
        bottom.add(removeBtn);
        bottom.add(checkoutBtn);
        add(bottom, BorderLayout.SOUTH);

        updateQtyBtn.addActionListener(e -> updateQuantity());
        removeBtn.addActionListener(e -> removeItem());
        checkoutBtn.addActionListener(e -> openPayment());

        loadCart();
    }

    private void loadCart() {
        tableModel.setRowCount(0);
        try {
            cart = cartDAO.getOrCreateCart(user.getUserId());
        } catch (RuntimeException ex) {
            cart = null;
        }
        if (cart == null) {
            totalLabel.setText("Cannot load cart: check MySQL connection.");
            return;
        }
        for (CartItem item : cart.getItems()) {
            tableModel.addRow(new Object[]{
                    item.getProductName(), item.getQuantity(),
                    String.format("%.2f", item.getUnitPrice()),
                    String.format("%.2f", item.getSubtotal())
            });
        }
        totalLabel.setText("Total: " + String.format("%.2f", cart.getTotal()));
    }

    private void updateQuantity() {
        int row = table.getSelectedRow();
        if (row < 0 || cart == null) return;
        CartItem item = cart.getItems().get(row);
        String input = JOptionPane.showInputDialog(this, "New quantity:", item.getQuantity());
        if (input == null) return;
        try {
            int qty = Integer.parseInt(input.trim());
            if (qty <= 0) {
                cartDAO.removeItem(item.getCartItemId());
            } else {
                cartDAO.updateItemQuantity(item.getCartItemId(), qty);
            }
            loadCart();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number.");
        }
    }

    private void removeItem() {
        int row = table.getSelectedRow();
        if (row < 0 || cart == null) return;
        CartItem item = cart.getItems().get(row);
        cartDAO.removeItem(item.getCartItemId());
        loadCart();
    }

    public void refreshCart() {
        loadCart();
    }

    private void openPayment() {
        if (cart == null || cart.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }
        Navigation.open(this, new PaymentFrame(user));
    }
}
