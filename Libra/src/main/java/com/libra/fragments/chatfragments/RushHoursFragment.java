package com.libra.fragments.chatfragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.libra.R;
import com.libra.entity.Cafe;
import com.libra.entity.User;
import com.libra.firebase.FBHelper;
import com.libra.fragments.BaseDialogFragment;
import com.libra.fragments.BaseFragment;
import com.libra.fragments.BaseHostedFragment;
import com.libra.fragments.dialogs.MsgDialogsFragment;
import com.libra.support.PrefHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RushHoursFragment extends BaseHostedFragment implements View.OnClickListener {

  private static final String USER = "user";
  public static final long DURATION = 300;

  private ImageView ivImage;
  private TextView mTitle;
  private TextView mAddress;
  private User mCurrentUser;
  private DatabaseReference mFirebaseCafe;
  private DatabaseReference mFirebaseRushHours;
  private Cafe mCurrentCafe;
  private ProgressBar mPb;
  private TextView tvLimited;
  private View llAlreadyRegistered;
  private View clContent;

  public static BaseFragment newInstance(User user) {
    Bundle args = new Bundle();
    args.putParcelable(USER, user);
    BaseFragment fragment = new RushHoursFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCurrentUser = getArguments().getParcelable(USER);
  }

  @Override public void onResume() {
    super.onResume();
    setTitleToolbar(getString(R.string.title_rush_hours));
  }

  @Override protected int getLayoutId() {
    return R.layout.fragment_rush_hours;
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mPb = (ProgressBar) view.findViewById(R.id.pb);
    tvLimited = (TextView) view.findViewById(R.id.tv_limited);

    llAlreadyRegistered = view.findViewById(R.id.ll_already_registered);
    view.findViewById(R.id.btn_cancel).setOnClickListener(this);

    clContent = view.findViewById(R.id.cl_content);
    ivImage = (ImageView) view.findViewById(R.id.iv_image_cafe);
    mTitle = view.findViewById(R.id.tv_title);
    mAddress = view.findViewById(R.id.tv_address);
    view.findViewById(R.id.btn_active).setOnClickListener(this);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    List<String> listSubscribeCafeId = PrefHelper.getArrayCafeIdRushHours(getContext());
    if (listSubscribeCafeId == null
        || listSubscribeCafeId.size() < 3
        || listSubscribeCafeId.contains(mCurrentUser.getCafeId())) {
      mFirebaseCafe = FBHelper.getCafeById(mCurrentUser.getCafeId());
      readCafeFromFirebase();
      mFirebaseRushHours = FBHelper.getCafeRushHoursByAppId(mCurrentUser.getCafeId(), PrefHelper.getAppId(getContext()));
    } else {
      mPb.setVisibility(View.GONE);
      //tvLimited.setVisibility(View.VISIBLE);
      showAnim(tvLimited);
    }
  }

  @Override public void onDestroyView() {
    removeRushHoursListener();
    removeCafeListener();
    super.onDestroyView();
  }

  private void readCafeFromFirebase() {
    mFirebaseCafe.addListenerForSingleValueEvent(mCafeListener);
  }

  private void removeCafeListener() {
    if (mFirebaseCafe != null) {
      mFirebaseCafe.removeEventListener(mCafeListener);
    }
  }

  private ValueEventListener mCafeListener = new ValueEventListener() {
    @Override public void onDataChange(DataSnapshot dataSnapshot) {
      mCurrentCafe = dataSnapshot.getValue(Cafe.class);
      mCurrentCafe.setId(dataSnapshot.getKey());
      bindData(mCurrentCafe);
      readRushHoursFromFirebase();
    }

    @Override public void onCancelled(DatabaseError firebaseError) {
      LOG.e(new Exception(firebaseError.getMessage()));
      //TODO handler UI
    }
  };

  private void readRushHoursFromFirebase() {
    mFirebaseRushHours.addListenerForSingleValueEvent(mRushHoursListener);
  }

  private void removeRushHoursListener() {
    if (mFirebaseRushHours != null) {
      mFirebaseRushHours.removeEventListener(mRushHoursListener);
    }
  }

  private ValueEventListener mRushHoursListener = new ValueEventListener() {
    @Override public void onDataChange(DataSnapshot dataSnapshot) {
      User user = dataSnapshot.getValue(User.class);
      changeState(user != null);
    }

    @Override public void onCancelled(DatabaseError firebaseError) {
      LOG.e(new Exception(firebaseError.getMessage()));
      //TODO handler UI
    }
  };

  private void bindData(Cafe cafe) {
    Picasso.get()
        .load(cafe.getImageUrl())
        .fit()
        .centerCrop()
        .placeholder(R.drawable.ic_restaurant_menu_24dp)
        .error(R.drawable.ic_restaurant_menu_24dp)
        .into(ivImage);
    mTitle.setText(cafe.getName());
    mAddress.setText(cafe.getAddress());
  }

  private void changeState(boolean isSubscribe) {
    mPb.setVisibility(View.GONE);

    if (isSubscribe) {
      //llAlreadyRegistered.setVisibility(View.VISIBLE);
      showAnim(llAlreadyRegistered);
      hideAnim(clContent);
      //clContent.setVisibility(View.GONE);
    } else {
      showAnim(clContent);
      hideAnim(llAlreadyRegistered);
      //llAlreadyRegistered.setVisibility(View.GONE);
      //clContent.setVisibility(View.VISIBLE);
    }
  }

  @Override protected int getTitleToolbar() {
    return 0;
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_active:
        showPd();
        mFirebaseRushHours.setValue(mCurrentUser, new DatabaseReference.CompletionListener() {
          @Override
          public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
            dismissPd();
            if (firebaseError == null) {
              //TODO change when will impl normal server
              PrefHelper.addCafeIdForRushHours(getContext(), mCurrentCafe.getId());
              //changeState(true);
              BaseDialogFragment dialog = MsgDialogsFragment.newInstance(getString(R.string.successfully_registered));
              dialog.show(getActivity().getSupportFragmentManager(), null);
              getActivity().onBackPressed();
            } else {
              LOG.e(new Exception(firebaseError.getMessage()));
              //TODO handler UI
            }
          }
        });
        break;

      case R.id.btn_cancel:
        showPd();
        mFirebaseRushHours.removeValue(new DatabaseReference.CompletionListener() {
          @Override
          public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
            dismissPd();
            if (firebaseError == null) {
              //TODO change when will impl normal server
              PrefHelper.setLastTimeRushHoursNotifyBuCafeId(getContext(), mCurrentCafe.getId(), 0);
              PrefHelper.removeCafeIdForRushHours(getContext(), mCurrentCafe.getId());
              changeState(false);
            } else {
              LOG.e(new Exception(firebaseError.getMessage()));
              //TODO handler UI
            }
          }
        });
        break;
    }
  }

  private void showAnim(final View view) {
    view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
      @Override public boolean onPreDraw() {
        view.getViewTreeObserver().removeOnPreDrawListener(this);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(DURATION)
            .setInterpolator(new AccelerateInterpolator())
            .start();
        return false;
      }
    });
  }

  private void hideAnim(final View view) {
    view.animate()
        .alpha(0f)
        .setDuration(DURATION)
        .setInterpolator(new AccelerateInterpolator())
        .withEndAction(new Runnable() {
          @Override public void run() {
            view.setVisibility(View.GONE);
          }
        })
        .start();
  }
}
