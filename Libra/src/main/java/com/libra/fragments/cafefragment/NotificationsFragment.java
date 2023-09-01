package com.libra.fragments.cafefragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.libra.R;
import com.libra.adapters.BaseRecyclerAdapter;
import com.libra.entity.Cafe;
import com.libra.firebase.FBHelper;
import com.libra.fragments.BaseFragment;
import com.libra.fragments.BaseHostedFragment;
import com.libra.support.PrefHelper;
import com.libra.view.DividerItem;
import com.libra.view.FlexDividerItemDecoration;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends BaseHostedFragment implements View.OnClickListener {

  private NotificationCafeAdapter mAdapter;
  private ViewSwitcher mSwitcher;
  private DatabaseReference mFirebaseCafe;

  public static BaseFragment newInstance() {
    Bundle args = new Bundle();
    BaseFragment fragment = new NotificationsFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override protected int getLayoutId() {
    return R.layout.fragment_notification;
  }

  @Override protected int getTitleToolbar() {
    return R.string.main_notifications;
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mSwitcher = (ViewSwitcher) view.findViewById(R.id.view_switcher);

    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setHasFixedSize(true);

    DividerItem divider = new DividerItem(getContext());
    divider.setColor(R.color.divider_gray_2);
    divider.setHeight(R.dimen.height_divider);
    Drawable divider1 = ContextCompat.getDrawable(getContext(), R.drawable.shape_gray_divider);
    FlexDividerItemDecoration decor = new FlexDividerItemDecoration(divider1, false, true);
    recyclerView.addItemDecoration(decor);

    mAdapter = new NotificationCafeAdapter();
    mAdapter.setOnClickListener(this);
    recyclerView.setAdapter(mAdapter);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mFirebaseCafe = FBHelper.getCafe();
    readAllCafe();
  }

  @Override public void onDestroyView() {
    removeCafeListener();
    super.onDestroyView();
  }

  private void readAllCafe() {
    mFirebaseCafe.addListenerForSingleValueEvent(mCafeListener);
  }

  private void removeCafeListener() {
    mFirebaseCafe.removeEventListener(mCafeListener);
  }

  private ValueEventListener mCafeListener = new ValueEventListener() {
    @Override public void onDataChange(DataSnapshot dataSnapshot) {
      if (getContext() == null) {
        return;
      }
      List<Cafe> lis = new ArrayList<>();
      List<String> listIdCafe = PrefHelper.getArrayCafeIdRushHours(getContext());

      if (listIdCafe != null && !listIdCafe.isEmpty()) {
        for (DataSnapshot data : dataSnapshot.getChildren()) {
          String cafeId = data.getKey();
          if (listIdCafe.contains(cafeId)) {
            Cafe cafe = data.getValue(Cafe.class);
            cafe.setId(cafeId);
            lis.add(cafe);
          }
        }
      }
      mAdapter.setData(lis);
      mSwitcher.setDisplayedChild(1);
    }

    @Override public void onCancelled(DatabaseError firebaseError) {
      LOG.e(new Exception(firebaseError.getMessage()));
      //TODO handler UI
    }
  };

  @Override public void onClick(View v) {
    final Cafe cafe = (Cafe) v.getTag();
    if (cafe != null) {
      FBHelper.getCafeRushHoursByAppId(cafe.getId(), PrefHelper.getAppId(getContext()))
          .removeValue(new DatabaseReference.CompletionListener() {
            @Override public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
              if (firebaseError == null) {
                PrefHelper.removeCafeIdForRushHours(getContext(), cafe.getId());
                mAdapter.removeItem(cafe);
              } else {
                LOG.e(new Exception(firebaseError.toString()));
                //TODO handler ui
              }
            }
          });
    }
  }

  private static class NotificationCafeAdapter extends BaseRecyclerAdapter<Cafe> {

    private static int TYPE_FULL = 1;
    private static int TYPE_EMPTY = 2;

    @Override public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      int id = viewType == TYPE_EMPTY ? R.layout.item_notification_empty
          : R.layout.item_notification_cafe;
      View v = LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
      return new Holder(v, getOnClickListener());
    }

    @Override public int getItemViewType(int position) {
      if (getItem(position).isEmpty()) {
        return TYPE_EMPTY;
      } else {
        return TYPE_FULL;
      }
    }

    @Override public void setData(List<Cafe> data) {
      if (data.isEmpty()) {
        data.add(new Cafe());
      }
      super.setData(data);
    }

    public void removeItem(Cafe cafe) {
      mData.remove(cafe);
      notifyDataSetChanged();
    }

    @Override public void onBindViewHolder(BaseHolder holder, int position) {
      if (holder.getItemViewType() == TYPE_EMPTY) {
        return;
      }
      Holder h = (Holder) holder;
      Cafe cafe = getItem(position);
      h.off.setTag(cafe);
      h.name.setText(cafe.getName());
      h.address.setText(cafe.getAddress());
    }

    private static class Holder extends BaseHolder {
      private TextView name;
      private TextView address;
      private Button off;

      public Holder(View v, View.OnClickListener listener) {
        super(v);
        name = (TextView) v.findViewById(R.id.name);
        address = (TextView) v.findViewById(R.id.address);
        off = (Button) v.findViewById(R.id.off);
        if (off != null) {
          off.setOnClickListener(listener);
        }
      }
    }
  }
}
