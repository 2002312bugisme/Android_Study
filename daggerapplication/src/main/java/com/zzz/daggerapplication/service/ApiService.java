package com.zzz.daggerapplication.service;

import com.zzz.daggerapplication.pojo.User;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("/api/user")
    Call<User> getUser();
}
