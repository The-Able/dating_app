package com.android.libramanage.activity;

import androidx.annotation.MenuRes;
import androidx.appcompat.widget.Toolbar;

import com.android.libramanage.fragments.BaseFragment;


public interface HostActivity {

    void showFragment(BaseFragment fg, boolean addBackStack);

    void changeFragment(BaseFragment fg);

    void setTitle(String title);

    void setMenu(@MenuRes int res, Toolbar.OnMenuItemClickListener listener);

    void onBackPres();

    Toolbar getToolbar();
}
