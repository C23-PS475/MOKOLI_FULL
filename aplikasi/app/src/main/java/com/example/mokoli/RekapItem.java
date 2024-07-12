package com.example.mokoli;

public class RekapItem {
    private String bulan;
    private double totalPembelianToken;
    private double totalJumlahToken;

    public RekapItem(String bulan, double totalPembelianToken, double totalJumlahToken) {
        this.bulan = bulan;
        this.totalPembelianToken = totalPembelianToken;
        this.totalJumlahToken = totalJumlahToken;
    }

    public String getBulan() {
        return bulan;
    }

    public double getTotalPembelianToken() {
        return totalPembelianToken;
    }

    public double getTotalJumlahToken() {
        return totalJumlahToken;
    }
}
