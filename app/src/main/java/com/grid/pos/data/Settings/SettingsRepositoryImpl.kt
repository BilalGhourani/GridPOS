package com.grid.pos.data.Settings

import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.getStringValue

class SettingsRepositoryImpl : SettingsRepository {
    override suspend fun getSalesInvoiceTransType(): String {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                // nothing to do
                return "SI"
            }

            CONNECTION_TYPE.LOCAL.key -> {
                //nothing to do
                return "SI"
            }

            else -> {
                var result = "SI"
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) {
                        "tt_type='Sales Invoice' and tt_cmp_id='${SettingsModel.getCompanyID()}'"
                    } else {
                        "tt_type='Sales Invoice'"
                    }

                    val dbResult = SQLServerWrapper.getListOf(
                        "acc_transactiontype",
                        "",
                        mutableListOf(
                            "tt_code"
                        ),
                        where
                    )
                    dbResult?.let {
                        if (it.next()) {
                            result = it.getStringValue(
                                "tt_code",
                                "SI"
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return result
            }
        }
    }

    override suspend fun getReturnSalesTransType(): String {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                // nothing to do
                return "RS"
            }

            CONNECTION_TYPE.LOCAL.key -> {
                //nothing to do
                return "RS"
            }

            else -> {
                var result = "RS"
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) {
                        "tt_type='Return Sale' and tt_cmp_id='${SettingsModel.getCompanyID()}'"
                    } else {
                        "tt_type='Return Sale'"
                    }
                    val dbResult = SQLServerWrapper.getListOf(
                        "acc_transactiontype",
                        "",
                        mutableListOf(
                            "tt_code"
                        ),
                        where
                    )
                    dbResult?.let {
                        if (it.next()) {
                            result = it.getStringValue(
                                "tt_code",
                                "RS"
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
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
                    dbResult?.let {
                        if (it.next()) {
                            result = it.getStringValue("bra_name")
                        }
                        SQLServerWrapper.closeResultSet(it)
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
                        if (SettingsModel.isSqlServerWebDb) "warehouse" else "st_warehouse",
                        "",
                        mutableListOf(
                            "wa_name"
                        ),
                        where
                    )
                    dbResult?.let {
                        if (it.next()) {
                            result = it.getStringValue("wa_name")
                        }
                        SQLServerWrapper.closeResultSet(it)
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
                    dbResult?.let {
                        if (it.next()) {
                            result = it.getStringValue("ra_id")
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return result
            }
        }
        return null
    }

}