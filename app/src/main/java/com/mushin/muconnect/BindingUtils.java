package com.mushin.muconnect;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;

public class BindingUtils {

    @BindingAdapter("android:text")
    public static void setInteger(TextView view, int value) {
        if (Float.isNaN(value)) {
            view.setText("");
        } else {
            view.setText( String.valueOf(value) );
        }
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static int getInteger(TextView view) {
        String num = view.getText().toString();
        if (num.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @BindingAdapter("android:text")
    public static void setFloat(TextView view, float value) {
        if (Float.isNaN(value)) {
            view.setText("");
        } else {
            view.setText( String.valueOf(value) );
        }
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static float getFloat(TextView view) {
        String num = view.getText().toString();
        if (num.isEmpty()) {
            return 0.0F;
        }

        try {
            return Float.parseFloat(num);
        } catch (NumberFormatException e) {
            return 0.0F;
        }
    }
}
