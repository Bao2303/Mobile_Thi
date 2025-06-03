package vn.edu.tlu.cse.nthx.qlmh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.tlu.cse.nthx.qlmh.R;
import vn.edu.tlu.cse.nthx.qlmh.database.DatabaseHelper;
import vn.edu.tlu.cse.nthx.qlmh.models.User;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    Button buttonLogin;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        // Khởi tạo DatabaseHelper ngay lập tức để đảm bảo DB được tạo nếu chưa có
        databaseHelper = new DatabaseHelper(this);
        // Gọi getReadableDatabase hoặc getWritableDatabase để kích hoạt onCreate/onUpgrade nếu cần
        databaseHelper.getReadableDatabase();


        buttonLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Vui lòng nhập email");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Vui lòng nhập mật khẩu");
            editTextPassword.requestFocus();
            return;
        }

        // Thực hiện kiểm tra đăng nhập với DB
        User user = databaseHelper.getUser(email, password);

        if (user != null) {
            // Đăng nhập thành công
            Toast.makeText(this, "Đăng nhập thành công! Xin chào " + user.getName(), Toast.LENGTH_SHORT).show();

            // Chuyển hướng dựa trên vai trò
            Intent intent;
            if ("Nhân viên".equals(user.getRole())) {
                intent = new Intent(LoginActivity.this, StaffHomeActivity.class); // Tạo activity này sau
            } else { // Mặc định là Khách hàng
                intent = new Intent(LoginActivity.this, CustomerHomeActivity.class);
            }
            // Gửi email của user đăng nhập sang activity tiếp theo
            intent.putExtra("USER_EMAIL", user.getEmail());
            intent.putExtra("USER_NAME", user.getName()); // Gửi cả tên nếu cần hiển thị

            // Xóa activity login khỏi stack để không quay lại được bằng nút back
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Kết thúc LoginActivity

        } else {
            // Đăng nhập thất bại
            Toast.makeText(this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_LONG).show();
        }
    }
}