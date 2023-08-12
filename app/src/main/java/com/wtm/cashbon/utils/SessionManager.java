package com.wtm.cashbon.utils;

/**
 * Created by @alimasudd on 9/12/2019.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.wtm.cashbon.LoginActivity;
import com.wtm.cashbon.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class SessionManager {

    SharedPreferences pref;

    Editor editor;

    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "com.pelunas";

    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_NAMA = "nama";

    public static final String KEY_NAMA_REKENING = "nama_rekening";

    public static final String KEY_EMAIL = "email";

    public static final String KEY_PASSWORD = "password";

    public static final String KEY_REKENING = "rekening";

    public static final String KEY_IMAGE_PROFILE = "image_profile";

    public static final String KEY_KODE = "kode";

    public static final String KEY_TOKEN = "token";

    public static final String KEY_BUNGA = "bunga";

    public static final String KEY_BIAYA_ADMIN = "biaya_admin";

    public static final String KEY_MAX_PINJAM = "max_pinjam";

    public static final String KEY_KELIPATAN = "kelipatan";

    public static final String KEY_JUMLAH_AWAL = "jumlah_awal";

    public static final String KEY_STATUS_PINJAMAN = "status_pinjaman";

    public static final String KEY_DONE = "done";

    public static final String KEY_EDIT_BANK = "edit_bank";

    public static final String KEY_BANK_ID = "bank_id";

    public static final String KEY_BANK = "bank";

    public static final String KEY_LATITUDE = "latitude";

    public static final String KEY_LONGITUDE = "longitude";

    public static final String KEY_ID_KLIEN= "id_klien";


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String token) {

        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_TOKEN, token);

        editor.commit();
    }

//    public void createLoginInvoiceSession(String token) {
//
//        editor.putBoolean(IS_LOGININVOICE, true);
//
//        editor.putString(KEY_TOKENINVOICE, token);
//
//        editor.commit();
//    }


    public boolean checkLogin() {
        // Check login status

        boolean stLogin = true;

        if (!this.isLoggedIn()) {

            stLogin = false;
        }

        return stLogin;

    }

    public String getNama() {

        return pref.getString(KEY_NAMA, "");
    }

    public void setNama(String nama) {
        editor.putString(KEY_NAMA, nama);
        editor.commit();
    }

    public String getNamaRekening() {

        return pref.getString(KEY_NAMA_REKENING, "");
    }

    public void setNamaRekening(String nama) {
        editor.putString(KEY_NAMA_REKENING, nama);
        editor.commit();
    }

    public String getIDKlien() {

        return pref.getString(KEY_ID_KLIEN, null);
    }

    public void setIDKlien(String id_klien) {
        editor.putString(KEY_ID_KLIEN, id_klien);
        editor.commit();
    }

    public String getBunga() {

        return pref.getString(KEY_BUNGA, "0");
    }

    public void setBunga(String bunga) {
        editor.putString(KEY_BUNGA, bunga);
        editor.commit();
    }

    public boolean getEditBank() {

        return pref.getBoolean(KEY_EDIT_BANK, false);
    }

    public void setEditBank(boolean editBank) {
        editor.putBoolean(KEY_EDIT_BANK, editBank);
        editor.commit();
    }

    public int getDone() {

        return pref.getInt(KEY_DONE, 0);
    }

    public void setDone(int done) {
        editor.putInt(KEY_DONE, done);
        editor.commit();
    }

    public int getBankID() {

        return pref.getInt(KEY_BANK_ID, 0);
    }

    public void setBankID(int bankID) {
        editor.putInt(KEY_BANK_ID, bankID);
        editor.commit();
    }

    public String getBank() {

        return pref.getString(KEY_BANK, "");
    }

    public void setBank(String bank) {
        editor.putString(KEY_BANK, bank);
        editor.commit();
    }

    public String getBiayaAdmin() {

        return pref.getString(KEY_BIAYA_ADMIN, "0");
    }

    public void setBiayaAdmin(String biayaAdmin) {
        editor.putString(KEY_BIAYA_ADMIN, biayaAdmin);
        editor.commit();
    }

    public String getStatusPinjaman() {

        return pref.getString(KEY_STATUS_PINJAMAN, "");
    }

    public void setStatusPinjaman(String json) {
        editor.putString(KEY_STATUS_PINJAMAN, json);
        editor.commit();
    }

    public String getMaxPinjam() {

        return pref.getString(KEY_MAX_PINJAM, "0");
    }

    public void setMaxPinjam(String maxPinjam) {
        editor.putString(KEY_MAX_PINJAM, maxPinjam);
        editor.commit();
    }

    public String getKelipatan() {

        return pref.getString(KEY_KELIPATAN, "0");
    }

    public void setKelipatan(String kelipatan) {
        editor.putString(KEY_KELIPATAN, kelipatan);
        editor.commit();
    }

    public String getJumlahAwal() {

        return pref.getString(KEY_JUMLAH_AWAL, "0");
    }

    public void setJumlahAwal(String JumlahAwal) {
        editor.putString(KEY_JUMLAH_AWAL, JumlahAwal);
        editor.commit();
    }

    public String getLatitude() {

        return pref.getString(KEY_LATITUDE, null);
    }

    public void setLatitude(String latitude) {
        editor.putString(KEY_LATITUDE, latitude);
        editor.commit();
    }

    public String getLongitude() {

        return pref.getString(KEY_LONGITUDE, null);
    }

    public void setLongitude(String longitude) {
        editor.putString(KEY_LONGITUDE, longitude);
        editor.commit();
    }

    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();


        Intent in = new Intent(_context, LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        _context.startActivity(in);

    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

//    public boolean isLoggedInInvoice() {
//        return pref.getBoolean(IS_LOGININVOICE, false);
//    }

    public String getEmail() {

        return pref.getString(KEY_EMAIL, null);
    }

    public void setEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public String getPassword() {

        return pref.getString(KEY_PASSWORD, null);
    }

    public void setPassword(String password) {
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public String getRekening() {

        return pref.getString(KEY_REKENING, null);
    }

    public void setRekening(String rekening) {
        editor.putString(KEY_REKENING, rekening);
        editor.commit();
    }

    public String getImageProfile() {

        return pref.getString(KEY_IMAGE_PROFILE, "");
    }

    public void setImageProfile(String imageProfile) {
        editor.putString(KEY_IMAGE_PROFILE, imageProfile);
        editor.commit();
    }

    public String getKode() {

        return pref.getString(KEY_KODE, null);
    }

    public void setKode(String kode) {
        editor.putString(KEY_KODE, kode);
        editor.commit();
    }

    public String getToken() {

        return pref.getString(KEY_TOKEN, null);
    }

    public void setToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    public void checkError(VolleyError error) {

        if (error instanceof TimeoutError) {
            GlobalToast.ShowToast(_context, _context.getString(R.string.timeout));
        } else if (error instanceof NoConnectionError) {
            GlobalToast.ShowToast(_context, _context.getString(R.string.no_connection));
        } else {
            GlobalToast.ShowToast(_context, _context.getString(R.string.gagal_server));
        }
    }

    public boolean checkResponse(String response) {

        boolean verify = true;

        try {
            Object json = new JSONTokener(response).nextValue();

            if (json instanceof JSONObject) {
                JSONObject jo = new JSONObject(response);
                boolean result = true;

                if (jo.has("responses")) {
                    if (jo.get("responses") instanceof Boolean) {
                        result = jo.getBoolean("responses");
                    }
                }
                if (jo.has("response")) {
                    if (jo.get("response") instanceof Boolean) {
                        result = jo.getBoolean("response");
                    }
                }

                if (!result) {
                    if (jo.has("message")) {

                        String message = jo.getString("message");
                        if (message.toLowerCase().contains("session")) {

                            verify = false;

                            logoutUser();

                        }

                        GlobalToast.ShowToast(_context, message);

                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return verify;
    }
}