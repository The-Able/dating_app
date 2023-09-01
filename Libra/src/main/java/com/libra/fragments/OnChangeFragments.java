package com.libra.fragments;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import android.widget.ImageButton;

public interface OnChangeFragments {

  void changeToolbar(String nameFg);

  void setTitleToolbar(@StringRes int res);

  void setTitleToolbar(String str);

  void changeFragment(BaseFragment fg, boolean addBackStack);

  ImageButton getRightImageButton();

  ImageButton getLeftImageButton();

  Toolbar getToolbar();
}
