package com.example.fms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fms.R;
import com.example.fms.model.Item;

import java.util.List;

public class ItemDialogAdapter extends ArrayAdapter<Item> {

    private LayoutInflater inflater;

    public ItemDialogAdapter(@NonNull Context context, @NonNull List<Item> items) {
        super(context, 0, items);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        ViewHolder holder;

        if (itemView == null) {
            itemView = inflater.inflate(R.layout.item_dialog_list_item, parent, false);
            holder = new ViewHolder();
            holder.itemIcon = itemView.findViewById(R.id.item_icon);
            holder.itemName = itemView.findViewById(R.id.item_name);
            holder.itemDescription = itemView.findViewById(R.id.item_description);
            holder.itemQuantity = itemView.findViewById(R.id.item_quantity);
            itemView.setTag(holder);
        } else {
            holder = (ViewHolder) itemView.getTag();
        }

        // Lấy item tại vị trí position
        Item item = getItem(position);
        if (item != null) {
            // Set dữ liệu cho các view
            setItemIcon(holder.itemIcon, item.getType().getIconResource());
            holder.itemName.setText(item.getName());
            holder.itemDescription.setText(item.getDescription());
            holder.itemQuantity.setText("x" + item.getQuantity());
            
            // Thêm hiệu ứng pulse cho item
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.item_pulse);
            animation.setStartOffset(position * 100); // Delay dựa vào vị trí
            itemView.startAnimation(animation);
        }

        return itemView;
    }

    private void setItemIcon(ImageView imageView, String iconResource) {
        int resourceId;
        try {
            resourceId = R.drawable.class.getField(iconResource).getInt(null);
            imageView.setImageResource(resourceId);
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.item_1); // Default image
        }
    }

    private static class ViewHolder {
        ImageView itemIcon;
        TextView itemName;
        TextView itemDescription;
        TextView itemQuantity;
    }
} 