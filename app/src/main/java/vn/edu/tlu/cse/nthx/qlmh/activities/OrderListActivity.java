package vn.edu.tlu.cse.nthx.qlmh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import vn.edu.tlu.cse.nthx.qlmh.adapters.OrderAdapter;
import vn.edu.tlu.cse.nthx.qlmh.database.DatabaseHelper;
import vn.edu.tlu.cse.nthx.qlmh.models.Order;

public class OrderListActivity extends AppCompatActivity {

    RecyclerView recyclerViewOrders;
    OrderAdapter orderAdapter;
    List<Order> orderList = new ArrayList<>();
    DatabaseHelper databaseHelper;
    BottomNavigationView bottomNavigationView;
    TextView textViewNoOrders; // TextView hiển thị khi không có đơn hàng
    String currentStaffEmail;
    String currentStaffName;
    private static final String TAG = "OrderListActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        databaseHelper = new DatabaseHelper(this);
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        bottomNavigationView = findViewById(R.id.bottom_navigation_staff_orders);
        textViewNoOrders = findViewById(R.id.textViewNoOrders);

        // Nhận thông tin nhân viên (nếu cần dùng)
        currentStaffEmail = getIntent().getStringExtra("USER_EMAIL");
        currentStaffName = getIntent().getStringExtra("USER_NAME");
        Log.d(TAG, "Viewing orders as staff: " + currentStaffEmail);


        setupRecyclerView();
        loadOrders();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load lại đơn hàng khi quay lại màn hình này (nếu có thể có thay đổi)
        loadOrders();
        // Đảm bảo item "Đơn hàng" được chọn trên bottom nav
        bottomNavigationView.setSelectedItemId(R.id.navigation_orders_staff);
    }

    private void setupRecyclerView() {
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        // Khởi tạo adapter (chưa cần listener ở đây)
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        orderList = databaseHelper.getAllOrders();
        if (orderList.isEmpty()) {
            Log.w(TAG, "No orders found.");
            recyclerViewOrders.setVisibility(View.GONE); // Ẩn RecyclerView
            textViewNoOrders.setVisibility(View.VISIBLE); // Hiện thông báo
        } else {
            Log.d(TAG, "Loaded " + orderList.size() + " orders.");
            recyclerViewOrders.setVisibility(View.VISIBLE); // Hiện RecyclerView
            textViewNoOrders.setVisibility(View.GONE); // Ẩn thông báo
            orderAdapter.updateData(orderList);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_products_staff) {
                // Quay lại màn hình quản lý sản phẩm
                // Không cần tạo Intent mới nếu StaffHomeActivity chưa bị finish()
                // Nếu bạn đã finish() StaffHomeActivity khi chuyển sang đây, thì phải tạo Intent mới
                finish(); // Đóng activity hiện tại để quay lại StaffHomeActivity (nếu nó chưa bị đóng)
                // Hoặc:
                // Intent productIntent = new Intent(OrderListActivity.this, StaffHomeActivity.class);
                // productIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Quay lại instance đã có
                // productIntent.putExtra("USER_EMAIL", currentStaffEmail); // Gửi lại nếu cần
                // productIntent.putExtra("USER_NAME", currentStaffName);
                // startActivity(productIntent);
                // finish();
                return true;
            } else if (itemId == R.id.navigation_orders_staff) {
                // Đang ở trang đơn hàng rồi
                return true;
            } else if (itemId == R.id.navigation_logout_staff) {
                // Đăng xuất
                logout();
                return true;
            }
            return false;
        });
        // Đặt item đơn hàng được chọn mặc định
        bottomNavigationView.setSelectedItemId(R.id.navigation_orders_staff);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    // Quay về màn hình Login
                    Intent intent = new Intent(OrderListActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAffinity(); // Đóng tất cả activity của ứng dụng trong task này
                    Toast.makeText(OrderListActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}