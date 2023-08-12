package com.wtm.cashbon.model;

public class TugasModel {

    String id;
    String judul;
    String status;
    String tanggalAwal;
    String tanggalAkhir;
    String prioritas;
    String prosentase;
    String prioritas_id;
    String status_id;
    String keterangan;

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrioritas_id() {
        return prioritas_id;
    }

    public void setPrioritas_id(String prioritas_id) {
        this.prioritas_id = prioritas_id;
    }

    public String getStatus_id() {
        return status_id;
    }

    public void setStatus_id(String status_id) {
        this.status_id = status_id;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTanggalAwal() {
        return tanggalAwal;
    }

    public void setTanggalAwal(String tanggalAwal) {
        this.tanggalAwal = tanggalAwal;
    }

    public String getTanggalAkhir() {
        return tanggalAkhir;
    }

    public void setTanggalAkhir(String tanggalAkhir) {
        this.tanggalAkhir = tanggalAkhir;
    }

    public String getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(String prioritas) {
        this.prioritas = prioritas;
    }

    public String getProsentase() {
        return prosentase;
    }

    public void setProsentase(String prosentase) {
        this.prosentase = prosentase;
    }
}
