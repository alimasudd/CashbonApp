package com.wtm.cashbon.home;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.ActivityAbsensiBinding;
import com.wtm.cashbon.databinding.ActivityProjectHomeBinding;
import com.wtm.cashbon.home.project.ListKunjunganActivity;
import com.wtm.cashbon.home.project.ListProjectActivity;
import com.wtm.cashbon.home.project.ListTugasActivity;
import com.wtm.cashbon.utils.SessionManager;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class ProjectHomeActivity extends AppCompatActivity {


    private Intent extra;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;

    private ActivityProjectHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        extra = getIntent();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.relativ_project, R.id.relativ_tugas, R.id.relativ_kunjungan})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.relativ_project:
                startActivity(new Intent(getApplicationContext(), ListProjectActivity.class));
                break;

            case R.id.relativ_kunjungan:
                startActivity(new Intent(getApplicationContext(), ListKunjunganActivity.class));
                break;

            case R.id.relativ_tugas:
                startActivity(new Intent(getApplicationContext(), ListTugasActivity.class));
                break;
        }
    }
}