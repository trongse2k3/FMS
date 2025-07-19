package com.example.fms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class DashboardFragment extends Fragment {
    public DashboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ImageView imgStadium = view.findViewById(R.id.football_field_image);
        imgStadium.setOnClickListener(v -> {
            // Sử dụng Navigation Component để chuyển sang ShopFragment
            Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_shopFragment);
        });
        return view;
    }
}
