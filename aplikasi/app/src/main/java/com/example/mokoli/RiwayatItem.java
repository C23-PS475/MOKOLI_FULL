package com.example.mokoli;

public class RiwayatItem {
    private String currentDate;
    private String fullname;
    private String jumlahToken;
    private String metodePembayaran;
    private String pembelianToken;


    public RiwayatItem(String currentDate, String fullname, String jumlahToken, String metodePembayaran, String pembelianToken) {
        this.currentDate = currentDate;
        this.fullname = fullname;
        this.jumlahToken = jumlahToken;
        this.metodePembayaran = metodePembayaran;
        this.pembelianToken = pembelianToken;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public String getFullname() {
        return fullname;
    }

    public String getJumlahToken() {
        return jumlahToken;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public String getPembelianToken() {
        return pembelianToken;
    }
}

