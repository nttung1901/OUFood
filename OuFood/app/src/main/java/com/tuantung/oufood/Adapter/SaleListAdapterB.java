package com.tuantung.oufood.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.tuantung.oufood.Activity.FoodDetailActivity;
import com.tuantung.oufood.Class.Food;
import com.tuantung.oufood.Class.Order;
import com.tuantung.oufood.Database.Database;
import com.tuantung.oufood.R;

import java.util.ArrayList;

public class SaleListAdapterB extends RecyclerView.Adapter<SaleListAdapterB.viewholder>{
    ArrayList<Food> items;
    Context context;

    public SaleListAdapterB(ArrayList<Food> items){
        this.items = items;
    }
    @NonNull
    @Override
    public SaleListAdapterB.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_sale_b,parent,false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleListAdapterB.viewholder holder, int position) {
        holder.textViewName.setText(items.get(position).getName());
        holder.textViewPrice.setText(items.get(position).getPrice());
        holder.textViewStar.setText(""+items.get(position).getCountStars());
        holder.textViewDiscount.setText(items.get(position).getDiscount()+"%\nOFF");

        Picasso.get()
                .load(items.get(position).getURL()) // URL của ảnh
                .fit() // Tự động điều chỉnh kích thước ảnh để khớp với ImageView
                .centerCrop() // Cắt và điều chỉnh trung tâm ảnh
                .into(holder.pic); // ImageView của bạn

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FoodDetailActivity.class);
            intent.putExtra("FoodId", items.get(position).getId());
            context.startActivity(intent);
        });

        holder.textViewPlus.setOnClickListener(v -> {
//            CuteToast.ct(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT, CuteToast.SUCCESS, true).show();
            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            Order order = new Order(items.get(position).getId(), items.get(position).getName(), items.get(position).getPrice(), "1", items.get(position).getDiscount(), "0");
            Database database1 = new Database(context);
            database1.addToCart(order);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewPrice;
        private TextView textViewStar;
        private TextView textViewDiscount;
        private TextView textViewPlus;

        private ImageView pic;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textView_name);
            textViewPrice = itemView.findViewById(R.id.textView_price);
            textViewStar = itemView.findViewById(R.id.textView_star);
            textViewDiscount = itemView.findViewById(R.id.textView_discount);
            textViewPlus = itemView.findViewById(R.id.textView_plus);

            pic = itemView.findViewById(R.id.pic);
        }
    }
}