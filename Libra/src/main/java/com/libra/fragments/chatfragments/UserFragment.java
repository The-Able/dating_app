package com.libra.fragments.chatfragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.libra.R;
import com.libra.entity.User;
import com.libra.fragments.BaseFragment;
import com.libra.fragments.BaseHostedFragment;
import com.squareup.picasso.Picasso;

public class UserFragment extends BaseHostedFragment {

    private static final String USER = "user";

    private ImageView ivAvatar;
    CardView closeBtn;
    private User mUser;

    public static BaseFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putParcelable(USER, user);
        BaseFragment fragment = new UserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getArguments().getParcelable(USER);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        closeBtn =  view.findViewById(R.id.close_btn);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Picasso.get()
                .load(mUser.getImageUrl())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder_add_photo)
                .error(R.drawable.ic_placeholder_add_photo)
                .into(ivAvatar);
    }

    @Override
    protected String getTitleToolbarStr() {
        return mUser.getName();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user;
    }
}
