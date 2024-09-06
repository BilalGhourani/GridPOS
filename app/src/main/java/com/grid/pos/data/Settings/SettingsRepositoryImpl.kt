package com.grid.pos.data.Settings

import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel

class SettingsRepositoryImpl : SettingsRepository {
    override suspend fun getSalesInvoiceTransType(): String? {
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
                            return it.getString("tt_code")
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }

    override suspend fun getReturnSalesTransType(): String? {
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
                            return it.getString("tt_code")
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
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
                            return it.getString("bra_name")
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
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
                            return it.getString("wa_name")
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }

}