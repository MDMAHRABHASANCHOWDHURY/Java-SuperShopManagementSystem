package gui;

import dao.CartDAO;
import dao.ProductDAO;
import model.Cart;
import model.CartItem;
import model.Product;
import model.User;
import service.BillingService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Customer checkout: demo payment selection, not a real payment gateway.
public class PaymentFrame extends JFrame {
    private final User customer;
    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final BillingService billingService = new BillingService();

    private final JRadioButton bkash = new JRadioButton("bKash", true);
    private final JRadioButton nagad = new JRadioButton("Nagad");
    private final JRadioButton card = new JRadioButton("Visa / Mastercard");
    private final JRadioButton banking = new JRadioButton("Internet Banking");
    private final JLabel amountLabel = new JLabel();
    private final JLabel methodNote = new JLabel();
    private final JCheckBox demoCheck = new JCheckBox("I understand this is only a demo payment.");
    private final JButton placeOrderButton = new JButton("Confirm Demo Payment & Place Order");

    public PaymentFrame(User customer) {
        this.customer = customer;
        setTitle("Amar Dokan - Customer Payment");
        setSize(1010, 690);
        setMinimumSize(new Dimension(780, 560));
        setLocationRelativeTo(null);
        Navigation.closeGoesBack(this);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout(10, 10));

        JPanel top = new SoftPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        top.add(Navigation.backButton(this));
        JLabel heading = new JLabel("AMAR DOKAN - PAYMENT");
        heading.setFont(new Font("SansSerif", Font.BOLD, 23));
        top.add(heading);
        add(top, BorderLayout.NORTH);

        JPanel form = new SoftPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(11, 15, 11, 15);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel title = new JLabel("Choose Your Payment Method");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        form.add(title, gc);

        gc.gridy++;
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 21));
        form.add(amountLabel, gc);

        gc.gridy++;
        form.add(new JLabel("Pay using:"), gc);

        ButtonGroup group = new ButtonGroup();
        JPanel choices = new JPanel(new GridLayout(2, 2, 24, 16));
        choices.setOpaque(false);
        for (JRadioButton option : new JRadioButton[]{bkash, nagad, card, banking}) {
            group.add(option);
            option.setOpaque(false);
            option.setFont(new Font("SansSerif", Font.BOLD, 16));
            option.addActionListener(e -> updateMethodNote());
            choices.add(option);
        }
        gc.gridy++;
        form.add(choices, gc);

        gc.gridy++;
        gc.anchor = GridBagConstraints.CENTER;
        methodNote.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(methodNote, gc);

        gc.gridy++;
        JLabel warning = new JLabel("DEMO ONLY - No money is transferred and no payment is verified.");
        warning.setForeground(new Color(145, 43, 22));
        warning.setFont(new Font("SansSerif", Font.BOLD, 14));
        form.add(warning, gc);

        gc.gridy++;
        demoCheck.setOpaque(false);
        form.add(demoCheck, gc);

        gc.gridy++;
        placeOrderButton.setPreferredSize(new Dimension(340, 50));
        placeOrderButton.setBackground(new Color(32, 121, 81));
        placeOrderButton.setForeground(Color.WHITE);
        placeOrderButton.setEnabled(false);
        form.add(placeOrderButton, gc);
        add(form, BorderLayout.CENTER);

        demoCheck.addActionListener(e -> placeOrderButton.setEnabled(demoCheck.isSelected()));
        placeOrderButton.addActionListener(e -> placeOrder());
        updateMethodNote();
        updateAmount();
    }

    private String selectedMethod() {
        if (bkash.isSelected()) return "bKash";
        if (nagad.isSelected()) return "Nagad";
        if (card.isSelected()) return "Visa / Mastercard";
        return "Internet Banking";
    }

    private void updateMethodNote() {
        String method = selectedMethod();
        String note;
        if ("bKash".equals(method)) {
            note = "bKash mobile wallet (demo selection)";
        } else if ("Nagad".equals(method)) {
            note = "Nagad mobile wallet (demo selection)";
        } else if ("Visa / Mastercard".equals(method)) {
            note = "Credit or debit card (demo selection - no card details needed)";
        } else {
            note = "Internet Banking (demo selection - no banking password needed)";
        }
        methodNote.setText(note);
    }

    private void updateAmount() {
        try {
            Cart cart = cartDAO.getOrCreateCart(customer.getUserId());
            if (cart == null) throw new IllegalStateException("MySQL not connected");
            double total = Math.round(cart.getTotal() * 1.05 * 100) / 100.0;
            amountLabel.setText(String.format("Amount (with 5%% VAT): BDT %.2f", total));
        } catch (RuntimeException ex) {
            amountLabel.setText("Cannot load amount: check MySQL connection.");
            placeOrderButton.setEnabled(false);
        }
    }

    private void placeOrder() {
        // Get cart and stock again, so an outdated cart cannot place an order.
        Cart cart;
        try {
            cart = cartDAO.getOrCreateCart(customer.getUserId());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Check MySQL settings.");
            return;
        }
        if (cart == null || cart.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }
        List<BillingService.BillLine> lines = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = productDAO.findById(item.getProductId());
            if (product == null || !product.isPurchasable() || item.getQuantity() > product.getStockQuantity()) {
                JOptionPane.showMessageDialog(this, "Some products are unavailable. Return to your cart.");
                return;
            }
            product.setPrice(item.getUnitPrice()); // Use the price saved in the cart.
            lines.add(new BillingService.BillLine(product, item.getQuantity()));
        }
        double total = Math.round(cart.getTotal() * 1.05 * 100) / 100.0;
        BillingService.BillResult bill = billingService.calculate(lines, 0, total);
        if (!bill.isValid()) {
            JOptionPane.showMessageDialog(this, bill.error);
            return;
        }
        String method = selectedMethod();
        int answer = JOptionPane.showConfirmDialog(this,
                String.format("Method: %s\nDemo amount: BDT %.2f\n\nNo real money will be charged.\nPlace this DEMO order?", method, bill.grandTotal),
                "Confirm Demo Order", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;

        placeOrderButton.setEnabled(false);
        int orderId = billingService.completeSale(lines, bill, customer.getUserId(), null, method, "DEMO");
        if (orderId > 0) {
            cartDAO.clearCart(cart.getCartId());
            JOptionPane.showMessageDialog(this,
                    "Demo order #" + orderId + " saved.\nMethod: " + method +
                    "\nStatus: DEMO (not paid).\nNo payment was processed.");
            Navigation.back(this);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Order could not be saved. Check your database / payment upgrade script and product stock.");
            placeOrderButton.setEnabled(true);
        }
    }
}
