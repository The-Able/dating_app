package com.libra.fragments;

import androidx.fragment.app.DialogFragment;

import com.libra.support.Logger;

public abstract class BaseDialogFragment extends DialogFragment {

    protected final Logger LOG = Logger.getLogger(getClass().getSimpleName(), isLogged());

    protected boolean isLogged() {
        return false;
    }
}
