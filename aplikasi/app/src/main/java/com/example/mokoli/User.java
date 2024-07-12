package com.example.mokoli;

public class User {
    private String noKamar;
    private String fullName;
    private String kwh; // Menyimpan nilai sisa token (KWH)

    public User() {
        // Diperlukan oleh Firestore
    }

    public User(String noKamar, String fullName, String kwh) {
        this.noKamar = noKamar;
        this.fullName = fullName;
        this.kwh = kwh;
    }

    public String getNoKamar() {
        return noKamar;
    }

    public String getFullName() {
        return fullName;
    }

    public String getKwh() {
        return kwh;
    }
}

