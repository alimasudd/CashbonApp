package com.wtm.cashbon.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by @alimasudd on 9/12/2019.
 */
public class GlobalToast {


    public static void ShowToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

    public static void ShowToast(String valueOf) {
    }
}
