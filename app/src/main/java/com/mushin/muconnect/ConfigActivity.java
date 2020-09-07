package com.mushin.muconnect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.mushin.muconnect.databinding.ConfigurationLayoutBinding;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

public class ConfigActivity extends DialogFragment {
    private Dialog dialog;
    private ConfigurationLayoutBinding binding;
    private Configuration configuration;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        binding = DataBindingUtil.inflate(inflater, R.layout.configuration_layout, null, false);

        MainActivity activity = (MainActivity) getActivity();

        configuration = new Configuration();
        configuration.copy(activity.getDeviceConfiguration());

        binding.setCfg(configuration);
        binding.setActivity(this);

        View view = binding.getRoot();

        builder.setView(view);

        dialog = builder.create();

        // hack to enable soft keyboard
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }

    public void onOkButton() {
        MainActivity activity = (MainActivity) getActivity();
        activity.setDeviceConfiguration(configuration);
        dialog.dismiss();
    }

    public void onCancelButton() {
        dialog.dismiss();
    }
}

