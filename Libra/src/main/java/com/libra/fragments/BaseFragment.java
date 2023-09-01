package com.libra.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.libra.R;
import com.libra.support.Logger;

public abstract class BaseFragment extends Fragment {

    protected final Logger LOG = Logger.getLogger(getClass().getSimpleName(), isLogged());

    private ProgressDialog mProgressDialog;

    @LayoutRes
    protected abstract int getLayoutId();

    protected boolean isLogged() {
        return false;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage(getString(R.string.wait));
        mProgressDialog.setCancelable(false);
    }

    protected void showPd() {
        mProgressDialog.show();
    }

    protected void dismissPd() {
        mProgressDialog.dismiss();
    }
}
