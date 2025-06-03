package vn.edu.tlu.cse.nthx.qlmh.models;

public class CartItem {
    private int id;
    private String userEmail;
    private int productId;
    private int quantity;

    // Thêm các trường này để hiển thị dễ dàng hơn trong giỏ hàng
    // Hoặc bạn có thể truy vấn thông tin sản phẩm khi cần
    private String productName;
    private double productPrice;


    public CartItem() {
    }

    // Constructor dùng khi thêm vào giỏ hàng
    public CartItem(String userEmail, int productId, int quantity) {
        this.userEmail = userEmail;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Constructor đầy đủ (dùng khi lấy từ DB ra)
    public CartItem(int id, String userEmail, int productId, int quantity) {
        this.id = id;
        this.userEmail = userEmail;
        this.productId = productId;
        this.quantity = quantity;
    }


    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Getters/Setters cho thông tin sản phẩm (nếu bạn thêm vào)
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    // Tính thành tiền cho item này
    public double getTotalPrice() {
        return this.productPrice * this.quantity;
    }
}