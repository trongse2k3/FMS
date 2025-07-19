package com.example.fms;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.firebase.ItemManager;
import com.example.fms.firebase.UserManager;
import com.example.fms.model.Item;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopFragment extends Fragment {
    private RecyclerView rvShopItems;
    private ShopItemAdapter shopItemAdapter;
    private TextView tvUserMoney;
    private ItemManager itemManager;
    private UserManager userManager;
    private FirebaseUser currentUser;
    private int currentMoney;
    private Map<Item.ItemType, Integer> userItemQuantities = new HashMap<>();
    private OnMoneyUpdateListener moneyUpdateListener;

    // Interface để thông báo khi số tiền thay đổi
    public interface OnMoneyUpdateListener {
        void onMoneyUpdated(int newMoney);
    }

    public ShopFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Kiểm tra xem Activity có triển khai interface không
        if (context instanceof OnMoneyUpdateListener) {
            moneyUpdateListener = (OnMoneyUpdateListener) context;
        } else {
            // Nếu không triển khai, ghi log hoặc ném exception
            // Log.w("ShopFragment", "Activity không triển khai OnMoneyUpdateListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        rvShopItems = view.findViewById(R.id.rv_shop_items);
        tvUserMoney = view.findViewById(R.id.tv_user_money);
        rvShopItems.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        itemManager = new ItemManager();
        userManager = new UserManager();
        currentUser = userManager.getCurrentUser();
        shopItemAdapter = new ShopItemAdapter(getAllShopItems(), userItemQuantities, this::onBuyItemClicked);
        rvShopItems.setAdapter(shopItemAdapter);
        loadUserDataAndItems();
        return view;
    }

    // Lấy danh sách tất cả item shop (dạng tĩnh)
    private List<Item> getAllShopItems() {
        List<Item> items = new ArrayList<>();
        for (Item.ItemType type : Item.ItemType.values()) {
            items.add(new Item(type));
        }
        return items;
    }

    // Load tiền và số lượng item user sở hữu
    private void loadUserDataAndItems() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        userManager.getUserData(userId, new UserManager.UserDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                if (userData.containsKey("money")) {
                    Object moneyObj = userData.get("money");
                    if (moneyObj instanceof Long) currentMoney = ((Long) moneyObj).intValue();
                    else if (moneyObj instanceof Integer) currentMoney = (Integer) moneyObj;
                    else currentMoney = 2000;
                } else {
                    currentMoney = 2000;
                }
                updateMoneyDisplay();
                loadUserItems();
            }
            @Override
            public void onFailure(Exception e) {
                currentMoney = 2000;
                updateMoneyDisplay();
                loadUserItems();
            }
        });
    }
    
    private void updateMoneyDisplay() {
        if (tvUserMoney != null) {
            tvUserMoney.setText("Số dư: " + currentMoney + "$");
        }
    }

    private void loadUserItems() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        itemManager.getUserItems(userId).addOnSuccessListener(items -> {
            userItemQuantities.clear();
            // Tính tổng số lượng theo loại item
            for (Item item : items) {
                Item.ItemType type = item.getType();
                int currentQty = userItemQuantities.getOrDefault(type, 0);
                userItemQuantities.put(type, currentQty + item.getQuantity());
            }
            shopItemAdapter.setUserItemQuantities(userItemQuantities);
        });
    }

    // Xử lý khi ấn nút Mua
    private void onBuyItemClicked(Item.ItemType itemType) {
        if (currentUser == null) return;
        int price = itemType.getPrice();
        if (currentMoney < price) {
            Toast.makeText(getContext(), "Không đủ tiền để mua vật phẩm này!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Trừ tiền user
        int newMoney = currentMoney - price;
        userManager.updateMoney(currentUser.getUid(), newMoney, new UserManager.UserDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                currentMoney = newMoney;
                updateMoneyDisplay();
                
                // Thông báo cho MainActivity cập nhật số tiền
                if (moneyUpdateListener != null) {
                    moneyUpdateListener.onMoneyUpdated(newMoney);
                }
                
                // Thêm/cộng dồn item cho user
                itemManager.addItemToUser(currentUser.getUid(), itemType, 1).addOnSuccessListener(success -> {
                    if (success) {
                        // Cập nhật số lượng item trên UI
                        int newQty = userItemQuantities.getOrDefault(itemType, 0) + 1;
                        userItemQuantities.put(itemType, newQty);
                        shopItemAdapter.setUserItemQuantities(userItemQuantities);
                        Toast.makeText(getContext(), "Mua vật phẩm thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Thêm hiệu ứng cho TextView hiển thị tiền
                        if (tvUserMoney != null) {
                            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.item_pulse);
                            tvUserMoney.startAnimation(animation);
                        }
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi thêm vật phẩm!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Lỗi khi cập nhật tiền!", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 