package com.libra.fragments.chatfragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.libra.R;
import com.libra.adapters.BaseRecyclerAdapter;
import com.libra.entity.User;
import com.libra.firebase.FBChildListener;
import com.libra.firebase.FBHelper;
import com.libra.firebase.FirebaseEvents;
import com.libra.fragments.BaseDialogFragment;
import com.libra.fragments.BaseFragment;
import com.libra.fragments.BaseHostedFragment;
import com.libra.services.AppGcmListenerService;
import com.libra.services.GeofenceIntentService;
import com.libra.support.NotificationMsgHelper;
import com.libra.support.PrefHelper;
import com.libra.tools.MessagingEndpoint;
import com.libra.ui.home.CafeActivity;
import com.libra.view.DividerItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ListChatFragment extends BaseHostedFragment implements View.OnClickListener {

    private static final String USER_ID = "userId";
    private static final String CAFE_ID = "cafeId";

    private UserAdapter mAdapter;
    private ViewSwitcher mSwitcher;
    private String mUserId;
    private String mCafeName;
    private String mCafeId;
    private User mCurUser;
    private DatabaseReference mFirebaseNameCafeRoom;
    private DatabaseReference mFirebaseUser;
    private DatabaseReference mFirebaseFriends;
    private DatabaseReference mFirebaseUsersForNotify;
    private DatabaseReference mFirebasePushTokensForRushHours;
    private int mCountUser;
    private ProgressBar mPb;

    private ImageView ivAvatar;
    CardView closeBtn;
    View userimageView;


    public static BaseFragment newInstance(String userId, String cafeId) {
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        args.putString(CAFE_ID, cafeId);
        BaseFragment fragment = new ListChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getArguments().getString(USER_ID);
        mCafeId = getArguments().getString(CAFE_ID);
    }

    @Override
    protected boolean isLogged() {
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        changeToolbarElevatin(0);
        mPb = (ProgressBar) view.findViewById(R.id.pb);
        mSwitcher = (ViewSwitcher) view.findViewById(R.id.view_switcher);

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setHasFixedSize(true);

        ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        closeBtn =  view.findViewById(R.id.close_btn);
        userimageView =  view.findViewById(R.id.user_image_view);
        userimageView.setVisibility(View.GONE);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userimageView.setVisibility(View.GONE);
            }
        });

        DividerItem divider = new DividerItem(getContext());
        divider.setColor(R.color.divider_gray_1);
        divider.setHeight(R.dimen.height_divider);
