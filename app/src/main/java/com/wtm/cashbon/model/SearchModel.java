package com.wtm.cashbon.model;

/**
 * Created by @alimasudd on 26/07/2019.
 */

public class SearchModel {

    String id, nama, keterangan, keterangan2;

    public SearchModel() {

    }

    public SearchModel(String id, String nama, String keterangan) {
        this.id = id;
        this.nama = nama;
        this.keterangan = keterangan;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getKeterangan2() {
        return keterangan2;
    }

    public void setKeterangan2(String keterangan2) {
        this.keterangan2 = keterangan2;
    }

    @Override
    public String toString() {
        return nama;

    }
}
