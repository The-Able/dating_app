package com.libra.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageButton;

import com.libra.R;

public abstract class BaseHostedFragment extends BaseFragment {

  protected OnChangeFragments mFragments;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    try {
      mFragments = (OnChangeFragments) context;
    } catch (ClassCastException e) {
      e.printStackTrace();
    }
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mFragments.changeToolbar(getClass().getSimpleName());
    if (getTitleToolbar() != 0) {
      mFragments.setTitleToolbar(getTitleToolbar());
    }
    if (!TextUtils.isEmpty(getTitleToolbarStr())) {
      mFragments.setTitleToolbar(getTitleToolbarStr());
    }
  }

  protected ImageButton getRightImageButton() {
    return mFragments == null ? null : mFragments.getRightImageButton();
  }

  protected ImageButton getLeftImageButton() {
    return mFragments == null ? null : mFragments.getLeftImageButton();
  }

  protected Toolbar getToolbar() {
    return mFragments == null ? null : mFragments.getToolbar();
  }

  @StringRes protected int getTitleToolbar() {
    return R.string.app_names;
  }

  protected String getTitleToolbarStr() {
    return null;
  }

  protected void setTitleToolbar(String title) {
    mFragments.setTitleToolbar(title);
  }

  public boolean handlerClickButtonToolbar(@IdRes int id) {
    return false;
  }
}
