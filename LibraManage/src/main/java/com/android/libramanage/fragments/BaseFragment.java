package com.android.libramanage.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.android.libramanage.R;
import com.android.libramanage.activity.HostActivity;
import com.android.libramanage.support.Logger;


public abstract class BaseFragment extends Fragment implements Toolbar.OnMenuItemClickListener {

    protected final Logger LOG = Logger.getLogger(getClass().getSimpleName(), isLogged());

    private com.android.libramanage.activity.HostActivity mHost;
    private ProgressDialog mPd;

    @StringRes
    protected abstract int getTitleId();

    @LayoutRes
    protected abstract int getLayoutId();

    @MenuRes
    protected abstract int getMenuId();

    protected boolean isLogged() {
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mHost = (HostActivity) context;
    }

    @Override
    public void onDetach() {
        mHost = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getLayoutId() != 0) {
            return inflater.inflate(getLayoutId(), container, false);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPd = new ProgressDialog(getContext());
        mPd.setMessage(getString(R.string.wait));
    }

    protected void showPd() {
        mPd.show();
    }

    protected void dismissPd() {
        mPd.dismiss();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle(getTitleId() == 0 ? "" : getString(getTitleId()));

        mHost.changeFragment(this);
    }

    @Override
    public void onResume() {
        setMenu(getMenuId());
        super.onResume();

    }

    protected void showFragment(BaseFragment fg, boolean addBackStack) {
        mHost.showFragment(fg, addBackStack);
    }

    protected void setMenu(@MenuRes int res) {
        mHost.setMenu(res, this);
    }

    protected void setTitle(String title) {
        mHost.setTitle(title);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    protected Toolbar getToolbar() {
        return mHost.getToolbar();
    }

    protected void onBackPress() {
        mHost.onBackPres();
    }
}
