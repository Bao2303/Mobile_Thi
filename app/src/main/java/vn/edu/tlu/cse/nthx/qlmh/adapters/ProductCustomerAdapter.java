package vn.edu.tlu.cse.nthx.qlmh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import vn.edu.tlu.cse.nthx.qlmh.R;
import vn.edu.tlu.cse.nthx.qlmh.models.Product;

public class ProductCustomerAdapter extends RecyclerView.Adapter<ProductCustomerAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;
    private OnAddToCartClickListener listener;
    private Locale localeVN = new Locale("vi", "VN");
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);


    // Interface để xử lý sự kiện click nút "Thêm"
    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public ProductCustomerAdapter(Context context, List<Product> productList, OnAddToCartClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_customer, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textViewProductName.setText(product.getName());
        holder.textViewProductPrice.setText("Giá: " + currencyFormatter.format(product.getPrice()));
        holder.textViewProductStock.setText("Tồn kho: " + product.getStockQuantity());

        // Xử lý click nút "Thêm vào giỏ"
        holder.buttonAddToCart.setOnClickListener(v -> {
            if (product.getStockQuantity() > 0) {
                if (listener != null) {
                    listener.onAddToCartClick(product);
                }
            } else {
                Toast.makeText(context, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Cập nhật dữ liệu cho adapter
    public void updateData(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật lại giao diện
    }

    // ViewHolder chứa các view của một item
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName, textViewProductPrice, textViewProductStock;
        Button buttonAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewProductStock = itemView.findViewById(R.id.textViewProductStock);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);
        }
    }
}