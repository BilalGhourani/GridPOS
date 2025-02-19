package com.grid.pos.data.settings

import com.grid.pos.model.DivisionModel
import com.grid.pos.model.ReportCountry
import com.grid.pos.model.TransactionTypeModel
import com.grid.pos.model.WarehouseModel

interface SettingsRepository {
    suspend fun getTransactionTypeId(type: String): String?
    suspend fun getTransactionTypes(type: String): MutableList<TransactionTypeModel>
    suspend fun getDefaultBranch(): String?
    suspend fun getDefaultWarehouse(): String?
    suspend fun getPosReceiptAccIdBy(type: String, currCode: String): String?
    suspend fun getCountries(): MutableList<ReportCountry>
    suspend fun getAllWarehouses(): MutableList<WarehouseModel>
    suspend fun getAllDivisions(): MutableList<DivisionModel>

    suspend fun getSizeById(sizeId: String): String?

    suspend fun getColorById(colorId: String): String?

    suspend fun getBranchById(branchId: String): String?
    suspend fun getUserPermissions(username: String): Map<String,String>
}
