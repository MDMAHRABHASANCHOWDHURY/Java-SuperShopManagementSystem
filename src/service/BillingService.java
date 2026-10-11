package service;

import dao.OrderDAO;
import dao.ProductDAO;
import model.Order;
import model.OrderItem;
import model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralizes all billing math so totals are calculated the same way everywhere
 * (cashier POS and customer checkout), rather than duplicated in the GUI.
 */
public class BillingService {

    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    public static class BillLine {
        public final Product product;
        public int quantity;

        public BillLine(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public double getLineTotal() {
            return product.getPrice() * quantity;
        }
    }

    public static class BillResult {
        public double subtotal;
        public double discount;
        public double vat;
        public double grandTotal;
        public double amountPaid;
        public double changeAmount;
        public String error;

        public boolean isValid() {
            return error == null;
        }
    }

    private static final double VAT_RATE = 0.05; // 5% default VAT

    /**
     * Calculates subtotal, discount, VAT, grand total and change for a set of bill lines.
     * discountPercent and amountPaid are optional inputs from the cashier.
     */
    public BillResult calculate(List<BillLine> lines, double discountPercent, double amountPaid) {
        BillResult result = new BillResult();

        for (BillLine line : lines) {
            if (line.quantity > line.product.getStockQuantity()) {
                result.error = "Insufficient stock for " + line.product.getProductName();
                return result;
            }
            if (!line.product.isPurchasable()) {
                result.error = line.product.getProductName() + " is expired or out of stock.";
                return result;
            }
        }

        double subtotal = 0;
        for (BillLine line : lines) {
            subtotal += line.getLineTotal();
        }

        double discountAmount = subtotal * (discountPercent / 100.0);
        double vat = (subtotal - discountAmount) * VAT_RATE;
        double grandTotal = subtotal - discountAmount + vat;

        grandTotal = round(grandTotal);
        if (amountPaid + 0.00001 < grandTotal) {
            result.error = "Insufficient payment.";
            return result;
        }

        result.subtotal = round(subtotal);
        result.discount = round(discountAmount);
        result.vat = round(vat);
        result.grandTotal = round(grandTotal);
        result.amountPaid = round(amountPaid);
        result.changeAmount = round(amountPaid - grandTotal);
        return result;
    }

    /** Finalizes the sale: saves the order, its items, and deducts stock. Returns the new order id or -1. */
    public int completeSale(List<BillLine> lines, BillResult bill, Integer customerId, Integer cashierId) {
        return completeSale(lines, bill, customerId, cashierId, "CASH", "RECORDED");
    }

    public int completeSale(List<BillLine> lines, BillResult bill, Integer customerId, Integer cashierId,
                            String paymentMethod, String paymentStatus) {
        if (!bill.isValid()) return -1;

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setCashierId(cashierId);
        order.setInvoiceNumber(generateInvoiceNumber());
        order.setSubtotal(bill.subtotal);
        order.setDiscount(bill.discount);
        order.setVat(bill.vat);
        order.setGrandTotal(bill.grandTotal);
        order.setAmountPaid(bill.amountPaid);
        order.setChangeAmount(bill.changeAmount);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(paymentStatus);
        if ("DEMO".equals(paymentStatus)) {
            // Demonstration only: no funds were received.
            order.setAmountPaid(0);
            order.setChangeAmount(0);
        }

        List<OrderItem> items = new ArrayList<>();
        for (BillLine line : lines) {
            items.add(new OrderItem(line.product.getProductId(), line.product.getProductName(),
                    line.quantity, line.product.getPrice()));
        }
        order.setItems(items);

        return orderDAO.saveOrder(order);
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
