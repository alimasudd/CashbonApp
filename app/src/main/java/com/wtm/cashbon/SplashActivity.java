package com.wtm.cashbon;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.wtm.cashbon.utils.SessionHelper;

public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    private final int DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler = new Handler();
        handler.postDelayed(runnable, DELAY);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            if (SessionHelper.sessionManager(SplashActivity.this).getIntro() == 1) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
//            } else {
//                Intent intent = new Intent(SplashActivity.this, IntroActivity.class);
//                startActivity(intent);
//            }
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        super.onDestroy();
    }
}