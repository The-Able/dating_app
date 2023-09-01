package com.libra.fragments.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.libra.R;
import com.libra.fragments.BaseDialogFragment;

public class MsgDialogsFragment extends BaseDialogFragment implements View.OnClickListener {

    private static final String TAG_MSG = "message";
    private String msg;

    public static BaseDialogFragment newInstance(String msg) {
        Bundle args = new Bundle();
        args.putString(TAG_MSG, msg);
        BaseDialogFragment fragment = new MsgDialogsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msg = getArguments().getString(TAG_MSG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_msg, (ViewGroup) getView());
        TextView tvMsg = (TextView) v.findViewById(R.id.tv_msg);
        tvMsg.setText(msg);
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
