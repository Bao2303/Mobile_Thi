package vn.edu.tlu.cse.nthx.qlmh.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Thêm dòng này

import androidx.annotation.Nullable; // Đảm bảo import đúng

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tlu.cse.nthx.qlmh.models.CartItem;
import vn.edu.tlu.cse.nthx.qlmh.models.Order;
import vn.edu.tlu.cse.nthx.qlmh.models.Product;
import vn.edu.tlu.cse.nthx.qlmh.models.User;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "convenience_store.db";
    private static final int DATABASE_VERSION = 1; // Tăng version nếu thay đổi cấu trúc DB

    // Tên bảng
    private static final String TABLE_USERS = "Users";
    private static final String TABLE_PRODUCTS = "Products";
    private static final String TABLE_CART = "Cart";
    private static final String TABLE_ORDERS = "Orders";

    // Cột bảng Users
    private static final String COLUMN_USER_ID = "ID";
    private static final String COLUMN_USER_EMAIL = "Email";
    private static final String COLUMN_USER_PASSWORD = "Password";
    private static final String COLUMN_USER_ROLE = "Role";
    private static final String COLUMN_USER_NAME = "Name";

    // Cột bảng Products
    private static final String COLUMN_PRODUCT_ID = "ID";
    private static final String COLUMN_PRODUCT_NAME = "Name";
    private static final String COLUMN_PRODUCT_CATEGORY = "Category";
    private static final String COLUMN_PRODUCT_PRICE = "Price";
    private static final String COLUMN_PRODUCT_STOCK = "StockQuantity";

    // Cột bảng Cart
    private static final String COLUMN_CART_ID = "ID";
    private static final String COLUMN_CART_USER_EMAIL = "UserEmail"; // Khóa ngoại tới Users.Email
    private static final String COLUMN_CART_PRODUCT_ID = "ProductID"; // Khóa ngoại tới Products.ID
    private static final String COLUMN_CART_QUANTITY = "Quantity";

    // Cột bảng Orders
    private static final String COLUMN_ORDER_ID = "ID";
    private static final String COLUMN_ORDER_CUSTOMER_EMAIL = "CustomerEmail"; // Khóa ngoại tới Users.Email
    private static final String COLUMN_ORDER_DATE = "OrderDate";
    private static final String COLUMN_ORDER_TOTAL = "TotalAmount";
    private static final String COLUMN_ORDER_STATUS = "Status";

    private static final String TAG = "DatabaseHelper"; // Tag để log


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Users
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_EMAIL + " TEXT UNIQUE," // Email là duy nhất
                + COLUMN_USER_PASSWORD + " TEXT,"
                + COLUMN_USER_ROLE + " TEXT,"
                + COLUMN_USER_NAME + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Tạo bảng Products
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PRODUCT_NAME + " TEXT,"
                + COLUMN_PRODUCT_CATEGORY + " TEXT,"
                + COLUMN_PRODUCT_PRICE + " REAL," // REAL cho số thực
                + COLUMN_PRODUCT_STOCK + " INTEGER" + ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);

        // Tạo bảng Cart
        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CART_USER_EMAIL + " TEXT,"
                + COLUMN_CART_PRODUCT_ID + " INTEGER,"
                + COLUMN_CART_QUANTITY + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_CART_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_EMAIL + "),"
                + "FOREIGN KEY(" + COLUMN_CART_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COLUMN_PRODUCT_ID + ")" + ")";
        db.execSQL(CREATE_CART_TABLE);

        // Tạo bảng Orders
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_CUSTOMER_EMAIL + " TEXT,"
                + COLUMN_ORDER_DATE + " TEXT,"
                + COLUMN_ORDER_TOTAL + " REAL,"
                + COLUMN_ORDER_STATUS + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_ORDER_CUSTOMER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_EMAIL + ")" + ")";
        db.execSQL(CREATE_ORDERS_TABLE);

        Log.i(TAG, "Database tables created");
        // Chèn dữ liệu mẫu khi tạo DB lần đầu
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu tồn tại khi nâng cấp DB (đơn giản nhất)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // Tạo lại bảng
        onCreate(db);
        Log.w(TAG, "Database upgraded from version " + oldVersion + " to " + newVersion);
    }

    // --- CRUD Operations ---

    // == Users ==
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_PASSWORD, user.getPassword()); // Nhớ mã hóa trong thực tế
        values.put(COLUMN_USER_ROLE, user.getRole());
        values.put(COLUMN_USER_NAME, user.getName());

        db.insert(TABLE_USERS, null, values);
        db.close();
        Log.i(TAG, "User added: " + user.getEmail());
    }

    public User getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_USER_EMAIL, COLUMN_USER_PASSWORD, COLUMN_USER_ROLE, COLUMN_USER_NAME},
                COLUMN_USER_EMAIL + "=? AND " + COLUMN_USER_PASSWORD + "=?",
                new String[]{email, password}, // So sánh mật khẩu trực tiếp (không an toàn)
                null, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ROLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME))
            );
            cursor.close();
            Log.i(TAG, "User found: " + email);
        } else {
            Log.w(TAG, "User not found or password mismatch: " + email);
        }
        db.close();
        return user;
    }

    // == Products ==
    public void addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, product.getName());
        values.put(COLUMN_PRODUCT_CATEGORY, product.getCategory());
        values.put(COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(COLUMN_PRODUCT_STOCK, product.getStockQuantity());

        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        Log.i(TAG, "Product added: " + product.getName());
    }

    public Product getProductById(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, COLUMN_PRODUCT_ID + "=?",
                new String[]{String.valueOf(productId)}, null, null, null);

        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            product = new Product(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CATEGORY)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_STOCK))
            );
            cursor.close();
        }
        db.close();
        return product;
    }


    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CATEGORY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_STOCK))
                );
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return productList;
    }

    public int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, product.getName());
        values.put(COLUMN_PRODUCT_CATEGORY, product.getCategory());
        values.put(COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(COLUMN_PRODUCT_STOCK, product.getStockQuantity());

        int rowsAffected = db.update(TABLE_PRODUCTS, values, COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(product.getId())});
        db.close();
        Log.i(TAG, "Product updated: ID " + product.getId() + ", Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    public void deleteProduct(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_PRODUCTS, COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)});
        db.close();
        Log.i(TAG, "Product deleted: ID " + productId + ", Rows deleted: " + rowsDeleted);
        // Cần xử lý thêm: xóa sản phẩm này khỏi giỏ hàng của mọi người? (tuỳ yêu cầu)
    }

    // == Cart ==
    // Lấy item trong giỏ hàng của user, kèm thông tin sản phẩm
    public List<CartItem> getCartItems(String userEmail) {
        List<CartItem> cartItemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query JOIN để lấy thông tin sản phẩm cùng lúc
        String query = "SELECT c." + COLUMN_CART_ID + ", c." + COLUMN_CART_USER_EMAIL + ", "
                + "c." + COLUMN_CART_PRODUCT_ID + ", c." + COLUMN_CART_QUANTITY + ", "
                + "p." + COLUMN_PRODUCT_NAME + ", p." + COLUMN_PRODUCT_PRICE
                + " FROM " + TABLE_CART + " c JOIN " + TABLE_PRODUCTS + " p"
                + " ON c." + COLUMN_CART_PRODUCT_ID + " = p." + COLUMN_PRODUCT_ID
                + " WHERE c." + COLUMN_CART_USER_EMAIL + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{userEmail});

        if (cursor.moveToFirst()) {
            do {
                CartItem cartItem = new CartItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_USER_EMAIL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_PRODUCT_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_QUANTITY))
                );
                // Thêm thông tin sản phẩm lấy được từ JOIN
                cartItem.setProductName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
                cartItem.setProductPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)));

                cartItemList.add(cartItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return cartItemList;
    }

    // Tìm xem sản phẩm đã có trong giỏ của user chưa
    private CartItem findCartItem(String userEmail, int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CART, null,
                COLUMN_CART_USER_EMAIL + "=? AND " + COLUMN_CART_PRODUCT_ID + "=?",
                new String[]{userEmail, String.valueOf(productId)},
                null, null, null);

        CartItem existingItem = null;
        if (cursor != null && cursor.moveToFirst()) {
            existingItem = new CartItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_USER_EMAIL)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_PRODUCT_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_QUANTITY))
            );
            cursor.close();
        }
        // không cần db.close() vì cursor.close() đã làm? --> nên close db rõ ràng
        db.close();
        return existingItem;
    }


    // Thêm sản phẩm vào giỏ hoặc cập nhật số lượng nếu đã tồn tại
    public void addOrUpdateCartItem(CartItem newItem) {
        CartItem existingItem = findCartItem(newItem.getUserEmail(), newItem.getProductId());
        SQLiteDatabase db = this.getWritableDatabase();

        if (existingItem != null) {
            // Sản phẩm đã có -> cập nhật số lượng
            int newQuantity = existingItem.getQuantity() + newItem.getQuantity(); // Cộng dồn số lượng
            ContentValues values = new ContentValues();
            values.put(COLUMN_CART_QUANTITY, newQuantity);
            db.update(TABLE_CART, values, COLUMN_CART_ID + " = ?",
                    new String[]{String.valueOf(existingItem.getId())});
            Log.i(TAG, "CartItem updated: ID " + existingItem.getId() + ", New Quantity: " + newQuantity);
        } else {
            // Sản phẩm chưa có -> thêm mới
            ContentValues values = new ContentValues();
            values.put(COLUMN_CART_USER_EMAIL, newItem.getUserEmail());
            values.put(COLUMN_CART_PRODUCT_ID, newItem.getProductId());
            values.put(COLUMN_CART_QUANTITY, newItem.getQuantity());
            db.insert(TABLE_CART, null, values);
            Log.i(TAG, "CartItem added: Product ID " + newItem.getProductId() + " for user " + newItem.getUserEmail());
        }
        db.close();
    }

    public int updateCartItemQuantity(int cartItemId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CART_QUANTITY, quantity);

        int rowsAffected = db.update(TABLE_CART, values, COLUMN_CART_ID + " = ?",
                new String[]{String.valueOf(cartItemId)});
        db.close();
        Log.i(TAG, "CartItem quantity updated: ID " + cartItemId + ", New Quantity: " + quantity);
        return rowsAffected;
    }

    public void removeCartItem(int cartItemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CART, COLUMN_CART_ID + " = ?",
                new String[]{String.valueOf(cartItemId)});
        db.close();
        Log.i(TAG, "CartItem removed: ID " + cartItemId + ", Rows deleted: " + rowsDeleted);
    }

    public void clearCart(String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CART, COLUMN_CART_USER_EMAIL + " = ?", new String[]{userEmail});
        db.close();
        Log.i(TAG, "Cart cleared for user: " + userEmail + ", Rows deleted: " + rowsDeleted);
    }

    // == Orders ==
    public long addOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_CUSTOMER_EMAIL, order.getCustomerEmail());
        values.put(COLUMN_ORDER_DATE, order.getOrderDate());
        values.put(COLUMN_ORDER_TOTAL, order.getTotalAmount());
        values.put(COLUMN_ORDER_STATUS, order.getStatus());

        long id = db.insert(TABLE_ORDERS, null, values);
        db.close();
        Log.i(TAG, "Order added: ID " + id + " for user " + order.getCustomerEmail());
        return id; // Trả về ID của đơn hàng mới tạo
    }

    public List<Order> getAllOrders() {
        List<Order> orderList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ORDERS + " ORDER BY " + COLUMN_ORDER_DATE + " DESC"; // Sắp xếp mới nhất lên đầu
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_CUSTOMER_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_TOTAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_STATUS))
                );
                orderList.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return orderList;
    }

    // Hàm tiện ích lấy ngày giờ hiện tại dạng text
    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    // Hàm chèn dữ liệu mẫu
    private void insertInitialData(SQLiteDatabase db) {
        Log.i(TAG, "Inserting initial data...");
        try {
            // Dữ liệu Users
            addUserInitial(db, "staff@store.com", "123456", "Nhân viên", "Nguyễn Văn A");
            addUserInitial(db, "user1@email.com", "abc123", "Khách hàng", "Trần Thị B");
            addUserInitial(db, "user2@email.com", "qwerty", "Khách hàng", "Lê Minh C");

            // Dữ liệu Products
            addProductInitial(db, "Mì Hảo Hảo", "Thực phẩm", 5000, 100);
            addProductInitial(db, "Sữa Vinamilk", "Đồ uống", 25000, 50);
            addProductInitial(db, "Bánh Oreo", "Bánh kẹo", 20000, 30);
            addProductInitial(db, "Coca-Cola lon", "Đồ uống", 10000, 80);

            // Dữ liệu Orders (Ví dụ) - Cart để người dùng tự thêm
            addOrderInitial(db, "user1@email.com", "2025-04-10", 55000, "Đã thanh toán");
            addOrderInitial(db, "user2@email.com", "2025-04-11", 50000, "Chưa thanh toán");

            Log.i(TAG, "Initial data inserted successfully.");

        } catch (Exception e) {
            Log.e(TAG, "Error inserting initial data", e);
        }
    }

    // Hàm hỗ trợ chèn User (dùng trong insertInitialData)
    private void addUserInitial(SQLiteDatabase db, String email, String password, String role, String name) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_ROLE, role);
        values.put(COLUMN_USER_NAME, name);
        db.insert(TABLE_USERS, null, values);
    }

    // Hàm hỗ trợ chèn Product (dùng trong insertInitialData)
    private void addProductInitial(SQLiteDatabase db, String name, String category, double price, int stock) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_CATEGORY, category);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_STOCK, stock);
        db.insert(TABLE_PRODUCTS, null, values);
    }

    // Hàm hỗ trợ chèn Order (dùng trong insertInitialData)
    private void addOrderInitial(SQLiteDatabase db, String email, String date, double total, String status) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_CUSTOMER_EMAIL, email);
        values.put(COLUMN_ORDER_DATE, date); // Sử dụng ngày cung cấp
        values.put(COLUMN_ORDER_TOTAL, total);
        values.put(COLUMN_ORDER_STATUS, status);
        db.insert(TABLE_ORDERS, null, values);
    }
}