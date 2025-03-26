package com.grid.pos.network

import com.grid.pos.model.LoginResponseModel
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("bomain/set_users/get_connection_string")
    suspend fun login(
        @Query("usr_name") username: String,
        @Query("usr_password") password: String
    ): LoginResponseModel
}