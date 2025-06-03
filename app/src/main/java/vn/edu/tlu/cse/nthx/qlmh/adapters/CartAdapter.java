package vn.edu.tlu.cse.nthx.qlmh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import vn.edu.tlu.cse.nthx.qlmh.R;
import vn.edu.tlu.cse.nthx.qlmh.models.CartItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItemList;
    private Context context;
    private CartItemListener listener;
    private Locale localeVN = new Locale("vi", "VN");
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

    // Interface để xử lý các sự kiện trong giỏ hàng
    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, CartItemListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        holder.textViewCartItemName.setText(item.getProductName()); // Cần đảm bảo đã lấy được productName
        holder.textViewCartItemPrice.setText("Đơn giá: " + currencyFormatter.format(item.getProductPrice())); // Cần đảm bảo đã lấy được productPrice
        holder.textViewCartItemQuantity.setText(String.valueOf(item.getQuantity()));
        holder.textViewCartItemSubtotal.setText("Thành tiền: " + currencyFormatter.format(item.getTotalPrice()));

        holder.buttonIncreaseQuantity.setOnClickListener(v -> {
            if (listener != null) {
                // Cần kiểm tra tồn kho trước khi tăng (logic này nên ở Activity hoặc ViewModel)
                listener.onQuantityChanged(item, item.getQuantity() + 1);
            }
        });

        holder.buttonDecreaseQuantity.setOnClickListener(v -> {
            if (listener != null && item.getQuantity() > 1) {
                listener.onQuantityChanged(item, item.getQuantity() - 1);
            } else if (listener != null && item.getQuantity() == 1) {
                // Nếu giảm từ 1 -> 0 thì coi như xóa item
                listener.onItemRemoved(item);
            }
        });

        holder.buttonRemoveCartItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemoved(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    // Cập nhật dữ liệu cho adapter
    public void updateData(List<CartItem> newCartList) {
        this.cartItemList = newCartList;
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật lại giao diện
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCartItemName, textViewCartItemPrice, textViewCartItemQuantity, textViewCartItemSubtotal;
        Button buttonDecreaseQuantity, buttonIncreaseQuantity;
        ImageButton buttonRemoveCartItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCartItemName = itemView.findViewById(R.id.textViewCartItemName);
            textViewCartItemPrice = itemView.findViewById(R.id.textViewCartItemPrice);
            textViewCartItemQuantity = itemView.findViewById(R.id.textViewCartItemQuantity);
            textViewCartItemSubtotal = itemView.findViewById(R.id.textViewCartItemSubtotal);
            buttonDecreaseQuantity = itemView.findViewById(R.id.buttonDecreaseQuantity);
            buttonIncreaseQuantity = itemView.findViewById(R.id.buttonIncreaseQuantity);
            buttonRemoveCartItem = itemView.findViewById(R.id.buttonRemoveCartItem);
        }
    }
}