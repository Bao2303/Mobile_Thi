package vn.edu.tlu.cse.nthx.qlmh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView; // Import này

import java.util.ArrayList;
import java.util.List;

import vn.edu.tlu.cse.nthx.qlmh.R;
import vn.edu.tlu.cse.nthx.qlmh.adapters.ProductCustomerAdapter;
import vn.edu.tlu.cse.nthx.qlmh.database.DatabaseHelper;
import vn.edu.tlu.cse.nthx.qlmh.models.CartItem;
import vn.edu.tlu.cse.nthx.qlmh.models.Product;

public class CustomerHomeActivity extends AppCompatActivity implements ProductCustomerAdapter.OnAddToCartClickListener {

    RecyclerView recyclerViewProducts;
    ProductCustomerAdapter productAdapter;
    List<Product> productList = new ArrayList<>();
    DatabaseHelper databaseHelper;
    BottomNavigationView bottomNavigationView;
    TextView textViewWelcome;

    String currentUserEmail;
    String currentUserName;
    private static final String TAG = "CustomerHomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        databaseHelper = new DatabaseHelper(this);
        recyclerViewProducts = findViewById(R.id.recyclerViewProductsCustomer);
        bottomNavigationView = findViewById(R.id.bottom_navigation_customer);
        textViewWelcome = findViewById(R.id.textViewWelcome);

        // Nhận thông tin người dùng từ LoginActivity
        currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
        currentUserName = getIntent().getStringExtra("USER_NAME");

        if (currentUserName != null) {
            textViewWelcome.setText("Chào mừng, " + currentUserName + "!");
        } else {
            textViewWelcome.setText("Chào mừng Khách hàng!");
        }

        if (currentUserEmail == null) {
            Log.e(TAG, "User email not passed to CustomerHomeActivity");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng.", Toast.LENGTH_LONG).show();
            // Có thể quay lại Login hoặc xử lý khác
            finish();
            return;
        }
        Log.d(TAG, "Current user email: " + currentUserEmail);


        setupRecyclerView();
        loadProducts();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load lại sản phẩm khi quay lại màn hình này (ví dụ sau khi checkout)
        // để cập nhật số lượng tồn kho
        loadProducts();
    }


    private void setupRecyclerView() {
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductCustomerAdapter(this, productList, this); // Truyền "this" vì Activity implement interface
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void loadProducts() {
        productList = databaseHelper.getAllProducts();
        if (productList.isEmpty()) {
            Log.w(TAG, "No products found in database.");
            Toast.makeText(this, "Không có sản phẩm nào.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Loaded " + productList.size() + " products.");
        }
        productAdapter.updateData(productList);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home_customer) {
                    // Đang ở trang chủ rồi, không cần làm gì
                    return true;
                } else if (itemId == R.id.navigation_cart) {
                    // Chuyển sang màn hình giỏ hàng
                    Intent cartIntent = new Intent(CustomerHomeActivity.this, CartActivity.class);
                    cartIntent.putExtra("USER_EMAIL", currentUserEmail); // Truyền email sang CartActivity
                    startActivity(cartIntent);
                    return true;
                } else if (itemId == R.id.navigation_logout_customer) {
                    // Đăng xuất
                    logout();
                    return true;
                }
                return false;
            }
        });
        // Đặt item home được chọn mặc định
        bottomNavigationView.setSelectedItemId(R.id.navigation_home_customer);
    }

    // Xử lý sự kiện khi nhấn nút "Thêm" từ adapter
    @Override
    public void onAddToCartClick(Product product) {
        Log.d(TAG, "Adding product to cart: " + product.getName() + " for user " + currentUserEmail);
        // Tạo CartItem mới với số lượng là 1
        CartItem newItem = new CartItem(currentUserEmail, product.getId(), 1);
        databaseHelper.addOrUpdateCartItem(newItem);
        Toast.makeText(this, product.getName() + " đã được thêm vào giỏ!", Toast.LENGTH_SHORT).show();
        // Không cần load lại danh sách sản phẩm ở đây, vì chỉ thêm vào giỏ
        // Cập nhật số lượng tồn kho chỉ xảy ra khi thanh toán
    }

    private void logout() {
        // Xóa thông tin phiên đăng nhập nếu có (ví dụ SharedPreferences) - Ở đây không cần

        // Quay về màn hình Login
        Intent intent = new Intent(CustomerHomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Đóng activity hiện tại
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }
}