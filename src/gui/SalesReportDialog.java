package gui;

import dao.OrderDAO;
import model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SalesReportDialog extends JDialog {

    public SalesReportDialog(Frame owner) {
        super(owner, "Sales Report", true);
        setSize(970, 630);
        setLocationRelativeTo(owner);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout());

        String[] columns = {"Invoice #", "Date", "Customer ID", "Cashier ID", "Subtotal", "Discount", "VAT", "Grand Total", "Payment Method", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        OrderDAO orderDAO = new OrderDAO();
        List<Order> orders = orderDAO.getAllOrders();
        double totalSales = 0;
        int demoOrders = 0;
        for (Order o : orders) {
            model.addRow(new Object[]{o.getInvoiceNumber(), o.getOrderDate(), o.getCustomerId(),
                    o.getCashierId(), o.getSubtotal(), o.getDiscount(), o.getVat(), o.getGrandTotal(),
                    o.getPaymentMethod(), o.getPaymentStatus()});
            if ("DEMO".equals(o.getPaymentStatus())) demoOrders++;
            else totalSales += o.getGrandTotal();
        }

        JLabel summary = new JLabel("Orders: " + orders.size() + "   |   Recorded Sales: " + String.format("%.2f", totalSales) +
                "   |   Demo Orders: " + demoOrders);
        summary.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel bottom = new SoftPanel(new BorderLayout());
        JButton back = new JButton("< Back");
        back.addActionListener(e -> dispose());
        bottom.add(back, BorderLayout.WEST);
        bottom.add(summary, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
