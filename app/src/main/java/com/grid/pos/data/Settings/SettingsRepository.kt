package com.grid.pos.data.Settings

interface SettingsRepository {
    suspend fun getSalesInvoiceTransType():String
    suspend fun getReturnSalesTransType():String
    suspend fun getDefaultBranch():String?
    suspend fun getDefaultWarehouse():String?
    suspend fun getPosReceiptAccIdBy(type:String,currCode:String): String?
}
