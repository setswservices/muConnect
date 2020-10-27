package com.mushin.muconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mushin.muconnect.databinding.ConfigurationLayoutBinding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class ConfigActivity extends AppCompatActivity {
    private ConfigurationLayoutBinding binding;
    private Configuration configuration;

    @NonNull
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        binding = DataBindingUtil.setContentView(this, R.layout.configuration_layout);

        configuration = new Configuration();
        Utils.restoreDeviceConfiguration(getApplicationContext(), configuration);

        binding.setCfg(configuration);
        binding.setActivity(this);
    }

    public void onOkButton() {
        Utils.storeDeviceConfiguration(getApplicationContext(),configuration);
        Intent result = new Intent();
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    public void onCancelButton() {
        Intent result = new Intent();
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }

}

