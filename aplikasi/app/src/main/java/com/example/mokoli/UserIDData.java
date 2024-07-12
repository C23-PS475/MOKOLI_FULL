package com.example.mokoli;
import com.google.gson.annotations.SerializedName;

public class UserIDData {
    @SerializedName("userID")
    private String userID;

    public UserIDData(String userID) {
        this.userID = userID;
    }

    // Getter dan setter jika diperlukan
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}


