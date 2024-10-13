package com.grid.pos.data.Settings

interface SettingsRepository {
    suspend fun getTransactionTypeId(type: String):String?
    suspend fun getDefaultBranch():String?
    suspend fun getDefaultWarehouse():String?
    suspend fun getPosReceiptAccIdBy(type:String,currCode:String): String?
}
