package com.grid.pos.data.settings

import com.grid.pos.model.ReportCountry

interface SettingsRepository {
    suspend fun getTransactionTypeId(type: String):String?
    suspend fun getDefaultBranch():String?
    suspend fun getDefaultWarehouse():String?
    suspend fun getPosReceiptAccIdBy(type:String,currCode:String): String?
    suspend fun getCountries(): MutableList<ReportCountry>
}
