package com.android.libramanage.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.libramanage.R;
import com.android.libramanage.adapters.BaseRecyclerAdapter;
import com.android.libramanage.autoimport.AutoImportActivity;
import com.android.libramanage.dialogs.EditRadiusDialog;
import com.android.libramanage.entity.Cafe;
import com.android.libramanage.firebase.FBHelper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class CafeFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private CafeAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private DatabaseReference mFirebaseCafe;
    private DatabaseReference mFirebaseCafeRooms;
    private DatabaseReference mFirebaseLocations;
    private ArrayList<String> cafePlacesIds = new ArrayList<>();
    private FloatingActionButton fab;

    public static BaseFragment newInstance() {
        Bundle args = new Bundle();
        BaseFragment fragment = new CafeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getTitleId() {
        return R.string.cafe;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_cafe;
    }

    @Override
    protected int getMenuId() {
        return R.menu.menu_main_screen;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(this);
        fab.setVisibility(View.GONE);

        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);

        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setHasFixedSize(true);
        mAdapter = new CafeAdapter();
        mAdapter.setOnClickListener(this);
        recycler.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSwipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefresh.setRefreshing(true);
            }
        });
        mFirebaseCafe = FBHelper.getCafe();
        mFirebaseCafeRooms = FBHelper.getCafeRooms();
        mFirebaseLocations = FBHelper.getLocations();
        readCafeFromFirebase();
    }

    @Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AutoImportActivity.REQUEST_CODE) {
            readCafeFromFirebase();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        removeCafeListener();
        super.onDestroyView();
    }

    private void readCafeFromFirebase() {
        cafePlacesIds.clear();
        mFirebaseCafe.addListenerForSingleValueEvent(mCafeListener);
    }

    private void removeCafeListener() {
        mFirebaseCafe.removeEventListener(mCafeListener);
    }

    private ValueEventListener mCafeListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
            List<Cafe> list = new ArrayList<>();
            for (com.google.firebase.database.DataSnapshot data : snapshot.getChildren()) {
                Cafe cafe = data.getValue(Cafe.class);
                cafe.setId(data.getKey());
                list.add(cafe);
                if (cafe.getPlacesId() != null) cafePlacesIds.add(cafe.getPlacesId());
            }
            mAdapter.setData(list);
            mSwipeRefresh.setRefreshing(false);
            fab.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            LOG.e(new Exception(error.getMessage()));
            mSwipeRefresh.setRefreshing(false);
        }


    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add:
                showCafeAddOptionsDialog();
                //showFragment(CafeDetailsFragment.newInstance(""), true);
                //AutoImportActivity.launch(getContext(), cafePlacesIds);
                break;
            default:
                String cafeId = (String) v.getTag();
                if (!TextUtils.isEmpty(cafeId)) {
                    showFragment(CafeDetailsFragment.newInstance(cafeId), true);
                }
                break;
        }
    }

    private void showCafeAddOptionsDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
            .setCancelable(true)
            .setTitle("Choose cafe adding option")
            .setItems(new String[] {
                "Add cafe manually", "Auto import by location"
            }, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        showFragment(CafeDetailsFragment.newInstance(""), true);
                    } else if (i == 1) {
                        AutoImportActivity.launch4Result(CafeFragment.this, cafePlacesIds);
                    }
                }
            })
            .create();
        dialog.show();
    }

    @Override
    public void onRefresh() {
        readCafeFromFirebase();
    }

    @Override public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.edit_distance) {
            EditRadiusDialog.show(getFragmentManager());
            return true;
        }
        if (item.getItemId() == R.id.delete_all_cafes) {
            showDeleteAllWarningDialog();
            return true;
        }  if (item.getItemId() == R.id.all_cafe_stats) {
            showFragment(CafeStatFragment.newInstance(), true);
            return true;
        }
        return super.onMenuItemClick(item);
    }

    private void showDeleteAllWarningDialog() {
        new AlertDialog.Builder(getContext())
            .setTitle("WARNING!!!")
            .setMessage("By confirming this action you'll delete all cafes from database. This action can't be undone. Don't press \"Yes\" if you don't sure that you really need to do it.\n\nAre you really want to delete all cafes?")
            .setCancelable(true)
            .setNegativeButton("No", null)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                @Override public void onClick(DialogInterface dialogInterface, int i) {
                    mFirebaseCafe.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            onRefresh();
                        }

                    });
                    mFirebaseCafeRooms.removeValue();
                    mFirebaseLocations.removeValue();
                }
            })
            .show();
    }

    protected static class CafeAdapter extends BaseRecyclerAdapter<Cafe> {

        @Override
        public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cafe, parent, false);
            v.setOnClickListener(getOnClickListener());
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(BaseHolder holder, int position) {
            Holder h = (Holder) holder;
            Cafe cafe = getItem(position);
            h.itemView.setTag(cafe.getId());
            h.tvName.setText(cafe.getName());
            h.tvAddress.setText(cafe.getAddress());
            Picasso.get()
                    .load(cafe.getImageUrl())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_restaurant_menu_24dp)
                    .error(R.drawable.ic_restaurant_menu_24dp)
                    .into(h.ivPhoto);
        }

        public static class Holder extends BaseHolder {

            private TextView tvName;
            private TextView tvAddress;
            private ImageView ivPhoto;

            public Holder(View itemView) {
                super(itemView);
                tvAddress = (TextView) itemView.findViewById(R.id.tv_address);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            }
        }
    }
}
