package com.example.mokoli;

public class Transaksi {
    private String currentDate;
    private String pembelianToken;
    private String jumlahToken;
    private String metodePembayaran;

    public Transaksi() {
        // Diperlukan konstruktor kosong untuk Firestore
    }

    public Transaksi(String currentDate, String pembelianToken, String jumlahToken, String metodePembayaran) {
        this.currentDate = currentDate;
        this.pembelianToken = pembelianToken;
        this.jumlahToken = jumlahToken;
        this.metodePembayaran = metodePembayaran;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getPembelianToken() {
        return pembelianToken;
    }

    public void setPembelianToken(String pembelianToken) {
        this.pembelianToken = pembelianToken;
    }

    public String getJumlahToken() {
        return jumlahToken;
    }

    public void setJumlahToken(String jumlahToken) {
        this.jumlahToken = jumlahToken;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public void setMetodePembayaran(String metodePembayaran) {
        this.metodePembayaran = metodePembayaran;
    }
}
