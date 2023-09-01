package com.android.libramanage.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.libramanage.R;
import com.android.libramanage.adapters.CafeListAdapter;
import com.android.libramanage.entity.Cafe;
import com.android.libramanage.entity.StateClass;
import com.android.libramanage.entity.User;
import com.android.libramanage.entity.dateModel;
import com.android.libramanage.firebase.FBHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CafeStatFragment extends BaseFragment {


    RecyclerView recyclerView;
    CafeListAdapter cafeListAdapter;
    List<StateClass> restaurantList = new ArrayList<>();
    String currentDate;


    public static CafeStatFragment newInstance() {
        CafeStatFragment fragment = new CafeStatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    }

    @Override
    protected int getTitleId() {
        return R.string.cafe;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_cafe_stat;
    }

    @Override
    protected int getMenuId() {
        return 0;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.list_state);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cafeListAdapter = new CafeListAdapter(getContext(), restaurantList);

        recyclerView.setAdapter(cafeListAdapter);

        FBHelper.getBase().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Cafe> cafeList = new ArrayList<>();
                List<StateClass> stateModelList = new ArrayList<>();
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if (FBHelper.CAFE.equals(snapshot.getKey())) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                Cafe cafe = data.getValue(Cafe.class);
                                cafe.setId(data.getKey());
                                cafeList.add(cafe);
                            }
                        }

                        if (FBHelper.CAFE_ROOMS_STAT.equals(snapshot.getKey())) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                StateClass stateClass = new StateClass();
                                stateClass.setCafeId(data.getKey());
                                List<dateModel> dateModelList = new ArrayList<>();
                                for (DataSnapshot dataSnapshot1 : data.getChildren()) {
                                    dateModel datemodel = new dateModel();
                                    datemodel.setDate(dataSnapshot1.getKey());
                                    List<User> users = new ArrayList<>();
                                    for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("users").getChildren()) {
                                        User user = new User();
                                        user.setGender(((Long) dataSnapshot2.child("gender").getValue()).intValue());
                                        user.setLoginedTime(((Long) dataSnapshot2.child("loginedTime").getValue()));
                                        user.setImageUrl(((String) dataSnapshot2.child("imageUrl").getValue()));
                                        user.setName(((String) dataSnapshot2.child("name").getValue()));
                                        user.setUserId(dataSnapshot2.getKey());
                                        users.add(user);
                                    }
                                    datemodel.setUserList(users);
                                    dateModelList.add(datemodel);
                                }
                                stateClass.setDateModelList(dateModelList);
                                stateModelList.add(stateClass);
                            }
                        }

                        for (StateClass stateClass : stateModelList) {
                            for (Cafe cafe : cafeList) {
                                if (stateClass.getCafeId().equalsIgnoreCase(cafe.getId())) {
                                    stateClass.setCafe(cafe);
                                }
                            }
                        }
                        restaurantList.clear();
                        restaurantList.addAll(stateModelList);
                        cafeListAdapter.notifyDataSetChanged();
                        Log.e("TAGS", "state list " + stateModelList.size() + " cafe " + cafeList.size() + " firs " + ((stateModelList.size() > 0) ? stateModelList.get(0).getCafe().getPlacesType() : ""));

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }


}