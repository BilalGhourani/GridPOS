package com.grid.pos.data.settings

import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.ReportCountry
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.WarehouseModel
import com.grid.pos.utils.Extension.getStringValue
import com.grid.pos.utils.Utils
import java.sql.ResultSet

class SettingsRepositoryImpl : SettingsRepository {
    override suspend fun getTransactionTypeId(type: String): String? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                // nothing to do
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                //nothing to do
                return null
            }

            else -> {
                var result: String? = null
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) {
                        "tt_type='$type' and tt_cmp_id='${SettingsModel.getCompanyID()}'"
                    } else {
                        "tt_type='$type'"
                    }

                    val dbResult = SQLServerWrapper.getListOf(
                        "acc_transactiontype",
                        "",
                        mutableListOf(
                            "tt_code"
                        ),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            if (it.next()) {
                                result = it.getStringValue("tt_code")
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return result
            }
        }
    }

    override suspend fun getDefaultBranch(): String? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                // nothing to do
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                //nothing to do
                return null
            }

            else -> {
                var result: String? = null
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) {
                        "bra_default=1 and bra_cmp_id='${SettingsModel.getCompanyID()}'"
                    } else {
                        "bra_default=1"
                    }
                    val dbResult = SQLServerWrapper.getListOf(
                        "branch",
                        "",
                        mutableListOf(
                            "bra_name"
                        ),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            if (it.next()) {
                                result = it.getStringValue("bra_name")
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return result
            }
        }
    }

    override suspend fun getDefaultWarehouse(): String? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                // nothing to do
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                //nothing to do
                return null
            }

            else -> {
                var result: String? = null
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) {
                        "wa_order=1 and wa_cmp_id='${SettingsModel.getCompanyID()}'"
                    } else {
                        "wa_order=1"
                    }
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_warehouse",
                        "",
                        mutableListOf(
                            "wa_name"
                        ),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            if (it.next()) {
                                result = it.getStringValue("wa_name")
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return result
            }
        }
    }

    override suspend fun getPosReceiptAccIdBy(
            type: String,
            currCode: String
    ): String? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return null
            }

            else -> {
                var result: String? = null
                try {
                    var where = "ra_cur_code='$currCode' AND ra_type = '$type'"
                    if (SettingsModel.isSqlServerWebDb) {
                        where += " AND ra_cmp_id = '${SettingsModel.getCompanyID()}'"
                    }
                    val dbResult = SQLServerWrapper.getListOf(
                        "pos_receiptacc",
                        "TOP 1",
                        mutableListOf("ra_id"),
                        where,
                        "group by ra_id"
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            if (it.next()) {
                                result = it.getStringValue("ra_id")
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return result
            }
        }
    }

    override suspend fun getCountries(): MutableList<ReportCountry> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return Utils.getReportCountries(true)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return Utils.getReportCountries(true)
            }

            else -> {
                if (SettingsModel.isSqlServerWebDb) {
                    val result: MutableList<ReportCountry> = mutableListOf(
                        ReportCountry(
                            "Default",
                            "Default"
                        )
                    )
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "set_country",
                            "",
                            mutableListOf("cu_name,cu_countryshortname"),
                            ""
                        )
                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                if (it.next()) {
                                    result.add(
                                        ReportCountry(
                                            it.getStringValue("cu_countryshortname"),
                                            it.getStringValue("cu_name"),
                                        )
                                    )
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return result
                } else {
                    return Utils.getReportCountries(true)
                }
            }
        }
    }

    override suspend fun getAllWarehouses(): MutableList<WarehouseModel> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                // nothing to do
                return mutableListOf()
            }

            CONNECTION_TYPE.LOCAL.key -> {
                //nothing to do
                return mutableListOf()
            }

            else -> {
                val warehouses: MutableList<WarehouseModel> = mutableListOf()
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) {
                        "wa_cmp_id='${SettingsModel.getCompanyID()}'"
                    } else {
                        ""
                    }
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_warehouse",
                        "",
                        mutableListOf(
                            "*"
                        ),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            if (it.next()) {
                                warehouses.add(WarehouseModel().apply {
                                    warehouseId = it.getStringValue("wa_name")
                                    warehouseName = it.getStringValue("wa_newname",it.getStringValue("wa_name"))
                                    warehouseOrder = it.getStringValue("wa_order")
                                })
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return warehouses
            }
        }
    }

}