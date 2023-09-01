package com.libra.fragments.cafefragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.libra.Const;
import com.libra.R;
import com.libra.adapters.BaseRecyclerAdapter;
import com.libra.entity.Cafe;
import com.libra.entity.CafeRoom;
import com.libra.entity.User;
import com.libra.firebase.FBHelper;
import com.libra.firebase.GeoQueryListener;
import com.libra.fragments.BaseFragment;
import com.libra.fragments.GeoLocationFragment;
import com.libra.fragments.OnChangeFragments;
import com.libra.support.Tools;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.util.GeoUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhatAroundFragment extends GeoLocationFragment {

  public static final double MIN_DISTANCE_FILTER = 0.1; // In meters
  public static final String DECIMAL_FORMAT = "##.#";

  private GeoQuery mGeoQueryCafeRoom;
  private DatabaseReference mFirebaseCafeRoom;
  private DatabaseReference mFirebaseCafes;
  private ProgressBar mPb;
  private ConstraintLayout vEmptyView;
  private OnChangeFragments toolbarParent = null;
  private WhatAroundAdapter mAdapter;
  private Map<String, GeoLocation> geoMaps = new HashMap<>();
  private Map<String, Cafe> cafesMap = new HashMap<String, Cafe>();

  public static BaseFragment newInstance() {
    Bundle args = new Bundle();
    BaseFragment fragment = new WhatAroundFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnChangeFragments) {
      toolbarParent = (OnChangeFragments) context;
    } else {
      toolbarParent = null;
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    toolbarParent = null;
  }

  @Override public void locationDenied() {

  }

  @Override protected int getLayoutId() {
    return R.layout.fragment_what_around;
  }

  @Override protected int getTitleToolbar() {
    return R.string.main_what_around;
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mPb = view.findViewById(R.id.pb);
    vEmptyView = view.findViewById(R.id.vEmptyView);

    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setHasFixedSize(true);

    mAdapter = new WhatAroundAdapter();
    recyclerView.setAdapter(mAdapter);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mPb.setVisibility(View.VISIBLE);

    mGeoQueryCafeRoom = FBHelper.getLocationsCafe()
        .queryAtLocation(getCurrentGeoLocation(), Const.RADIUS_AROUND_CAFE_ROOMS_IN_KM);
    mFirebaseCafeRoom = FBHelper.getCafeRooms();
    mFirebaseCafes = FBHelper.getCafe();

    Location curLocation = getCurrentLocation();
    if (curLocation != null) {
      locationRequestingSuccess();
    }
  }

  @Override public void onDestroyView() {
    removeListenerGeoCafeRoom();
    removeListenerCafeRoom();
    super.onDestroyView();
  }

  private void readGeoCafeRoom() {
    mGeoQueryCafeRoom.setCenter(getCurrentGeoLocation());
    mGeoQueryCafeRoom.setRadius(Const.RADIUS_AROUND_CAFE_ROOMS_IN_KM);
    mGeoQueryCafeRoom.addGeoQueryEventListener(mGeoListener);
  }

  private void removeListenerGeoCafeRoom() {
    mGeoQueryCafeRoom.removeAllListeners();
  }

  private void readCafesList() {
    mFirebaseCafes.addListenerForSingleValueEvent(mCafesListener);
  }

  private void readCafeRoom() {
    mFirebaseCafeRoom.addValueEventListener(mCafeRoomListener);
  }

  private void removeListenerCafeRoom() {
    mFirebaseCafeRoom.removeEventListener(mCafeRoomListener);
  }

  private GeoQueryListener mGeoListener = new GeoQueryListener() {

    @Override public void onKeyEntered(String key, final GeoLocation location) {
      LOG.i("onKeyEntered key -" + key + "location - " + location.toString());
      geoMaps.put(key, location);
    }

    @Override public void onGeoQueryReady() {
      LOG.i("onGeoQueryReady");
      removeListenerGeoCafeRoom();
      //readCafeRoom();
      readCafesList();
    }

    @Override public void onGeoQueryError(DatabaseError error) {
      super.onGeoQueryError(error);
      LOG.e(error.getMessage());
      removeListenerGeoCafeRoom();
      mPb.setVisibility(View.GONE);
    }
  };

  private ValueEventListener mCafesListener = new ValueEventListener() {

    @Override public void onDataChange(DataSnapshot dataSnapshot) {
      for (DataSnapshot entry : dataSnapshot.getChildren()) {
        String key = entry.getKey();
        Cafe cafe = entry.getValue(Cafe.class);
        if (cafe != null) {
          cafesMap.put(key, cafe);
        }
      }
      readCafeRoom();
    }

    @Override public void onCancelled(DatabaseError databaseError) {
      LOG.e(new Exception(databaseError.getMessage()));
      mPb.setVisibility(View.GONE);
    }
  };

  private ValueEventListener mCafeRoomListener = new ValueEventListener() {

    @Override public void onDataChange(DataSnapshot dataSnapshot) {
      LOG.i("onDataChange");

      List<CafeRoom> list = new ArrayList<>();

      for (Map.Entry<String, GeoLocation> entry : geoMaps.entrySet()) {
        CafeRoom room = new CafeRoom();
        String cafeName = dataSnapshot.child(entry.getKey()).child(Cafe.NAME).getValue().toString();
        String cafeAddress = cafesMap.get(entry.getKey()).getAddress();
        room.setName(cafeName);
        room.setAddress(cafeAddress);

        DataSnapshot users = dataSnapshot.child(entry.getKey()).child(FBHelper.USERS);
        for(DataSnapshot snapshot: users.getChildren()){
          room.getUsers().add(snapshot.getValue(User.class));
        }
        int countFemale = 0;
        for (DataSnapshot shot : users.getChildren()) {
          User user = shot.getValue(User.class);
          if (user.isFemale()) {
            countFemale++;
          }
        }
        room.setCountFemale(countFemale);
        if (getCurrentGeoLocation() == null) {
          room.setDistance(0);
        } else {
          room.setDistance(GeoUtils.distance(getCurrentGeoLocation(), entry.getValue()));
        }
        list.add(room);
      }
      if (list.size() == 0) {
        mAdapter.clearData();
        showEmptyView();
      } else {
        mAdapter.setData(list);
        hideEmptyView();
      }
      mPb.setVisibility(View.GONE);
    }

    @Override public void onCancelled(DatabaseError firebaseError) {
      LOG.e(new Exception(firebaseError.getMessage()));
      mPb.setVisibility(View.GONE);
      //TODO handler UI
    }
  };

  @Override public void locationRequestingSuccess() {
    readGeoCafeRoom();
  }

  private void showEmptyView() {
    if (toolbarParent != null) {
      toolbarParent.setTitleToolbar("");
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        toolbarParent.getToolbar().setElevation(0);
      }
    }

    if (vEmptyView.getVisibility() != View.VISIBLE) {
      vEmptyView.setAlpha(0f);
      vEmptyView.setVisibility(View.VISIBLE);
      vEmptyView.animate().alpha(1f).start();
    }
  }

  private void hideEmptyView() {
    if (toolbarParent != null) {
      toolbarParent.setTitleToolbar(getTitleToolbar());
    }

    if (vEmptyView.getVisibility() == View.VISIBLE) {
      vEmptyView.animate().alpha(0).withEndAction(new Runnable() {
        @Override public void run() {
          vEmptyView.setVisibility(View.GONE);
        }
      }).start();
    }
  }

  private static class WhatAroundAdapter extends BaseRecyclerAdapter<CafeRoom> {

    private static int TYPE_FULL = 1;
    private static int TYPE_EMPTY = 2;

    @Override public int getItemViewType(int position) {
      if (getItem(position).isEmpty()) {
        return TYPE_EMPTY;
      } else {
        return TYPE_FULL;
      }
    }

    @Override public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      int id = viewType == TYPE_EMPTY ? R.layout.item_cafe_empty : R.layout.item_what_around;
      View v = LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
      return new Holder(v);
    }

    @Override public void setData(List<CafeRoom> data) {
      Collections.sort(data, new Comparator<CafeRoom>() {
        @Override public int compare(CafeRoom lhs, CafeRoom rhs) {
          return Double.compare(lhs.getDistance(), rhs.getDistance());
        }
      });
      mData.clear();
      mData.addAll(data);
      notifyDataSetChanged();
    }

    @Override public void onBindViewHolder(BaseHolder holder, int position) {
      if (holder.getItemViewType() == TYPE_EMPTY) {
        return;
      }
      Holder h = (Holder) holder;
      CafeRoom room = getItem(position);
      h.name.setText(room.getName());
      h.tvAddress.setText(room.getAddress());
      if (room.getCountUser() == 0) {
        h.emptyView.setVisibility(View.VISIBLE);
      } else {
        h.emptyView.setVisibility(View.GONE);
        h.proggressContainer.setWeightSum(room.getCountUser());
        setWeight(h.ladiesProggress, room.getCountFemale());
        setWeight(h.mensProggress, room.getCountMale());
        h.ladiesValue.setText(room.getCountFemale() + "");
        h.mansValue.setText(room.getCountMale() + "");
      }

      h.distance.setText("");

      double km = (room.getDistance() / 1000);
      double miles = Tools.convertKmToMiles(km);
      DecimalFormat df = new DecimalFormat(DECIMAL_FORMAT);

      StringBuilder sb = new StringBuilder();
      if (miles > MIN_DISTANCE_FILTER) {
        String milesStr = df.format(miles);
        sb.append(h.getContext().getString(R.string.distance_miles, milesStr));
      }
      if (km > MIN_DISTANCE_FILTER) {
        String kmStr = df.format(km);
        sb.append(" ").append(h.getContext().getString(R.string.distance_km, kmStr));
      }
      if (sb.length() == 0) {
        sb.append(h.getContext().getString(R.string.distance_empty));
      }
      h.distance.setText(sb.toString());
    }

    private void setWeight(View view, int weight) {
      LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
      params.weight = weight;
      view.setLayoutParams(params);
    }

    private static class Holder extends BaseHolder {
      private ProgressBar pb;
      private TextView name;
      private TextView distance;
      private TextView tvAddress;
      private LinearLayout proggressContainer;
      private FrameLayout ladiesProggress;
      private FrameLayout mensProggress;
      private TextView ladiesValue;
      private TextView mansValue;
      private FrameLayout emptyView;

      private final double iconScale = 0.65;

      public Holder(View v) {
        super(v);
        name = (TextView) v.findViewById(R.id.name);
        distance = (TextView) v.findViewById(R.id.distance);
        tvAddress = v.findViewById(R.id.tvAddress);
        proggressContainer = v.findViewById(R.id.progressContainer);
        ladiesProggress = v.findViewById(R.id.progressLadies);
        mensProggress = v.findViewById(R.id.progressMans);
        ladiesValue = v.findViewById(R.id.ladiesValue);
        mansValue = v.findViewById(R.id.mansValue);
        emptyView = v.findViewById(R.id.emptyView);

        if (ladiesValue != null) setAdaptiveIcon(ladiesValue, R.drawable.ic_ladies);
        if (mansValue != null) setAdaptiveIcon(mansValue, R.drawable.ic_men);
      }

      private void setAdaptiveIcon(final TextView tv, final int iconId) {
        tv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            Drawable img = getContext().getResources().getDrawable(iconId);
            int newWidth = (int) ((img.getIntrinsicWidth() * tv.getMeasuredHeight()
                / img.getIntrinsicHeight()) * iconScale);
            int newHeight = (int) (tv.getMeasuredHeight() * iconScale);
            img.setBounds(0, 0, newWidth, newHeight);
            tv.setCompoundDrawables(null, null, img, null);
            tv.getViewTreeObserver().removeOnPreDrawListener(this);
            return true;
          }
        });
      }
    }
  }
}
