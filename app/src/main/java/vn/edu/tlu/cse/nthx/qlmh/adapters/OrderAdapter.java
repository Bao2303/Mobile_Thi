package vn.edu.tlu.cse.nthx.qlmh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import vn.edu.tlu.cse.nthx.qlmh.R;
import vn.edu.tlu.cse.nthx.qlmh.models.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private Locale localeVN = new Locale("vi", "VN");
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

    // Có thể thêm Listener nếu cần tương tác với item (ví dụ: xem chi tiết đơn hàng)
    // public interface OrderListener { void onItemClick(Order order); }
    // private OrderListener listener;

    public OrderAdapter(Context context, List<Order> orderList /*, OrderListener listener*/) {
        this.context = context;
        this.orderList = orderList;
        // this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.textViewOrderId.setText("Mã ĐH: #" + order.getId());
        holder.textViewOrderCustomer.setText("Khách hàng: " + order.getCustomerEmail());
        holder.textViewOrderDate.setText("Ngày đặt: " + order.getOrderDate()); // Định dạng ngày nếu cần
        holder.textViewOrderTotal.setText("Tổng tiền: " + currencyFormatter.format(order.getTotalAmount()));
        holder.textViewOrderStatus.setText("Trạng thái: " + order.getStatus());

        // Bắt sự kiện click vào item nếu cần
        /*
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(order);
            }
        });
         */
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // Cập nhật dữ liệu cho adapter
    public void updateData(List<Order> newOrderList) {
        this.orderList = newOrderList;
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật lại giao diện
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewOrderId, textViewOrderCustomer, textViewOrderDate, textViewOrderTotal, textViewOrderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewOrderCustomer = itemView.findViewById(R.id.textViewOrderCustomer);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            textViewOrderTotal = itemView.findViewById(R.id.textViewOrderTotal);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
        }
    }
}