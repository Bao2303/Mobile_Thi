package vn.edu.tlu.cse.nthx.qlmh.models;

public class Order {
    private int id;
    private String customerEmail;
    private String orderDate; // Lưu dạng text YYYY-MM-DD HH:MM:SS hoặc chỉ YYYY-MM-DD
    private double totalAmount;
    private String status; // "Đã thanh toán", "Chưa thanh toán"

    public Order() {
    }

    public Order(int id, String customerEmail, String orderDate, double totalAmount, String status) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}