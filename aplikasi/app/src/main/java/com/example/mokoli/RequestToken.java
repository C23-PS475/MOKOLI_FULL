package com.example.mokoli;

import java.util.HashMap;
import java.util.Map;

public class RequestToken {
    private String currentDate;
    private String pembelianToken;
    private String jumlahToken;
    private String metodePembayaran;
    private String buktiPembayaran;
    private String fullname;
    private String requestId;

    private String userId;

    public RequestToken() {
        // Diperlukan untuk parsing data dari Firestore
    }

    public RequestToken(String currentDate, String pembelianToken, String jumlahToken, String metodePembayaran, String buktiPembayaran, String fullname, String userId, String requestId) {
        this.currentDate = currentDate;
        this.pembelianToken = pembelianToken;
        this.jumlahToken = jumlahToken;
        this.metodePembayaran = metodePembayaran;
        this.buktiPembayaran = buktiPembayaran;
        this.fullname = fullname;
        this.userId = userId;
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("currentDate", currentDate);
        map.put("pembelianToken", pembelianToken);
        map.put("jumlahToken", jumlahToken);
        map.put("metodePembayaran", metodePembayaran);
        map.put("buktiPembayaran", buktiPembayaran);
        map.put("fullname", fullname);
        return map;
    }

    public String getRequestId() {
        return requestId;
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

    public String getBuktiPembayaran() {
        return buktiPembayaran;
    }

    public void setBuktiPembayaran(String buktiPembayaran) {
        this.buktiPembayaran = buktiPembayaran;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

}



