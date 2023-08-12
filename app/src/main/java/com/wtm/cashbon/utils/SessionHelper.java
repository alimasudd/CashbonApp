package com.wtm.cashbon.utils;

import android.content.Context;

/**
 * Created by @alimasudd on 9/12/2019.
 */

public class SessionHelper {

    public static SessionManager sessionManager(Context context){

        SessionManager sManager = new SessionManager(context);

        return sManager;
    }
}
