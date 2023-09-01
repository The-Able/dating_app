package com.android.libramanage.fragments;

import androidx.fragment.app.DialogFragment;

import com.android.libramanage.support.Logger;

public abstract class BaseDialogFragment extends DialogFragment {

    protected final Logger LOG = Logger.getLogger(getClass().getSimpleName(), isLogged());

    protected boolean isLogged() {
        return false;
    }
}
