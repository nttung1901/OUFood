package com.tuantung.oufood.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tuantung.oufood.Activity.AddressListActivity;
import com.tuantung.oufood.Activity.CartActivity;
import com.tuantung.oufood.Activity.SaleListActivity;
import com.tuantung.oufood.Activity.SearchActivity;
import com.tuantung.oufood.Adapter.CategoryAdapter;
import com.tuantung.oufood.Adapter.FoodListAdapter;
import com.tuantung.oufood.Adapter.SaleListAdapter;
import com.tuantung.oufood.Class.Category;
import com.tuantung.oufood.Class.Food;
import com.tuantung.oufood.R;
import com.tuantung.oufood.common.Common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MenuFragment extends Fragment {
    private final int FINE_PERMISSION_CODE =1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextView textViewAddress;

    private RecyclerView recyclerView_bestSeller, recyclerView_categories, recyclerView_all_food;
    private ProgressBar mProgressBarCategory;
    private ProgressBar mProgressBarBestFood;
    private TextView mButtonFlashSale;
    private ImageView imageView_cart;
    private TextView searchView;
    private ImageView imageViewAddress;


    // Biến để lưu trữ các ValueEventListener và DatabaseReference
    private ValueEventListener categoriesListener;
    private ValueEventListener bestSellerListener;
    private ValueEventListener allFoodListener;
    private DatabaseReference categoriesRef;
    private DatabaseReference bestSellerRef;
    private DatabaseReference allFoodRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        textViewAddress = view.findViewById(R.id.textView_address);
        imageViewAddress = view.findViewById(R.id.imageView_address);
        imageView_cart = view.findViewById(R.id.imageView_cart);
        mButtonFlashSale = view.findViewById(R.id.button_flashSale);
        mProgressBarCategory = view.findViewById(R.id.progressBar_category);
        recyclerView_categories = view.findViewById(R.id.recycler_categories);
        recyclerView_bestSeller = view.findViewById(R.id.recycler_best_sale);
        mProgressBarBestFood = view.findViewById(R.id.progressBar_bestFood);
        recyclerView_all_food = view.findViewById(R.id.recyclerView_all_food);
        searchView = view.findViewById(R.id.searchView);

        //Get current location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLastLocation();

        //move to addressListActivity
        imageViewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), AddressListActivity.class));
            }
        });

        //move to cartActivity
        imageView_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), CartActivity.class));
            }
        });

        //button flashSale
        mButtonFlashSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), SaleListActivity.class));
            }
        });

        //recycler categories
        setupRecyclerCategories();

        //recycler best seller
        setupRecyclerViewBestSeller();

        //recycler all food
        setupRecyclerViewAllFood();

        //move to searchActivity
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), SearchActivity.class));
            }
        });

        return view;
    }

    private void getLastLocation(){
        if(ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    Geocoder geocoder = new Geocoder(requireActivity());
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Address address1 = addresses.get(0);
                    textViewAddress.setText(address1.getAddressLine(0));
                }
            }
        });


    }

    private void setupRecyclerCategories() {
        mProgressBarCategory.setVisibility(View.VISIBLE);
        categoriesRef = Common.FIREBASE_DATABASE.getReference(Common.REF_CATEGORIES); // Lưu trữ DatabaseReference
        categoriesListener = new ValueEventListener() { // Lưu trữ ValueEventListener
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Category> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    category.setId(dataSnapshot.getKey());
                    list.add(category);
                }

                if (!list.isEmpty()) {
                    recyclerView_categories.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
                    RecyclerView.Adapter adapter = new CategoryAdapter(list);
                    recyclerView_categories.setAdapter(adapter);
                }
                mProgressBarCategory.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        };
        categoriesRef.addValueEventListener(categoriesListener); // Sử dụng addValueEventListener
    }

    private void setupRecyclerViewBestSeller() {
        mProgressBarBestFood.setVisibility(View.VISIBLE);
        bestSellerRef = Common.FIREBASE_DATABASE.getReference(Common.REF_FOODS); // Lưu trữ DatabaseReference
        bestSellerListener = new ValueEventListener() { // Lưu trữ ValueEventListener
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Food> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Food food = dataSnapshot.getValue(Food.class);
                    food.setId(dataSnapshot.getKey());
                    list.add(food);
                }

                for (int i = list.size() - 1; i >= 0; i--) {
                    if (list.get(i).getDiscount().equals("0")) {
                        list.remove(i);
                    }
                }

                for (int i = list.size() - 1; i >= Common.TOP_BEST_SELLER; i--) {
                    list.remove(i);
                }

                if (!list.isEmpty()) {
                    recyclerView_bestSeller.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
                    RecyclerView.Adapter adapter = new SaleListAdapter(list);
                    recyclerView_bestSeller.setAdapter(adapter);
                }
                mProgressBarBestFood.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        };
        bestSellerRef.addValueEventListener(bestSellerListener); // Sử dụng addValueEventListener
    }

    private void setupRecyclerViewAllFood() {
        allFoodRef = Common.FIREBASE_DATABASE.getReference(Common.REF_FOODS); // Lưu trữ DatabaseReference
        allFoodListener = new ValueEventListener() { // Lưu trữ ValueEventListener
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Food> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Food food = dataSnapshot.getValue(Food.class);
                    food.setId(dataSnapshot.getKey());
                    list.add(food);
                }

                Collections.shuffle(list);

                for (int i = list.size() - 1; i >= 0; i--) {
                    if (!list.get(i).getDiscount().equals("0")) {
                        list.remove(i);
                    }
                }

                if (!list.isEmpty()) {
                    recyclerView_all_food.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
                    RecyclerView.Adapter adapter = new FoodListAdapter(list);
                    recyclerView_all_food.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        };
        allFoodRef.addValueEventListener(allFoodListener); // Sử dụng addValueEventListener
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy các sự kiện lắng nghe khi Fragment bị hủy
        if (categoriesRef != null && categoriesListener != null) {
            categoriesRef.removeEventListener(categoriesListener);
        }
        if (bestSellerRef != null && bestSellerListener != null) {
            bestSellerRef.removeEventListener(bestSellerListener);
        }
        if (allFoodRef != null && allFoodListener != null) {
            allFoodRef.removeEventListener(allFoodListener);
        }

    }
}