//        rv.addItemDecoration(divider);

        if (mAdapter == null) {
            mAdapter = new UserAdapter();
            mAdapter.setOnClickListener(this);
        } else {
            changeCountUser(mAdapter.getItemCount());
        }
        rv.setAdapter(mAdapter);
        if (mCafeName != null) setTitleToolbar(mCafeName);
    }

    private void updateUserVisitTime() {
        if (mCurUser != null) {
            mCurUser.setLoginedTime(System.currentTimeMillis() / 1000);
            FBHelper.getUserById(mCafeId, mUserId).setValue(mCurUser);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFirebaseNameCafeRoom = FBHelper.getNameCafeRoom(mCafeId);
        readTitleCafeFromFirebase();

        mFirebaseUser = FBHelper.getUserById(mCafeId, mUserId);
        readUserFromFirebase();

        mFirebaseFriends = FBHelper.getUsersFromCafeRoom(mCafeId);
        mFirebaseUsersForNotify = FBHelper.getUsersFromCafeRoom(mCafeId);
        mFirebasePushTokensForRushHours = FBHelper.getCafeRushHours(mCafeId);

        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mNewMessageReceiver, new IntentFilter(AppGcmListenerService.NEW_MESSAGE));
    }

    @Override
    public void onDestroyView() {
        removeTitleListener();
        removeUserListener();
        removeFriendsListener();
        removeUsersNotifyListener();
        removeListenerPushTokens();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mNewMessageReceiver);
        super.onDestroyView();
    }

    private BroadcastReceiver mNewMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String friendId = intent.getStringExtra(AppGcmListenerService.USER_ID);
            if (friendId != null) mAdapter.changePositionUserById(friendId);
        }
    };

    private void readPushTokensForRushHours() {
        mFirebasePushTokensForRushHours.addListenerForSingleValueEvent(mListenerPushTokens);
    }

    private void removeListenerPushTokens() {
        mFirebasePushTokensForRushHours.removeEventListener(mListenerPushTokens);
    }

    private ValueEventListener mListenerPushTokens = new ValueEventListener() {

        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<String> list = new ArrayList<>();
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                User user = data.getValue(User.class);
                if (user.getGender() != mCurUser.getGender()) {
                    list.add(user.getPushToken());
                }
            }

            if (!list.isEmpty()) {
                //new NewUserTask(getContext(), list, mCafeId, mCafeName, mCountUser).execute(); //Old way to send push
                new MessagingEndpoint().notifyNewUser(list, mCafeId, mCafeName, mCountUser);//New way to send push
            }
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            LOG.e(new Exception(firebaseError.getMessage()));
            //TODO handler UI
        }
    };

    private void readUsersForNotify() {
        if (PrefHelper.isNotifyNewUser(getContext())) {
            LOG.i("not send notify new user");
            return;
        } else {
            LOG.i("sending notify new user");
        }
        mFirebaseUsersForNotify.addListenerForSingleValueEvent(mUserNotifyListener);
    }

    private void removeUsersNotifyListener() {
        mFirebaseUsersForNotify.removeEventListener(mUserNotifyListener);
    }

    private ValueEventListener mUserNotifyListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mCountUser = 0;
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                User user = data.getValue(User.class);
                if (mCurUser.getGender() == user.getGender()) {
                    mCountUser++;
                }
            }

            if (mCountUser > 4) {
                readPushTokensForRushHours();
            }
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            LOG.e(new Exception(firebaseError.getMessage()));
            //TODO handler UI
        }
    };

    private void readTitleCafeFromFirebase() {
        mFirebaseNameCafeRoom.addListenerForSingleValueEvent(mNameListener);
    }

    private void removeTitleListener() {
        mFirebaseNameCafeRoom.removeEventListener(mNameListener);
    }

    private ValueEventListener mNameListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mCafeName = dataSnapshot.getValue(String.class);
            setTitleToolbar(mCafeName);
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            LOG.e(new Exception(firebaseError.getMessage()));
            //TODO handler UI
        }
    };

    private void readUserFromFirebase() {
        mFirebaseUser.addListenerForSingleValueEvent(mUserListener);
    }

    private void removeUserListener() {
        mFirebaseUser.removeEventListener(mUserListener);
    }

    private ValueEventListener mUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mCurUser = dataSnapshot.getValue(User.class);
            if (mCurUser == null) {
                mCurUser = PrefHelper.getUser(getActivity());
            }

            if (mCurUser == null) {
                startActivity(CafeActivity.launchIntent(getActivity()));
                getActivity().finish();
                return;
            }

            mCurUser.setId(mUserId);
            mCurUser.setCafeId(mCafeId);
            if (getContext() != null) {
                updateUserVisitTime();
                readFriendsFromFirebase();
                readUsersForNotify();
            }
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            LOG.e(new Exception(firebaseError.getMessage()));
            //TODO handler UI
        }
    };

    private void readFriendsFromFirebase() {
        mFirebaseFriends.addChildEventListener(mFriendsListener);
    }

    private void removeFriendsListener() {
        mFirebaseFriends.removeEventListener(mFriendsListener);
    }

    private FBChildListener mFriendsListener = new FBChildListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (!TextUtils.equals(dataSnapshot.getKey(), mUserId)) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getGender() != mCurUser.getGender()) {
                    user.setId(dataSnapshot.getKey());
                    mAdapter.addUser(user);
                }
            }
            changeCountUser(mAdapter.getItemCount());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mAdapter.removeUserById(dataSnapshot.getKey());
            changeCountUser(mAdapter.getItemCount());
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            LOG.e(new Exception(firebaseError.getMessage()));
            //TODO handler UI
        }
    };

    private void changeCountUser(int count) {
        if (mPb.getVisibility() == View.VISIBLE) {
            mPb.setVisibility(View.GONE);
            mSwitcher.setVisibility(View.VISIBLE);
        }
        if (count == 0) {
            changeToolbarElevatin(0);
            mSwitcher.setDisplayedChild(0);
        }
//        else if (count == 909) {
//            changeToolbarElevatin((int) getResources().getDimension(R.dimen.space_4));
//            mSwitcher.setDisplayedChild(2);
//        }
        else {
            changeToolbarElevatin((int) getResources().getDimension(R.dimen.space_4));
            mSwitcher.setDisplayedChild(1);
        }
    }

    private void changeToolbarElevatin(int elevation) {
        ViewCompat.setElevation(getToolbar(), elevation);
    }

    @Override
    protected String getTitleToolbarStr() {
        return mCafeName != null ? mCafeName : " ";
    }

    @Override
    public boolean handlerClickButtonToolbar(@IdRes int id) {
        switch (id) {
            //case R.id.btn_toolbar_right:
            //  if (mCurUser != null) {
            //    mFragments.changeFragment(RushHoursFragment.newInstance(mCurUser), true);
            //  } else {
            //    Toast.makeText(getContext(), R.string.wait, Toast.LENGTH_SHORT).show();
            //  }
            //  return true;

            case R.id.btn_toolbar_left:
                LogoutDialog.newInstance().show(getChildFragmentManager(), null);
                return true;
        }
        return super.handlerClickButtonToolbar(id);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_list_chat;
    }

    @Override
    public void onClick(View v) {
        final User friend = (User) v.getTag();
        if (friend == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.container_avatar:
                Picasso.get()
                        .load(mCurUser.getImageUrl())
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.ic_placeholder_add_photo)
                        .error(R.drawable.ic_placeholder_add_photo)
                        .into(ivAvatar);
               userimageView.setVisibility(View.VISIBLE);
//                mFragments.changeFragment(UserFragment.newInstance(friend), true);
                break;
            case R.id.btn_chat:
                if (getActivity() != null)
                    FirebaseAnalytics.getInstance(getActivity()).logEvent(FirebaseEvents.CLICKED_ON_CHAT, null);
                mFragments.changeFragment(ChatFragment.newInstance(mCurUser, friend.getId()), true);
                break;
        }
    }

    public static class LogoutDialog extends BaseDialogFragment implements View.OnClickListener {

        public static BaseDialogFragment newInstance() {
            Bundle args = new Bundle();
            BaseDialogFragment fragment = new LogoutDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getContext())
                    .inflate(R.layout.dialog_logout, (ViewGroup) getView());
            v.findViewById(R.id.iv_close).setOnClickListener(this);
            v.findViewById(R.id.btn_negative).setOnClickListener(this);
            v.findViewById(R.id.btn_positive).setOnClickListener(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ThemeDialog_Transparent)
                    .setView(v);
            return builder.create();
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_close:
                case R.id.btn_negative:
                    dismiss();
                    break;
                case R.id.btn_positive:
                    getContext().startService(GeofenceIntentService.getManualLogoutIntent(getContext()));
                    break;
            }
        }
    }

    private static class UserAdapter extends BaseRecyclerAdapter<User> {

        private static final int TYPE_EVEN = 1;
        private static final int TYPE_ODD = 2;
        private static final int TYPE_EMPTY = 3;

        public void addUser(User user) {
            if (getUserById(user.getId()) != null) return;
            mData.add(user);
            notifyDataSetChanged();
        }

        public void removeUserById(String id) {
            User user = getUserById(id);
            if (user != null) {
                mData.remove(user);
                notifyDataSetChanged();
            }
        }

        public void changePositionUserById(String userId) {
            User user = getUserById(userId);
            removeUserById(userId);
            mData.add(0, user);
            notifyDataSetChanged();
        }

        private User getUserById(String id) {
            for (User user : mData) {
                if (TextUtils.equals(user.getId(), id)) {
                    return user;
                }
            }
            return null;
        }

        @Override
        public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int idRes;
            if (viewType == TYPE_EVEN) {
//                idRes = R.layout.item_chat_even;
                idRes = R.layout.item_chat_odd;
            } else if (viewType == TYPE_ODD) {
                idRes = R.layout.item_chat_odd;
            } else {
                idRes = R.layout.item_chat_empty;
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(idRes, parent, false);
            return new Holder(v, getOnClickListener());
        }

        @Override
        public int getItemViewType(int position) {
            User user = getItem(position);
            if (user == null || user.isEmpty()) {
                return TYPE_EMPTY;
            } else if (position % 2 == 0) {
                return TYPE_EVEN;
            } else {
                return TYPE_ODD;
            }
        }

        @Override
        public void onBindViewHolder(BaseHolder holder, int position) {
            if (holder.getItemViewType() == TYPE_EMPTY) {
                return;
            }
            final Holder h = (Holder) holder;
            User user = getItem(position);

            h.btnChat.setTag(user);
            h.containerAvatar.setTag(user);
            h.tvName.setText(user.getName());
            Picasso.get()
                    .load(user.getImageUrl())
                    .fit()
                    .centerCrop()

                    .placeholder(R.drawable.ic_placeholder_add_photo)
                    .error(R.drawable.ic_placeholder_add_photo)
                    .into(h.ivAvatar);

            int countNewMsg = NotificationMsgHelper.getCountMessageByUserId(h.getContext(), user.getId());
            if (countNewMsg == 0) {
                h.tvNewMsg.setVisibility(View.INVISIBLE);
            } else {
                h.tvNewMsg.setVisibility(View.VISIBLE);
                h.tvNewMsg.setText(String.valueOf(countNewMsg));
            }
        }

        private static class Holder extends BaseHolder {

            private ImageView ivAvatar;
            private TextView tvName;
            private View containerAvatar;
            private Button btnChat;
            private TextView tvNewMsg;

            public Holder(View v, View.OnClickListener listener) {
                super(v);
                ivAvatar = (ImageView) v.findViewById(R.id.iv_avatar);
                containerAvatar = v.findViewById(R.id.container_avatar);
                if (containerAvatar != null) {
                    containerAvatar.setOnClickListener(listener);
                }
                tvName = (TextView) v.findViewById(R.id.tv_name);
                btnChat = (Button) v.findViewById(R.id.btn_chat);
                if (btnChat != null) {
                    btnChat.setOnClickListener(listener);
                }
                tvNewMsg = (TextView) v.findViewById(R.id.tv_new_msg);
            }
        }
    }

    //    private static class NewUserTask extends AsyncTask<Void, Void, Void> {
    //        private final Logger LOG = Logger.getLogger(getClass().getSimpleName(), true);
    //
    //        private WeakReference<Context> mRefContext;
    //        private ArrayList<String> mListPushTokens;
    //        private String mCafeId;
    //        private String mCafeName;
    //        private int mUserCount;
    //        private String mAppName;
    //
    //        public NewUserTask(Context context, ArrayList<String> listPushToken, String cafeId, String cafeName, int userCount) {
    //            this.mRefContext = new WeakReference<>(context);
    //            this.mListPushTokens = listPushToken;
    //            this.mCafeId = cafeId;
    //            this.mCafeName = cafeName;
    //            this.mUserCount = userCount;
    //            this.mAppName = context.getString(R.string.app_name);
    //        }
    //
    //        @Override
    //        protected Void doInBackground(Void... params) {
    //            Messaging.Builder builder = new Messaging.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
    //                    .setApplicationName(mAppName)
    //                    .setRootUrl(BuildConfig.ENDPOINT_NOTIFY);
    //            if (TextUtils.equals(BuildConfig.FLAVOR, "devLocal")) {
    //                builder.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
    //                    @Override
    //                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
    //                            throws IOException {
    //                        abstractGoogleClientRequest.setDisableGZipContent(true);
    //                    }
    //                });
    //            }
    //
    //            Messaging service = builder.build();
    //            try {
    //                Messaging.MessagingEndpoint.NotifyNewUser notifyNewUser = service.messagingEndpoint().notifyNewUser(new Gson().toJson(mListPushTokens), mCafeId, mCafeName, mUserCount);
    //                notifyNewUser.execute();
    //                Context context = mRefContext.get();
    //                if (context != null) {
    //                    LOG.i("send notify new user is completed");
    //                    PrefHelper.setNotifyNewUser(context, true);
    //                }
    //            } catch (IOException e) {
    //                LOG.e(e);
    //            }
    //            return null;
    //        }
    //
    //        @Override
    //        protected void onPostExecute(Void aVoid) {
    //        }
    //    }
}
