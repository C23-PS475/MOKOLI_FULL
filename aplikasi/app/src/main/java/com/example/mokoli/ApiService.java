package com.example.mokoli;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    // Definisikan metode untuk mengirim userID menggunakan metode POST
    @POST("/userID")
    Call<Void> sendUserID(@Body UserIDData userIDData);

}



