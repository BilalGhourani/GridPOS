package com.grid.pos.data.settings

import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DivisionModel
import com.grid.pos.model.ReportCountry
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.TransactionTypeModel
import com.grid.pos.model.WarehouseModel
import com.grid.pos.utils.Extension.getIntValue
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
                        "tt_type='$type' AND tt_cmp_id='${SettingsModel.getCompanyID()}' AND tt_default = 1"
                    } else {
                        "tt_type='$type' AND tt_default = 1"
                    }

                    val dbResult = SQLServerWrapper.getListOf(
                        "acc_transactiontype",
                        "TOP 1",
                        mutableListOf(
                            "tt_code"
                        ),
                        where
                    )
                    dbResult?.let {
                        if (it.next()) {
                            result = it.getStringValue("tt_code")
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

    override suspend fun getTransactionTypes(type: String): MutableList<TransactionTypeModel> {
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
                val transactionTypes: MutableList<TransactionTypeModel> = mutableListOf()
                try {
                    val where: String
                    val codeKey: String
                    if (SettingsModel.isSqlServerWebDb) {
                        where = "tt_type='$type' and tt_cmp_id='${SettingsModel.getCompanyID()}'"
                        codeKey = "tt_newcode"
                    } else {
                        where = "tt_type='$type'"
                        codeKey = "tt_code"
                    }

                    val dbResult = SQLServerWrapper.getListOf(
                        "acc_transactiontype",
                        "",
                        mutableListOf("*"),
                        where,
                        "ORDER BY $codeKey ASC"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            transactionTypes.add(
                                TransactionTypeModel(
                                    transactionTypeId = it.getStringValue("tt_code"),
                                    transactionTypeCode = it.getStringValue(codeKey),
                                    transactionTypeDesc = it.getStringValue("tt_desc"),
                                    transactionTypeDefault = it.getIntValue("tt_default")
                                )
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return transactionTypes
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
                        "st_warehouse",
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
                        dbResult?.let {
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
                    val where: String
                    val nameKey: String
                    if (SettingsModel.isSqlServerWebDb) {
                        where = "wa_cmp_id='${SettingsModel.getCompanyID()}'"
                        nameKey = "wa_newname"
                    } else {
                        where = ""
                        nameKey = "wa_name"
                    }
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_warehouse",
                        "",
                        mutableListOf(
                            "*"
                        ),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            warehouses.add(WarehouseModel().apply {
                                warehouseId = it.getStringValue("wa_name")
                                warehouseName = it.getStringValue(nameKey)
                                warehouseOrder = it.getStringValue("wa_order")
                            })
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return warehouses
            }
        }
    }

    override suspend fun getAllDivisions(): MutableList<DivisionModel> {
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
                val divisions: MutableList<DivisionModel> = mutableListOf()
                try {
                    val nameKey = if (SettingsModel.isSqlServerWebDb) {
                        "div_newname"
                    } else {
                        "div_name"
                    }
                    val dbResult = SQLServerWrapper.getListOf(
                        "division",
                        "",
                        mutableListOf(
                            "*"
                        ),
                        "div_name not IN (select div_parent from division)"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            divisions.add(DivisionModel().apply {
                                divisionId = it.getStringValue("div_name")
                                divisionName = it.getStringValue(nameKey)
                            })
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return divisions
            }
        }
    }

    override suspend fun getSizeById(sizeId: String): String? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                // nothing to do
                return null
            }

            else -> {
                if (SettingsModel.isSqlServerWebDb) {
                    var sizeName: String? = null
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "st_itemsize",
                            "",
                            mutableListOf(
                                "sz_name"
                            ),
                            "sz_id='$sizeId'"
                        )
                        dbResult?.let {
                            if (it.next()) {
                                sizeName = it.getStringValue("sz_name")
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return sizeName
                } else {
                    return null
                }
            }
        }
    }

    override suspend fun getColorById(colorId: String): String? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                // nothing to do
                return null
            }

            else -> {
                if (SettingsModel.isSqlServerWebDb) {
                    var colorName: String? = null
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "st_itemcolor",
                            "",
                            mutableListOf(
                                "co_name"
                            ),
                            "co_id='$colorId'"
                        )
                        dbResult?.let {
                            if (it.next()) {
                                colorName = it.getStringValue("co_name")
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return colorName
                } else {
                    return null
                }
            }
        }
    }

    override suspend fun getBranchById(branchId: String): String? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                // nothing to do
                return null
            }

            else -> {
                if (SettingsModel.isSqlServerWebDb) {
                    var branchName: String? = null
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "branch",
                            "",
                            mutableListOf(
                                "bra_newname"
                            ),
                            "bra_name='$branchId'"
                        )
                        dbResult?.let {
                            if (it.next()) {
                                branchName = it.getStringValue("bra_newname")
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return branchName
                } else {
                    return null
                }
            }
        }
    }

    override suspend fun getUserPermissions(username: String): Map<String, String> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                // nothing to do
                return mutableMapOf()
            }

            else -> {
                val result = mutableMapOf<String, String>()
                if(username.isEmpty()){
                    return result
                }
                try {
                    if (SettingsModel.isSqlServerWebDb) {
                        val query =
                            "select * from set_groupsettings,set_users where gs_grp_desc=usr_grp_desc and usr_username='$username'"
                        SQLServerWrapper.getQueryResult(query)?.let {
                            while (it.next()) {
                                result[it.getStringValue("gs_lscode")] =
                                    it.getStringValue("gs_value")
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } else {
                        val query =
                            "select * from pay_groupssecurity,pay_employees where gs_grp_desc=emp_grp_desc and emp_username='$username'"
                        SQLServerWrapper.getQueryResult(query)?.let {
                            while (it.next()) {
                                result[it.getStringValue("gs_allow")] = "Yes"
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

}