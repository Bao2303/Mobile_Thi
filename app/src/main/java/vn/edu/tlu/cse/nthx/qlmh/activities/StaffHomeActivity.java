package vn.edu.tlu.cse.nthx.qlmh.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tlu.cse.nthx.qlmh.R;
import vn.edu.tlu.cse.nthx.qlmh.adapters.ProductStaffAdapter;
import vn.edu.tlu.cse.nthx.qlmh.database.DatabaseHelper;
import vn.edu.tlu.cse.nthx.qlmh.models.Product;

public class StaffHomeActivity extends AppCompatActivity implements ProductStaffAdapter.ProductStaffListener {

    RecyclerView recyclerViewProductsStaff;
    ProductStaffAdapter productStaffAdapter;
    List<Product> productList = new ArrayList<>();
    DatabaseHelper databaseHelper;
    BottomNavigationView bottomNavigationView;
    Button buttonAddNewProduct;
    TextView textViewStaffWelcome;

    String currentStaffEmail;
    String currentStaffName;
    private static final String TAG = "StaffHomeActivity";

    // Launcher để nhận kết quả từ AddEditProductActivity
    private ActivityResultLauncher<Intent> addEditProductLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        databaseHelper = new DatabaseHelper(this);
        recyclerViewProductsStaff = findViewById(R.id.recyclerViewProductsStaff);
        bottomNavigationView = findViewById(R.id.bottom_navigation_staff);
        buttonAddNewProduct = findViewById(R.id.buttonAddNewProduct);
        textViewStaffWelcome = findViewById(R.id.textViewStaffWelcome); // Ánh xạ TextView welcome


        // Nhận thông tin nhân viên từ LoginActivity
        currentStaffEmail = getIntent().getStringExtra("USER_EMAIL");
        currentStaffName = getIntent().getStringExtra("USER_NAME"); // Nhận tên

        if (currentStaffName != null) {
            textViewStaffWelcome.setText("Quản lý sản phẩm (" + currentStaffName + ")");
        } else {
            textViewStaffWelcome.setText("Quản lý sản phẩm");
        }


        if (currentStaffEmail == null) {
            Log.e(TAG, "Staff email not passed to StaffHomeActivity");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin nhân viên.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, "Current staff email: " + currentStaffEmail);


        setupRecyclerView();
        loadProducts();
        setupBottomNavigation();
        setupAddButton();

        // Khởi tạo ActivityResultLauncher
        addEditProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Nếu có kết quả OK (đã thêm/sửa thành công), load lại danh sách
                        loadProducts();
                        Toast.makeText(this, "Danh sách sản phẩm đã được cập nhật", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load lại sản phẩm khi quay lại màn hình này (có thể không cần thiết nếu dùng Launcher)
        // loadProducts();
        // Đảm bảo item "Sản phẩm" được chọn trên bottom nav
        bottomNavigationView.setSelectedItemId(R.id.navigation_products_staff);
    }


    private void setupRecyclerView() {
        recyclerViewProductsStaff.setLayoutManager(new LinearLayoutManager(this));
        productStaffAdapter = new ProductStaffAdapter(this, productList, this);
        recyclerViewProductsStaff.setAdapter(productStaffAdapter);
    }

    private void loadProducts() {
        productList = databaseHelper.getAllProducts();
        if (productList.isEmpty()) {
            Log.w(TAG, "No products found for staff management.");
            // Có thể hiển thị thông báo "Chưa có sản phẩm" trên RecyclerView
        } else {
            Log.d(TAG, "Loaded " + productList.size() + " products for staff.");
        }
        productStaffAdapter.updateData(productList);
    }

    private void setupAddButton() {
        buttonAddNewProduct.setOnClickListener(v -> {
            Intent intent = new Intent(StaffHomeActivity.this, AddEditProductActivity.class);
            // Không cần gửi ID vì đây là thêm mới
            addEditProductLauncher.launch(intent); // Mở activity và chờ kết quả
        });
    }


    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_products_staff) {
                // Đang ở trang quản lý sản phẩm rồi
                return true;
            } else if (itemId == R.id.navigation_orders_staff) {
                // Chuyển sang màn hình danh sách đơn hàng
                Intent orderIntent = new Intent(StaffHomeActivity.this, OrderListActivity.class);
                orderIntent.putExtra("USER_EMAIL", currentStaffEmail); // Gửi email nhân viên nếu cần
                orderIntent.putExtra("USER_NAME", currentStaffName);
                // Không cần FLAG_ACTIVITY_CLEAR_TOP vì có thể muốn quay lại màn hình sản phẩm
                startActivity(orderIntent);
                // finish(); // Không nên finish() ở đây nếu muốn quay lại
                return true;
            } else if (itemId == R.id.navigation_logout_staff) {
                // Đăng xuất
                logout();
                return true;
            }
            return false;
        });
        // Đặt item quản lý sản phẩm được chọn mặc định
        bottomNavigationView.setSelectedItemId(R.id.navigation_products_staff);
    }

    // Xử lý sự kiện click nút "Sửa" từ adapter
    @Override
    public void onEditClick(Product product) {
        Log.d(TAG,"Editing product: " + product.getName());
        Intent intent = new Intent(StaffHomeActivity.this, AddEditProductActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId()); // Gửi ID của sản phẩm cần sửa
        addEditProductLauncher.launch(intent); // Mở activity và chờ kết quả
    }

    // Xử lý sự kiện click nút "Xóa" từ adapter
    @Override
    public void onDeleteClick(Product product) {
        Log.d(TAG,"Attempting to delete product: " + product.getName());
        // Hiển thị hộp thoại xác nhận trước khi xóa
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm '" + product.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Thực hiện xóa
                    databaseHelper.deleteProduct(product.getId());
                    Toast.makeText(StaffHomeActivity.this, "Đã xóa sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
                    // Load lại danh sách sản phẩm
                    loadProducts();
                })
                .setNegativeButton("Hủy", null) // Không làm gì khi nhấn Hủy
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    // Quay về màn hình Login
                    Intent intent = new Intent(StaffHomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Đóng activity hiện tại
                    Toast.makeText(StaffHomeActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}