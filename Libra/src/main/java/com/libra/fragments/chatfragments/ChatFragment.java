package com.libra.fragments.chatfragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.libra.R;
import com.libra.adapters.BaseRecyclerAdapter;
import com.libra.entity.Message;
import com.libra.entity.User;
import com.libra.firebase.FBHelper;
import com.libra.fragments.BaseDialogFragment;
import com.libra.fragments.BaseFragment;
import com.libra.fragments.BaseHostedFragment;
import com.libra.support.NotificationMsgHelper;
import com.libra.support.Tools;
import com.libra.tools.MessagingEndpoint;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends BaseHostedFragment implements View.OnClickListener, ChangeCountMsg {

    private static final String USER = "user";
    private static final String FRIEND_ID = "friendId";
    private static final String DIALOG_WAS_SHOWN = "dialog_shown";
    private ChatAdapter mAdapter;
    private EditText mEdtMsg;
    private User mUser;
    private User mFriend;
    private LinearLayoutManager mLLManager;
    private Button mBtnSend;
    private String mFriendId;
    private DatabaseReference mFirebaseFriend;
    private DatabaseReference mFirebaseChat;
    private DatabaseReference mFirebaseHeart;
    private View llLockMsg, ivLockMsg1, ivLockMsg2, ivLockMsg3, ivLockMsg4, ivLockMsg5, ivLockMsg6, ivLockMsg7, ivLockMsg8, ivLockMsg9, ivLockMsg10;
    private InputMethodManager mImm;

    public static BaseFragment newInstance(User user, String friendId) {
        Bundle args = new Bundle();
        args.putParcelable(USER, user);
        args.putString(FRIEND_ID, friendId);
        BaseFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getParcelable(USER);
            mFriendId = getArguments().getString(FRIEND_ID);
        }
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getContext() != null)
            mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        mEdtMsg = view.findViewById(R.id.edt_msg);
        mBtnSend = view.findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);

        llLockMsg = view.findViewById(R.id.ll_lock_msg);
        ivLockMsg1 = view.findViewById(R.id.iv_lock_1);
        ivLockMsg2 = view.findViewById(R.id.iv_lock_2);
        ivLockMsg3 = view.findViewById(R.id.iv_lock_3);
        ivLockMsg4 = view.findViewById(R.id.iv_lock_4);
        ivLockMsg5 = view.findViewById(R.id.iv_lock_5);
        ivLockMsg6 = view.findViewById(R.id.iv_lock_6);
        ivLockMsg7 = view.findViewById(R.id.iv_lock_7);
        ivLockMsg8 = view.findViewById(R.id.iv_lock_8);
        ivLockMsg9 = view.findViewById(R.id.iv_lock_9);
        ivLockMsg10 = view.findViewById(R.id.iv_lock_10);

        RecyclerView rv = view.findViewById(R.id.recycler);
        mLLManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(mLLManager);

        mAdapter = new ChatAdapter(mUser);
        mAdapter.setChangeCountListener(this);
        rv.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFirebaseFriend = FBHelper.getUserById(mUser.getCafeId(), mFriendId);
        readFriendFromFirebase();
        mFirebaseChat = FBHelper.getChatMessagesById(generateChatId(mUser.getId(), mFriendId));
        mFirebaseHeart = FBHelper.getHeartChatById(generateChatId(mUser.getId(), mFriendId));
    }

    @Override
    public void onDestroyView() {
        removeFriendListener();
        removeChatListener();
        //removeHeartListener();
        super.onDestroyView();
    }

    //TODO Uncomment to return heart functionality
    //private void readHeartState() {
    //  mFirebaseHeart.addValueEventListener(mHeartListener);
    //}
    //
    //private void removeHeartListener() {
    //  mFirebaseHeart.removeEventListener(mHeartListener);
    //}

    //private ValueEventListener mHeartListener = new ValueEventListener() {
    //  @Override public void onDataChange(DataSnapshot dataSnapshot) {
    //    boolean userLike = false;
    //    try {
    //      userLike = dataSnapshot.child(mUser.getId()).getValue(boolean.class);
    //    } catch (NullPointerException e) {
    //      e.printStackTrace();
    //    }
    //    mUser.setLike(userLike);
    //    boolean friendLike = false;
    //    try {
    //      friendLike = dataSnapshot.child(mFriendId).getValue(boolean.class);
    //    } catch (NullPointerException e) {
    //      e.printStackTrace();
    //    }
    //    mFriend.setLike(friendLike);
    //    boolean dialogWasShown = false;
    //    try {
    //      dialogWasShown = dataSnapshot.child(DIALOG_WAS_SHOWN).getValue(boolean.class);
    //    } catch (NullPointerException e) {
    //      e.printStackTrace();
    //    }
    //
    //    if (getContext() == null) {
    //      return;
    //    }
    //
    //    Drawable drawable;
    //    if (mUser.isLike()) {
    //      drawable = Tools.getDrawableFromRes(getContext(), R.drawable.ic_favorite_24dp, R.color.chat_like);
    //    } else {
    //      drawable = Tools.getDrawableFromRes(getContext(), R.drawable.ic_favorite_24dp, R.color.color_accent);
    //    }
    //    getRightImageButton().setImageDrawable(drawable);
    //    getRightImageButton().setVisibility(View.VISIBLE);
    //
    //    if (userLike && friendLike && !dialogWasShown) {
    //      DiscountDialog.newInstance().show(getChildFragmentManager(), null);
    //      updateLikeData(true, true, true);
    //    }
    //  }
    //
    //  @Override public void onCancelled(DatabaseError firebaseError) {
    //    LOG.e(new Exception(firebaseError.getMessage()));
    //    //TODO handler UI
    //  }
    //};

    private void readFriendFromFirebase() {
        mFirebaseFriend.addListenerForSingleValueEvent(mFriendListener);
    }

    private void removeFriendListener() {
        mFirebaseFriend.removeEventListener(mFriendListener);
    }

    private ValueEventListener mFriendListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mFriend = dataSnapshot.getValue(User.class);
            if (mFriend != null) {
                mFriend.setId(dataSnapshot.getKey());
                mFriend.setCafeId(mUser.getCafeId());
                mAdapter.setFriend(mFriend);
                setTitleToolbar(mFriend.getName());
                readChatFromFirebase();
                //readHeartState();
            } else {
                LOG.e(new Exception("mFriend == null"));
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            LOG.e(new Exception(firebaseError.getMessage()));
            //TODO handler UI
        }
    };

    private void readChatFromFirebase() {
        mFirebaseChat.addValueEventListener(mChatListener);
    }

    private void removeChatListener() {
        mFirebaseChat.removeEventListener(mChatListener);
    }

    private ValueEventListener mChatListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            List<Message> messages = new ArrayList<>();
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                messages.add(data.getValue(Message.class));
            }

            mAdapter.setData(messages);
            if (!messages.isEmpty()) {
                mLLManager.scrollToPosition(messages.size() - 1);
            }
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            LOG.e(new Exception(firebaseError.getMessage()));
            //TODO handler UI
        }
    };

    private void enabledSendWidget(boolean enabled) {
        mEdtMsg.setEnabled(enabled);
        mBtnSend.setEnabled(enabled);
    }

    @Override
    protected String getTitleToolbarStr() {
        return " ";
    }

    @Override
    public void onStart() {
        super.onStart();
        NotificationMsgHelper.setUserIdCurrentChat(mFriendId);
        NotificationMsgHelper.cancelByUserId(getContext(), mFriendId);
    }

    @Override
    public void onStop() {
        NotificationMsgHelper.setUserIdCurrentChat("");
        super.onStop();
    }

    private String generateChatId(String userId, String friendId) {
        int sumUserId = Tools.convertStrInSumAscii(userId);
        int sumFiendId = Tools.convertStrInSumAscii(friendId);
        String id;
        if (sumUserId <= sumFiendId) {
            id = userId + friendId;
        } else {
            id = friendId + userId;
        }
        return id;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    public boolean handlerClickButtonToolbar(@IdRes int id) {
        if (id == R.id.btn_toolbar_right) {
            if (mFriend == null) {
                Toast.makeText(getContext(), R.string.wait, Toast.LENGTH_SHORT).show();
                return true;
            }

            if (mUser.isLike() && mFriend.isLike()) {
                DiscountDialog.newInstance().show(getChildFragmentManager(), null);
                return true;
            }

            if (!mUser.isLike() || !mFriend.isLike()) {
                new MessagingEndpoint().notifyHeartMessage(mFriend.getPushToken(), mUser.getId(), mUser
                        .getName());
            }

            updateLikeData(!mUser.isLike(), mFriend.isLike(), false);
            return true;
        }
        return super.handlerClickButtonToolbar(id);
    }

    private void updateLikeData(boolean userLike, boolean friendLike, boolean dialogShown) {
        Map<String, Boolean> mapHeart = new HashMap<>();
        mapHeart.put(mUser.getId(), userLike);
        mapHeart.put(mFriendId, friendLike);
        mapHeart.put(DIALOG_WAS_SHOWN, dialogShown);
        mFirebaseHeart.setValue(mapHeart);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                String msg = mEdtMsg.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    return;
                }
                sendMsg(msg);
                break;
        }
    }

    private void sendMsg(final String msg) {
        enabledSendWidget(false);

        Map<String, String> map = new HashMap<>();
        map.put(Message.USER_ID, mUser.getId());
        map.put(Message.MSG, msg);
        map.put(Message.USER_NAME, mUser.getName());
        mFirebaseChat.push().setValue(map, (firebaseError, firebase) -> {
            if (isDetached()) {
                return;
            }
            if (firebaseError == null) {
                //new NotifySendMsgTask().execute(mFriend.getPushToken(), mUser.getId(), mUser.getName(), msg, getString(R.string.app_name)); //Old way to send push
                // no need to send the push from here, it will be automatically send by the cloud function
                //new MessagingEndpoint().notifySendMessage(mFriend.getPushToken(), mUser.getId(), mUser.getName(), msg); //New way to send push
                mEdtMsg.setText("");
            } else {
                LOG.e(new Exception(firebaseError.getMessage()));
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChangeCountMsg(int currentCount) {
        int MAX_MESSAGE_COUNT = 10;

//        enabledSendWidget(currentCount < MAX_MESSAGE_COUNT);
        enabledSendWidget(true);

//        switch (currentCount) {
//            case 9:
//                ivLockMsg9.setVisibility(View.GONE);
//            case 8:
//                ivLockMsg8.setVisibility(View.GONE);
//            case 7:
//                ivLockMsg7.setVisibility(View.GONE);
//            case 6:
//                ivLockMsg6.setVisibility(View.GONE);
//            case 5:
//                ivLockMsg5.setVisibility(View.GONE);
//            case 4:
//                ivLockMsg4.setVisibility(View.GONE);
//            case 3:
//                ivLockMsg3.setVisibility(View.GONE);
//            case 2:
//                ivLockMsg2.setVisibility(View.GONE);
//            case 1:
//                ivLockMsg1.setVisibility(View.GONE);
//            case 0:
//                mEdtMsg.setHint("");
//                llLockMsg.setVisibility(View.GONE);
//                break;
//
//            default:
//                mEdtMsg.setHint(getString(R.string.max_count_msg, MAX_MESSAGE_COUNT));
//                ivLockMsg10.setVisibility(View.GONE);
//                llLockMsg.setVisibility(View.GONE);
//                break;
//        }
    }

    //    private static class NotifySendMsgTask extends AsyncTask<String, Void, Void> {
    //        //TODO handler response
    //        @Override
    //        protected Void doInBackground(String... params) {
    //            Messaging.Builder builder = new Messaging.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
    //                    .setApplicationName(params[4])
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
    //            Messaging service = builder.build();
    //            try {
    //                service.messagingEndpoint().notifySendMessage(params[0], params[1], params[2], params[3]).execute();
    //            } catch (IOException e) {
    //                e.printStackTrace();
    //            }
    //            return null;
    //        }
    //    }

    public static class DiscountDialog extends BaseDialogFragment implements View.OnClickListener {

        public static BaseDialogFragment newInstance() {
            Bundle args = new Bundle();
            BaseDialogFragment fragment = new DiscountDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getContext())
                    .inflate(R.layout.dialog_discount, (ViewGroup) getView());
            v.findViewById(R.id.iv_close).setOnClickListener(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ThemeDialog_Transparent)
                    .setView(v);
            return builder.create();
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_close) {
                dismiss();
            }
        }
    }

    public static class ChatAdapter extends BaseRecyclerAdapter<Message> {

        private static final int TYPE_MSG_MY = 1;
        private static final int TYPE_MSG_FRIEND = 2;

        private User mUser;
        private User mFriend;
        private ChangeCountMsg mImplChangeCount;

        public ChatAdapter(User user) {
            this.mUser = user;
        }

        public void setFriend(User friend) {
            this.mFriend = friend;
        }

        @Override
        public void setData(List<Message> data) {
            super.setData(data);
            int count = 0;
            for (Message message : data) {
                if (TextUtils.equals(mUser.getId(), message.getUserId())) {
                    count++;
                }
            }
            if (mImplChangeCount != null) {
                mImplChangeCount.onChangeCountMsg(count);
            }
        }

        @NotNull
        @Override
        public BaseHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            int id;
            if (viewType == TYPE_MSG_FRIEND) {
                id = R.layout.item_msg;
            } else {
                id = R.layout.item_msg_friend;
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
            return new Holder(v);
        }

        public void setChangeCountListener(ChangeCountMsg changeCountListener) {
            this.mImplChangeCount = changeCountListener;
        }

        @Override
        public int getItemViewType(int position) {
            Message msg = getItem(position);
            if (TextUtils.equals(msg.getUserId(), mUser.getId())) {
                return TYPE_MSG_MY;
            } else {
                return TYPE_MSG_FRIEND;
            }
        }

        @Override
        public void onBindViewHolder(@NotNull BaseHolder holder, int position) {
            Holder h = (Holder) holder;
            Message msg = getItem(position);
            h.tvMsg.setText(msg.getMsg());

            String avatarUrl;
            if (TextUtils.equals(mUser.getId(), msg.getUserId())) {
                avatarUrl = mUser.getImageUrl();
            } else {
                avatarUrl = mFriend.getImageUrl();
            }

            Picasso.get()
                    .load(avatarUrl)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_placeholder_add_photo)
                    .error(R.drawable.ic_placeholder_add_photo)
                    .into(h.ivAvatar);
        }

        private static class Holder extends BaseHolder {

            private ImageView ivAvatar;
            private TextView tvMsg;

            public Holder(View v) {
                super(v);
                ivAvatar = v.findViewById(R.id.iv_avatar);
                tvMsg = v.findViewById(R.id.tv_msg);
            }
        }
    }
}
