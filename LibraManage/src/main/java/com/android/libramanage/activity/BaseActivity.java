package com.android.libramanage.activity;

import androidx.appcompat.app.AppCompatActivity;

import com.android.libramanage.support.Logger;

public abstract class BaseActivity extends AppCompatActivity {

    protected final Logger LOG = Logger.getLogger(getClass().getSimpleName(), isLogged());

    protected boolean isLogged() {
        return false;
    }

}
