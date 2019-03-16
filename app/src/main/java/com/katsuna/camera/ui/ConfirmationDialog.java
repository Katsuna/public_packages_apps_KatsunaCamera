package com.katsuna.camera.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import java.util.Objects;

public class ConfirmationDialog extends DialogFragment {

    private static final String ARG_MESSAGE_ID = "ARG_MESSAGE_ID";
    private static final String ARG_PERMISSIONS = "PERMISSIONS";
    private static final String ARG_REQUEST_CODE = "ARG_REQUEST_CODE";

    public static ConfirmationDialog newInstance(int messageId, String[] permissions, int requestCode) {
        ConfirmationDialog dialog = new ConfirmationDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE_ID, messageId);
        args.putStringArray(ARG_PERMISSIONS, permissions);
        args.putInt(ARG_REQUEST_CODE, requestCode);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Fragment parent = getParentFragment();

        Bundle args = Objects.requireNonNull(getArguments());
        int messageId = args.getInt(ARG_MESSAGE_ID);
        String[] permissions = args.getStringArray(ARG_PERMISSIONS);
        int requestCode = args.getInt(ARG_REQUEST_CODE);

        return new AlertDialog.Builder(getActivity())
            .setMessage(messageId)
            .setPositiveButton(android.R.string.ok, (dialog, which) ->
                requestPermissions(permissions, requestCode))
            .setNegativeButton(android.R.string.cancel,
                (dialog, which) ->
                {
                    if (parent != null) {
                        Activity activity = parent.getActivity();
                        if (activity != null) {
                            activity.finish();
                        }
                    }
                })
            .create();
    }

}


