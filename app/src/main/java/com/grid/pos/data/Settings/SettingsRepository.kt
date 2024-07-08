package com.grid.pos.data.Settings

interface SettingsRepository {
    suspend fun getSalesInvoiceTransType():String?
    suspend fun getReturnSalesTransType():String?
}
