package com.grid.pos.data.connection

import com.grid.pos.model.LoginResponseModel
import com.grid.pos.network.RetrofitClient

class ConnectionRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun login(username: String, password: String): LoginResponseModel {
        return apiService.login(username, password)
    }
}