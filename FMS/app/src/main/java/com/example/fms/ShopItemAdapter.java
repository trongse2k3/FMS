package com.example.fms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.ItemViewHolder> {
    private List<Item> itemList;
    private Map<Item.ItemType, Integer> userItemQuantities;
    private OnBuyClickListener buyClickListener;

    public interface OnBuyClickListener {
        void onBuy(Item.ItemType itemType);
    }

    public ShopItemAdapter(List<Item> itemList, Map<Item.ItemType, Integer> userItemQuantities, OnBuyClickListener buyClickListener) {
        this.itemList = itemList;
        this.userItemQuantities = userItemQuantities != null ? userItemQuantities : new HashMap<>();
        this.buyClickListener = buyClickListener;
    }

    public void setUserItemQuantities(Map<Item.ItemType, Integer> userItemQuantities) {
        this.userItemQuantities = userItemQuantities != null ? userItemQuantities : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        Item.ItemType type = item.getType();
        holder.tvName.setText(type.getName());
        holder.tvDesc.setText(type.getDescription());
        holder.tvPrice.setText(type.getPrice() + "$" );
        int qty = userItemQuantities.getOrDefault(type, 0);
        holder.tvQuantity.setText("Sở hữu: " + qty);
        // Set icon
        int iconResId = getIconResId(holder.imgIcon, type.getIconResource());
        if (iconResId != 0) holder.imgIcon.setImageResource(iconResId);
        else holder.imgIcon.setImageResource(R.drawable.ic_list_placeholder);
        holder.btnBuy.setOnClickListener(v -> {
            if (buyClickListener != null) buyClickListener.onBuy(type);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvName, tvDesc, tvPrice, tvQuantity;
        Button btnBuy;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_item_icon);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvDesc = itemView.findViewById(R.id.tv_item_desc);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_item_quantity);
            btnBuy = itemView.findViewById(R.id.btn_buy_item);
        }
    }

    // Hàm lấy resource icon từ tên (nếu có)
    private int getIconResId(View view, String iconName) {
        if (iconName == null) return 0;
        try {
            return view.getContext().getResources().getIdentifier(iconName, "drawable", view.getContext().getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }
} 