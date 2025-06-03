package vn.edu.tlu.cse.nthx.qlmh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.tlu.cse.nthx.qlmh.R;
import vn.edu.tlu.cse.nthx.qlmh.database.DatabaseHelper;
import vn.edu.tlu.cse.nthx.qlmh.models.Product;

public class AddEditProductActivity extends AppCompatActivity {

    EditText editTextProductName, editTextProductCategory, editTextProductPrice, editTextProductStock;
    Button buttonSaveProduct, buttonCancel;
    TextView textViewTitle;
    DatabaseHelper databaseHelper;

    private int productIdToEdit = -1; // ID sản phẩm cần sửa, -1 nếu là thêm mới
    private static final String TAG = "AddEditProductActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductCategory = findViewById(R.id.editTextProductCategory);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextProductStock = findViewById(R.id.editTextProductStock);
        buttonSaveProduct = findViewById(R.id.buttonSaveProduct);
        buttonCancel = findViewById(R.id.buttonCancel);
        textViewTitle = findViewById(R.id.textViewAddEditTitle);
        databaseHelper = new DatabaseHelper(this);

        // Kiểm tra xem có Intent và dữ liệu PRODUCT_ID được gửi đến không
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("PRODUCT_ID")) {
            productIdToEdit = intent.getIntExtra("PRODUCT_ID", -1);
            Log.d(TAG, "Editing product with ID: " + productIdToEdit);
            if (productIdToEdit != -1) {
                textViewTitle.setText("Sửa Thông Tin Sản Phẩm");
                loadProductData(productIdToEdit);
                buttonSaveProduct.setText("Cập nhật");
            } else {
                Log.w(TAG,"Invalid PRODUCT_ID received.");
                textViewTitle.setText("Thêm Sản Phẩm Mới"); // Vẫn là thêm mới nếu ID không hợp lệ
            }
        } else {
            Log.d(TAG,"Adding new product.");
            textViewTitle.setText("Thêm Sản Phẩm Mới");
        }

        buttonSaveProduct.setOnClickListener(v -> saveProduct());
        buttonCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED); // Đặt kết quả là Canceled khi hủy
            finish(); // Đóng activity
        });
    }

    private void loadProductData(int productId) {
        Product product = databaseHelper.getProductById(productId);
        if (product != null) {
            editTextProductName.setText(product.getName());
            editTextProductCategory.setText(product.getCategory());
            editTextProductPrice.setText(String.valueOf(product.getPrice()));
            editTextProductStock.setText(String.valueOf(product.getStockQuantity()));
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm để sửa.", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Product with ID " + productId + " not found for editing.");
            finish(); // Đóng activity nếu không tìm thấy sản phẩm
        }
    }


    private void saveProduct() {
        String name = editTextProductName.getText().toString().trim();
        String category = editTextProductCategory.getText().toString().trim();
        String priceStr = editTextProductPrice.getText().toString().trim();
        String stockStr = editTextProductStock.getText().toString().trim();

        // Kiểm tra dữ liệu nhập
        if (TextUtils.isEmpty(name)) {
            editTextProductName.setError("Tên sản phẩm không được để trống");
            editTextProductName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(category)) {
            editTextProductCategory.setError("Danh mục không được để trống");
            editTextProductCategory.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            editTextProductPrice.setError("Giá không được để trống");
            editTextProductPrice.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(stockStr)) {
            editTextProductStock.setError("Số lượng tồn không được để trống");
            editTextProductStock.requestFocus();
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) throw new NumberFormatException("Price cannot be negative");
        } catch (NumberFormatException e) {
            editTextProductPrice.setError("Giá không hợp lệ");
            editTextProductPrice.requestFocus();
            return;
        }
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) throw new NumberFormatException("Stock cannot be negative");
        } catch (NumberFormatException e) {
            editTextProductStock.setError("Số lượng không hợp lệ");
            editTextProductStock.requestFocus();
            return;
        }

        // Tạo đối tượng Product
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStockQuantity(stock);

        if (productIdToEdit == -1) {
            // Thêm mới
            databaseHelper.addProduct(product);
            Toast.makeText(this, "Đã thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "New product added: " + name);
        } else {
            // Cập nhật
            product.setId(productIdToEdit); // Đặt ID cho sản phẩm cần cập nhật
            int rowsAffected = databaseHelper.updateProduct(product);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Product updated: ID " + productIdToEdit);
            } else {
                Toast.makeText(this, "Cập nhật sản phẩm thất bại.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to update product: ID " + productIdToEdit);
            }
        }

        // Đặt kết quả là OK để activity trước (StaffHomeActivity) biết và load lại list
        setResult(RESULT_OK);
        finish(); // Đóng activity sau khi lưu
    }
}