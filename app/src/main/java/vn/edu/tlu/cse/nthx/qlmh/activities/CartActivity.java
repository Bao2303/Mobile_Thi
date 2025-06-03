package vn.edu.tlu.cse.nthx.qlmh.activities;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tlu.cse.nthx.qlmh.R;
import vn.edu.tlu.cse.nthx.qlmh.adapters.CartAdapter;
import vn.edu.tlu.cse.nthx.qlmh.database.DatabaseHelper;
import vn.edu.tlu.cse.nthx.qlmh.models.CartItem;
import vn.edu.tlu.cse.nthx.qlmh.models.Order;
import vn.edu.tlu.cse.nthx.qlmh.models.Product; // Cần import Product

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    RecyclerView recyclerViewCartItems;
    CartAdapter cartAdapter;
    List<CartItem> cartItemList = new ArrayList<>();
    DatabaseHelper databaseHelper;
    TextView textViewTotalAmount, textViewCartTitle, textViewEmptyCart;
    Button buttonCheckout;
    LinearLayout layoutCheckout; // Layout chứa tổng tiền và nút checkout

    String currentUserEmail;
    private static final String TAG = "CartActivity";
    private Locale localeVN = new Locale("vi", "VN");
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        databaseHelper = new DatabaseHelper(this);
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        textViewCartTitle = findViewById(R.id.textViewCartTitle);
        textViewEmptyCart = findViewById(R.id.textViewEmptyCart);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        layoutCheckout = findViewById(R.id.layoutCheckout); // Ánh xạ layout


        currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
        if (currentUserEmail == null) {
            Log.e(TAG, "User email not passed to CartActivity");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin giỏ hàng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, "Viewing cart for user: " + currentUserEmail);
        textViewCartTitle.setText("Giỏ Hàng Của " + currentUserEmail); // Hiển thị email user

        setupRecyclerView();
        loadCartItems(); // Load dữ liệu và cập nhật UI ban đầu

        // --- Phần bị thiếu đây ---
        buttonCheckout.setOnClickListener(v -> {
            // Kiểm tra xem giỏ hàng có trống không trước khi thanh toán
            if (cartItemList.isEmpty()) {
                Toast.makeText(CartActivity.this, "Giỏ hàng trống, không thể thanh toán.", Toast.LENGTH_SHORT).show();
            } else {
                confirmCheckout(); // Gọi hàm xác nhận thanh toán
            }
        });
        // --- Kết thúc phần bị thiếu ---
    } // Kết thúc onCreate

    private void setupRecyclerView() {
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(this));
        // Khởi tạo adapter với danh sách rỗng ban đầu và listener là activity này
        cartAdapter = new CartAdapter(this, cartItemList, this);
        recyclerViewCartItems.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        Log.d(TAG, "Loading cart items for: " + currentUserEmail);
        // Lấy dữ liệu mới từ DB
        List<CartItem> updatedCartList = databaseHelper.getCartItems(currentUserEmail);
        // Cập nhật danh sách trong Activity và thông báo cho Adapter
        cartItemList.clear();
        cartItemList.addAll(updatedCartList);
        cartAdapter.notifyDataSetChanged(); // Thông báo adapter có thay đổi dữ liệu hoàn toàn

        updateTotalAmount(); // Cập nhật tổng tiền
        checkEmptyCart(); // Kiểm tra và cập nhật trạng thái giỏ hàng trống
        Log.d(TAG, "Loaded " + cartItemList.size() + " items.");
    }


    // Cập nhật hiển thị tổng tiền
    private void updateTotalAmount() {
        double total = 0;
        for (CartItem item : cartItemList) {
            total += item.getTotalPrice(); // Dùng phương thức getTotalPrice() đã thêm trong CartItem
        }
        textViewTotalAmount.setText("Tổng tiền: " + currencyFormatter.format(total));
    }

    // Kiểm tra giỏ hàng trống và ẩn/hiện các view tương ứng
    private void checkEmptyCart() {
        if (cartItemList.isEmpty()) {
            recyclerViewCartItems.setVisibility(View.GONE);
            textViewEmptyCart.setVisibility(View.VISIBLE);
            layoutCheckout.setVisibility(View.GONE); // Ẩn layout tổng tiền và checkout
        } else {
            recyclerViewCartItems.setVisibility(View.VISIBLE);
            textViewEmptyCart.setVisibility(View.GONE);
            layoutCheckout.setVisibility(View.VISIBLE); // Hiện layout tổng tiền và checkout
        }
    }


    // --- Thực thi Interface CartAdapter.CartItemListener ---

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        Log.d(TAG, "Quantity change requested for item ID " + item.getId() + " to " + newQuantity);
        // 1. Lấy thông tin sản phẩm để kiểm tra tồn kho
        Product product = databaseHelper.getProductById(item.getProductId());
        if (product == null) {
            Log.e(TAG,"Product not found for stock check (ID: " + item.getProductId() + ")");
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm.", Toast.LENGTH_SHORT).show();
            // Load lại giỏ hàng để loại bỏ item lỗi hoặc cập nhật trạng thái
            loadCartItems();
            return;
        }

        // 2. Kiểm tra tồn kho
        if (newQuantity > product.getStockQuantity()) {
            Toast.makeText(this, "Số lượng '" + product.getName() + "' vượt quá tồn kho (" + product.getStockQuantity() + ")", Toast.LENGTH_SHORT).show();
            // Không cập nhật DB, load lại để reset UI
            loadCartItems();
        } else if (newQuantity < 1) {
            // Nếu số lượng mới < 1, coi như xóa item
            onItemRemoved(item);
        }
        else {
            // 3. Cập nhật số lượng trong database
            int rowsAffected = databaseHelper.updateCartItemQuantity(item.getId(), newQuantity);
            if (rowsAffected > 0) {
                Log.d(TAG,"Quantity updated in DB for item ID " + item.getId());
                // 4. Load lại danh sách giỏ hàng để cập nhật RecyclerView và tổng tiền
                loadCartItems();
            } else {
                Log.e(TAG,"Failed to update quantity in DB for item ID " + item.getId());
                Toast.makeText(this, "Lỗi khi cập nhật số lượng.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemRemoved(CartItem item) {
        Log.d(TAG, "Remove requested for item ID " + item.getId());
        // Hiển thị hộp thoại xác nhận trước khi xóa
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa '" + item.getProductName() + "' khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Thực hiện xóa item khỏi database
                    databaseHelper.removeCartItem(item.getId());
                    Log.d(TAG,"Item removed from DB: ID " + item.getId());
                    // Load lại danh sách giỏ hàng
                    loadCartItems();
                    Toast.makeText(CartActivity.this, "Đã xóa '" + item.getProductName() + "'", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null) // Không làm gì khi nhấn Hủy
                .show();
    }

    // --- Xử lý Thanh toán (Checkout) ---

    // Hiển thị hộp thoại xác nhận thanh toán
    private void confirmCheckout() {
        // Tính lại tổng tiền một lần nữa cho chắc chắn
        double finalTotal = 0;
        // Dùng danh sách hiện tại trong Activity (đã được cập nhật)
        for (CartItem item : cartItemList) {
            finalTotal += item.getTotalPrice();
        }
        String totalFormatted = currencyFormatter.format(finalTotal);

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Thanh toán")
                .setMessage("Tổng số tiền cần thanh toán là: " + totalFormatted + "\n\nBạn có chắc chắn muốn đặt hàng?")
                .setPositiveButton("Đặt hàng", (dialog, which) -> {
                    processCheckout(); // Tiến hành xử lý thanh toán
                })
                .setNegativeButton("Xem lại", null) // Đóng hộp thoại
                .show();
    }


    // Hàm xử lý chính việc thanh toán
    private void processCheckout() {
        Log.i(TAG, "Processing checkout for user: " + currentUserEmail);

        // Quan trọng: Lấy lại danh sách giỏ hàng MỚI NHẤT từ DB trước khi xử lý
        // để đảm bảo không có thay đổi nào từ lúc load đến lúc checkout
        List<CartItem> currentCartState = databaseHelper.getCartItems(currentUserEmail);
        if (currentCartState.isEmpty()) {
            Log.w(TAG, "Checkout aborted: Cart is empty upon final check.");
            Toast.makeText(this, "Giỏ hàng đã trống.", Toast.LENGTH_SHORT).show();
            loadCartItems(); // Cập nhật lại UI
            return;
        }


        // 2. Kiểm tra tồn kho cho TẤT CẢ sản phẩm trong giỏ (dựa trên trạng thái mới nhất)
        boolean allInStock = true;
        List<String> outOfStockItems = new ArrayList<>();
        double orderTotal = 0; // Tính tổng tiền dựa trên trạng thái mới nhất

        for(CartItem item : currentCartState) {
            Product product = databaseHelper.getProductById(item.getProductId());
            if (product == null || item.getQuantity() > product.getStockQuantity()) {
                allInStock = false;
                outOfStockItems.add(item.getProductName() + " (Yêu cầu: " + item.getQuantity() + ", Tồn kho: " + (product != null ? product.getStockQuantity() : "N/A") + ")");
            }
            // Tính tổng tiền ngay trong vòng lặp này
            if(product != null) {
                // Dùng giá từ DB để đảm bảo chính xác nhất
                orderTotal += product.getPrice() * item.getQuantity();
            } else {
                // Xử lý nếu sản phẩm bị xóa trong lúc người dùng ở giỏ hàng?
                Log.e(TAG, "Product ID " + item.getProductId() + " not found during final checkout calculation!");
                allInStock = false; // Coi như không đủ hàng nếu sản phẩm không tồn tại
                outOfStockItems.add(item.getProductName() + " (Không tìm thấy)");
            }
        }

        if (!allInStock) {
            Log.w(TAG, "Checkout aborted: Not enough stock for some items.");
            // Hiển thị thông báo lỗi chi tiết
            StringBuilder stockErrorMessage = new StringBuilder("Không đủ hàng hoặc có lỗi với các sản phẩm sau:\n");
            for (String error : outOfStockItems) {
                stockErrorMessage.append("- ").append(error).append("\n");
            }
            stockErrorMessage.append("\nVui lòng cập nhật lại giỏ hàng.");
            new AlertDialog.Builder(this)
                    .setTitle("Lỗi Tồn Kho")
                    .setMessage(stockErrorMessage.toString())
                    .setPositiveButton("OK", null)
                    .show();
            loadCartItems(); // Load lại giỏ hàng để người dùng thấy trạng thái đúng
            return; // Dừng thanh toán
        }


        // 4. Tạo đơn hàng mới (Order) - dùng orderTotal đã tính
        Order newOrder = new Order();
        newOrder.setCustomerEmail(currentUserEmail);
        newOrder.setOrderDate(getCurrentDateTimeFormatted()); // Lấy ngày giờ hiện tại
        newOrder.setTotalAmount(orderTotal);
        newOrder.setStatus("Đã thanh toán"); // Hoặc "Chờ xử lý", "Chưa thanh toán" tùy logic

        long orderId = databaseHelper.addOrder(newOrder);

        if (orderId == -1) {
            Log.e(TAG, "Checkout failed: Could not create order in DB.");
            Toast.makeText(this, "Đặt hàng thất bại! Không thể tạo đơn hàng.", Toast.LENGTH_LONG).show();
            return; // Dừng lại nếu không tạo được đơn hàng
        }
        Log.i(TAG, "Order created successfully with ID: " + orderId + ", Total: " + orderTotal);


        // 5. Cập nhật số lượng tồn kho sản phẩm (dùng currentCartState)
        try {
            for (CartItem item : currentCartState) {
                Product product = databaseHelper.getProductById(item.getProductId());
                // Kiểm tra lại product != null (dù đã kiểm tra ở trên nhưng để an toàn)
                if (product != null) {
                    int newStock = product.getStockQuantity() - item.getQuantity();
                    product.setStockQuantity(newStock);
                    databaseHelper.updateProduct(product); // Cập nhật vào DB
                    Log.d(TAG, "Updated stock for product ID " + product.getId() + " to " + newStock);
                } else {
                    Log.e(TAG, "Could not find product ID " + item.getProductId() + " during stock update after checkout (this should not happen here).");
                    // Cân nhắc xử lý lỗi này
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating product stock after checkout", e);
            // Lỗi nghiêm trọng, thông báo và không nên xóa giỏ hàng
            Toast.makeText(this, "Lỗi nghiêm trọng khi cập nhật kho. Đơn hàng đã tạo (ID: "+orderId+"), vui lòng liên hệ quản trị.", Toast.LENGTH_LONG).show();
            // Không xóa giỏ hàng, không chuyển màn hình để người dùng/admin xử lý
            return;
        }


        // 6. Xóa giỏ hàng của người dùng (dùng email)
        databaseHelper.clearCart(currentUserEmail);
        Log.i(TAG, "Cart cleared for user: " + currentUserEmail);

        // 7. Thông báo thành công và xử lý tiếp theo
        Toast.makeText(this, "Đặt hàng thành công! Mã đơn hàng: #" + orderId, Toast.LENGTH_LONG).show();

        // Chuyển về màn hình chính
        Intent homeIntent = new Intent(CartActivity.this, CustomerHomeActivity.class);
        homeIntent.putExtra("USER_EMAIL", currentUserEmail); // Gửi lại email
        homeIntent.putExtra("USER_NAME", getIntent().getStringExtra("USER_NAME")); // Gửi lại tên nếu có
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish(); // Đóng CartActivity

    }

    // Hàm tiện ích lấy ngày giờ hiện tại định dạng chuẩn
    private String getCurrentDateTimeFormatted() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

} // Kết thúc lớp CartActivity