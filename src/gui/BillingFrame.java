package gui;

import model.Product;
import model.User;
import service.BillingService;
import service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BillingFrame extends JFrame {

    private final User cashier;
    private final InventoryService inventoryService = new InventoryService();
    private final BillingService billingService = new BillingService();

    private final JTextField searchField = new JTextField(15);
    private final DefaultTableModel billModel;
    private final JTable billTable;
    private final JTextField discountField = new JTextField("0", 5);
    private final JTextField paymentField = new JTextField(8);

    private final JLabel subtotalLabel = new JLabel("0.00");
    private final JLabel vatLabel = new JLabel("0.00");
    private final JLabel grandTotalLabel = new JLabel("0.00");
    private final JLabel changeLabel = new JLabel("0.00");

    private final List<BillingService.BillLine> lines = new ArrayList<>();

    public BillingFrame(User cashier) {
        this.cashier = cashier;
        setTitle("Amar Dokan - Cashier Billing");
        setSize(1120, 740);
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout());

        // Top: search
        JPanel top = new SoftPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(Navigation.backButton(this));
        top.add(new JLabel("AMAR DOKAN  |  "));
        top.add(new JLabel("Search Product:"));
        top.add(searchField);
        JButton searchBtn = new JButton("Search / Add");
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        // Center: bill table
        String[] columns = {"Product", "Qty", "Price", "Total"};
        billModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        billTable = new JTable(billModel);
        add(new JScrollPane(billTable), BorderLayout.CENTER);

        // Bottom: totals & payment
        JPanel bottom = new SoftPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.gridx = 0; gc.gridy = 0;

        JButton removeBtn = new JButton("Remove Selected");
        bottom.add(removeBtn, gc);
        gc.gridx = 1;
        bottom.add(new JLabel("Discount %:"), gc);
        gc.gridx = 2;
        bottom.add(discountField, gc);

        gc.gridx = 0; gc.gridy = 1;
        bottom.add(new JLabel("Subtotal:"), gc);
        gc.gridx = 1;
        bottom.add(subtotalLabel, gc);
        gc.gridx = 2;
        bottom.add(new JLabel("VAT (5%):"), gc);
        gc.gridx = 3;
        bottom.add(vatLabel, gc);

        gc.gridx = 0; gc.gridy = 2;
        bottom.add(new JLabel("Grand Total:"), gc);
        gc.gridx = 1;
        bottom.add(grandTotalLabel, gc);
        gc.gridx = 2;
        bottom.add(new JLabel("Payment:"), gc);
        gc.gridx = 3;
        bottom.add(paymentField, gc);

        gc.gridx = 0; gc.gridy = 3;
        bottom.add(new JLabel("Change:"), gc);
        gc.gridx = 1;
        bottom.add(changeLabel, gc);

        JButton recalcBtn = new JButton("Recalculate");
        gc.gridx = 2;
        bottom.add(recalcBtn, gc);

        JButton generateBtn = new JButton("GENERATE BILL");
        generateBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        gc.gridx = 3;
        bottom.add(generateBtn, gc);

        add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> searchAndAdd());
        removeBtn.addActionListener(e -> removeSelected());
        recalcBtn.addActionListener(e -> recalc());
        generateBtn.addActionListener(e -> generateBill());
    }

    private void searchAndAdd() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;
        List<Product> results = inventoryService.searchProducts(keyword);
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching product found.");
            return;
        }
        Product product = results.get(0);
        if (!product.isPurchasable()) {
            JOptionPane.showMessageDialog(this, product.getProductName() + " is expired or out of stock.");
            return;
        }

        String qtyInput = JOptionPane.showInputDialog(this, "Quantity for " + product.getProductName() + ":", "1");
        if (qtyInput == null) return;
        int qty;
        try {
            qty = Integer.parseInt(qtyInput.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity.");
            return;
        }
        if (qty <= 0 || qty > product.getStockQuantity()) {
            JOptionPane.showMessageDialog(this, "Insufficient stock.");
            return;
        }

        lines.add(new BillingService.BillLine(product, qty));
        refreshBillTable();
        searchField.setText("");
    }

    private void removeSelected() {
        int row = billTable.getSelectedRow();
        if (row < 0) return;
        lines.remove(row);
        refreshBillTable();
    }

    private void refreshBillTable() {
        billModel.setRowCount(0);
        for (BillingService.BillLine line : lines) {
            billModel.addRow(new Object[]{
                    line.product.getProductName(), line.quantity,
                    String.format("%.2f", line.product.getPrice()),
                    String.format("%.2f", line.getLineTotal())
            });
        }
        recalc();
    }

    private void recalc() {
        double discount = parseOrZero(discountField.getText());
        double payment = parseOrZero(paymentField.getText());
        BillingService.BillResult bill = billingService.calculate(lines, discount, Math.max(payment, 0));

        double subtotal = 0;
        for (BillingService.BillLine line : lines) subtotal += line.getLineTotal();
        subtotalLabel.setText(String.format("%.2f", subtotal));

        if (bill.isValid()) {
            vatLabel.setText(String.format("%.2f", bill.vat));
            grandTotalLabel.setText(String.format("%.2f", bill.grandTotal));
            changeLabel.setText(String.format("%.2f", bill.changeAmount));
        } else {
            vatLabel.setText("-");
            grandTotalLabel.setText("-");
            changeLabel.setText("-");
        }
    }

    private void generateBill() {
        if (lines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one product.");
            return;
        }
        double discount = parseOrZero(discountField.getText());
        double payment = parseOrZero(paymentField.getText());

        BillingService.BillResult bill = billingService.calculate(lines, discount, payment);
        if (!bill.isValid()) {
            JOptionPane.showMessageDialog(this, bill.error);
            return;
        }

        int orderId = billingService.completeSale(lines, bill, null, cashier.getUserId());
        if (orderId == -1) {
            JOptionPane.showMessageDialog(this, "Failed to generate bill.");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Invoice generated.\nGrand Total: " + bill.grandTotal + "\nChange: " + bill.changeAmount);

        lines.clear();
        refreshBillTable();
        paymentField.setText("");
        discountField.setText("0");
    }

    private double parseOrZero(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
