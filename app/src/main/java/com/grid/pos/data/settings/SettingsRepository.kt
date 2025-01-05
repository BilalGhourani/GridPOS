package com.grid.pos.data.settings

import com.grid.pos.model.ReportCountry
import com.grid.pos.model.WarehouseModel

interface SettingsRepository {
    suspend fun getTransactionTypeId(type: String): String?
    suspend fun getDefaultBranch(): String?
    suspend fun getDefaultWarehouse(): String?
    suspend fun getPosReceiptAccIdBy(type: String, currCode: String): String?
    suspend fun getCountries(): MutableList<ReportCountry>
    suspend fun getAllWarehouses(): MutableList<WarehouseModel>

    suspend fun getSizeById(sizeId: String): String?

    suspend fun getColorById(colorId: String): String?

    suspend fun getBranchById(branchId: String): String?
}
