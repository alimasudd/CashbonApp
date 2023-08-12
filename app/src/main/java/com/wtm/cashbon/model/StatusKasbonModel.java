package com.wtm.cashbon.model;

public class StatusKasbonModel {

    String tanggal;
    String Status;
    String nominal;
    String noPinjaman;
    String isSigned;
    String idPengajuan;
    String idKlien;
    String SignStatus;
    String SignRegistered;
    int nomer;

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getNominal() {
        return nominal;
    }

    public void setNominal(String nominal) {
        this.nominal = nominal;
    }

    public String getNoPinjaman() {
        return noPinjaman;
    }

    public void setNoPinjaman(String noPinjaman) {
        this.noPinjaman = noPinjaman;
    }


    public int getNomer() {
        return nomer;
    }

    public void setNomer(int nomer) {
        this.nomer = nomer;
    }

    public String getIsSigned() {
        return isSigned;
    }

    public void setIsSigned(String isSigned) {
        this.isSigned = isSigned;
    }

    public String getIdPengajuan() {
        return idPengajuan;
    }

    public void setIdPengajuan(String idPengajuan) {
        this.idPengajuan = idPengajuan;
    }

    public String getIdKlien() {
        return idKlien;
    }

    public void setIdKlien(String idKlien) {
        this.idKlien = idKlien;
    }

    public String getSignStatus() {
        return SignStatus;
    }

    public void setSignStatus(String signStatus) {
        SignStatus = signStatus;
    }

    public String getSignRegistered() {
        return SignRegistered;
    }

    public void setSignRegistered(String signRegistered) {
        SignRegistered = signRegistered;
    }




}
