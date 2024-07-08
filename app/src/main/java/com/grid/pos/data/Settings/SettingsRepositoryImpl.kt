package com.grid.pos.data.Settings

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Utils
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.Date

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
                if (dbResult.isNotEmpty()) {
                    return dbResult[0].optString("tt_code")
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
                if (dbResult.isNotEmpty()) {
                    return dbResult[0].optString("tt_code")
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
                if (dbResult.isNotEmpty()) {
                    return dbResult[0].optString("bra_name")
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
                val where = if (SettingsModel.isSqlServerWebDb) {
                    "wa_order=1 and wa_cmp_id='${SettingsModel.getCompanyID()}'"
                } else {
                    "wa_order=1"
                }
                val dbResult = SQLServerWrapper.getListOf(
                    "warehouse",
                    "",
                    mutableListOf(
                        "wa_name"
                    ),
                    where
                )
                if (dbResult.isNotEmpty()) {
                    return dbResult[0].optString("wa_name")
                }
                return null
            }
        }
    }

}