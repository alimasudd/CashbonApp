package com.wtm.cashbon.home.absensi;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.ActivityAbsensiBinding;
import com.wtm.cashbon.databinding.ActivityCheckOutBinding;
import com.wtm.cashbon.databinding.ActivityViewCheckoutBinding;
import com.wtm.cashbon.utils.SessionManager;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ViewCheckoutActivity extends AppCompatActivity {


    private Intent extra;
    private SessionManager sessionManager;

    private ActivityViewCheckoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        extra = getIntent();

        init();
    }

    private void init(){
        binding.txName.setText(sessionManager.getNama());
        binding.txJamKerja.setText(extra.getStringExtra("jam_kerja"));
        binding.txJamMasuk.setText(extra.getStringExtra("jam_masuk"));
        binding.txJamKeluar.setText(extra.getStringExtra("jam_keluar"));
        binding.txJam.setText(extra.getStringExtra("jam"));
        binding.txMenit.setText(extra.getStringExtra("menit"));
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.btKembali})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:

            case R.id.btKembali:
                finish();
                break;
        }
    }
}