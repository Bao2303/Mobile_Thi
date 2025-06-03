package vn.edu.tlu.cse.nthx.qlmh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import vn.edu.tlu.cse.nthx.qlmh.R;
import vn.edu.tlu.cse.nthx.qlmh.models.Product;

public class ProductStaffAdapter extends RecyclerView.Adapter<ProductStaffAdapter.ProductStaffViewHolder> {

    private List<Product> productList;
    private Context context;
    private ProductStaffListener listener;
    private Locale localeVN = new Locale("vi", "VN");
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

    // Interface để xử lý sự kiện click nút Sửa/Xóa
    public interface ProductStaffListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public ProductStaffAdapter(Context context, List<Product> productList, ProductStaffListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductStaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_staff, parent, false);
        return new ProductStaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductStaffViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textViewProductName.setText(product.getName());
        holder.textViewProductCategory.setText("Danh mục: " + product.getCategory());
        holder.textViewProductPrice.setText("Giá: " + currencyFormatter.format(product.getPrice()));
        holder.textViewProductStock.setText("Tồn kho: " + product.getStockQuantity());

        holder.buttonEditProduct.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(product);
            }
        });

        holder.buttonDeleteProduct.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(product);
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

    static class ProductStaffViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName, textViewProductCategory, textViewProductPrice, textViewProductStock;
        ImageButton buttonEditProduct, buttonDeleteProduct;

        public ProductStaffViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewStaffProductName);
            textViewProductCategory = itemView.findViewById(R.id.textViewStaffProductCategory);
            textViewProductPrice = itemView.findViewById(R.id.textViewStaffProductPrice);
            textViewProductStock = itemView.findViewById(R.id.textViewStaffProductStock);
            buttonEditProduct = itemView.findViewById(R.id.buttonEditProduct);
            buttonDeleteProduct = itemView.findViewById(R.id.buttonDeleteProduct);
        }
    }
}