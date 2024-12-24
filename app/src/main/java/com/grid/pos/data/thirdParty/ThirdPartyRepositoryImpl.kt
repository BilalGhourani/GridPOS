package com.grid.pos.data.thirdParty

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class ThirdPartyRepositoryImpl(
        private val thirdPartyDao: ThirdPartyDao
) : ThirdPartyRepository {
    override suspend fun insert(thirdParty: ThirdParty): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("thirdParty")
                    .add(thirdParty.getMap()).await()
                thirdParty.thirdPartyDocumentId = docRef.id
                return DataModel(thirdParty)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                thirdPartyDao.insert(thirdParty)
                return DataModel(thirdParty)
            }

            else -> {
                return insertByProcedure(thirdParty)
            }
        }
    }

    override suspend fun delete(thirdParty: ThirdParty): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                thirdParty.thirdPartyDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("thirdParty").document(it).delete()
                        .await()
                    return DataModel(thirdParty)
                }
                return DataModel(
                    thirdParty,
                    false
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                thirdPartyDao.delete(thirdParty)
                return DataModel(thirdParty)
            }

            else -> {
                return deleteByProcedure(thirdParty)
            }
        }
    }

    override suspend fun update(
            thirdpartyId: String,
            thirdParty: ThirdParty
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                thirdParty.thirdPartyDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("thirdParty").document(it)
                        .update(thirdParty.getMap()).await()
                    return DataModel(thirdParty)
                }
                return DataModel(
                    thirdParty,
                    false
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                thirdPartyDao.update(thirdParty)
                return DataModel(thirdParty)
            }

            else -> {
                return updateByProcedure(
                    thirdpartyId,
                    thirdParty
                )
            }
        }
    }

    override suspend fun getAllThirdParties(): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("thirdParty")
                    .whereEqualTo(
                        "tp_cmp_id",
                        SettingsModel.getCompanyID()
                    ).get().await()
                val thirdParties = mutableListOf<ThirdParty>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(ThirdParty::class.java)
                        if (obj.thirdPartyId.isNotEmpty()) {
                            obj.thirdPartyDocumentId = document.id
                            thirdParties.add(obj)
                        }
                    }
                }
                return DataModel(thirdParties)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(thirdPartyDao.getAllThirdParties(SettingsModel.getCompanyID() ?: ""))
            }

            else -> {
                val thirdParties: MutableList<ThirdParty> = mutableListOf()
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "tp_cse = 'Receivable' AND tp_cmp_id='${SettingsModel.getCompanyID()}'" else "tp_cse = 'Receivable'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "thirdparty",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            val isDefaultOneFound = false
                            while (it.next()) {
                                thirdParties.add(ThirdParty().apply {
                                    thirdPartyId = it.getStringValue("tp_name")
                                    thirdPartyName = if (SettingsModel.isSqlServerWebDb) it.getStringValue("tp_newname") else it.getStringValue(
                                        "tp_name"
                                    )
                                    thirdPartyFn = it.getStringValue("tp_fn")
                                    thirdPartyCompId = if (SettingsModel.isSqlServerWebDb) it.getStringValue("tp_cmp_id") else SettingsModel.getCompanyID()
                                    thirdPartyType = it.getStringValue("tp_cse")
                                    thirdPartyPhone1 = it.getStringValue("tp_phone1")
                                    thirdPartyPhone2 = it.getStringValue("tp_phone2")
                                    thirdPartyAddress = it.getStringValue("tp_address")
                                    val timeStamp = it.getObjectValue("tp_timestamp")
                                    thirdPartyTimeStamp = when (timeStamp) {
                                        is Date -> timeStamp
                                        is String -> DateHelper.getDateFromString(
                                            timeStamp,
                                            "yyyy-MM-dd hh:mm:ss.SSS"
                                        )

                                        else -> null
                                    }
                                    thirdPartyDateTime = (thirdPartyTimeStamp ?: Date()).time
                                    thirdPartyUserStamp = it.getStringValue("tp_userstamp")
                                    thirdPartyDefault = !isDefaultOneFound && thirdPartyName.equals(
                                        "cash",
                                        ignoreCase = true
                                    )
                                })
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } else {
                        return DataModel(
                            null,
                            false,
                            dbResult.result as? String
                        )
                    }
                    return DataModel(thirdParties)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return DataModel(
                        null,
                        false,
                        e.message
                    )
                }
            }
        }
    }

    override suspend fun getAllThirdParties(types: List<String>): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("thirdParty")
                    .whereEqualTo(
                        "tp_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereIn(
                        "tp_cse",
                        types
                    ).get().await()
                val thirdParties = mutableListOf<ThirdParty>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(ThirdParty::class.java)
                        if (obj.thirdPartyId.isNotEmpty()) {
                            obj.thirdPartyDocumentId = document.id
                            thirdParties.add(obj)
                        }
                    }
                }
                return DataModel(thirdParties)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(
                    thirdPartyDao.getAllThirdParties(
                        types,
                        SettingsModel.getCompanyID() ?: ""
                    )
                )
            }

            else -> {
                val thirdParties: MutableList<ThirdParty> = mutableListOf()
                try {
                    val typeStr = types.joinToString(",") { "'$it'" }
                    val where = if (SettingsModel.isSqlServerWebDb) "tp_cse IN ($typeStr) AND tp_cmp_id='${SettingsModel.getCompanyID()}'" else "tp_cse IN ($typeStr)"
                    val dbResult = SQLServerWrapper.getListOf(
                        "thirdparty",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            val isDefaultOneFound = false
                            while (it.next()) {
                                thirdParties.add(ThirdParty().apply {
                                    thirdPartyId = it.getStringValue("tp_name")
                                    thirdPartyName = if (SettingsModel.isSqlServerWebDb) it.getStringValue("tp_newname") else it.getStringValue(
                                        "tp_name"
                                    )
                                    thirdPartyFn = it.getStringValue("tp_fn")
                                    thirdPartyCompId = if (SettingsModel.isSqlServerWebDb) it.getStringValue("tp_cmp_id") else SettingsModel.getCompanyID()
                                    thirdPartyType = it.getStringValue("tp_cse")
                                    thirdPartyPhone1 = it.getStringValue("tp_phone1")
                                    thirdPartyPhone2 = it.getStringValue("tp_phone2")
                                    thirdPartyAddress = it.getStringValue("tp_address")
                                    val timeStamp = it.getObjectValue("tp_timestamp")
                                    thirdPartyTimeStamp = when (timeStamp) {
                                        is Date -> timeStamp
                                        is String -> DateHelper.getDateFromString(
                                            timeStamp,
                                            "yyyy-MM-dd hh:mm:ss.SSS"
                                        )

                                        else -> null
                                    }
                                    thirdPartyDateTime = (thirdPartyTimeStamp ?: Date()).time
                                    thirdPartyUserStamp = it.getStringValue("tp_userstamp")
                                    thirdPartyDefault = !isDefaultOneFound && thirdPartyName.equals(
                                        "cash",
                                        ignoreCase = true
                                    )
                                })
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } else {
                        return DataModel(
                            null,
                            false,
                            dbResult.result as? String
                        )
                    }
                    return DataModel(thirdParties)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return DataModel(
                        null,
                        false,
                        e.message
                    )
                }
            }
        }
    }

    override suspend fun getThirdPartyByID(thirdpartyId: String): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("thirdParty")
                    .whereEqualTo(
                        "tp_id",
                        thirdpartyId
                    ).limit(1).get().await()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(ThirdParty::class.java)
                        if (obj.thirdPartyId.isNotEmpty()) {
                            obj.thirdPartyDocumentId = document.id
                            return DataModel(obj)
                        }
                    }
                }
                return DataModel(null)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(thirdPartyDao.getThirdPartyByID(thirdpartyId))
            }

            else -> {
                try {
                    var thirdParty: ThirdParty? = null
                    val where = " tp_id='$thirdpartyId'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "thirdparty",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            while (it.next()) {
                                thirdParty = ThirdParty().apply {
                                    thirdPartyId = it.getStringValue("tp_name")
                                    thirdPartyName = if (SettingsModel.isSqlServerWebDb) it.getStringValue("tp_newname") else it.getStringValue(
                                        "tp_name"
                                    )
                                    thirdPartyFn = it.getStringValue("tp_fn")
                                    thirdPartyCompId = if (SettingsModel.isSqlServerWebDb) it.getStringValue("tp_cmp_id") else SettingsModel.getCompanyID()
                                    thirdPartyType = it.getStringValue("tp_cse")
                                    thirdPartyPhone1 = it.getStringValue("tp_phone1")
                                    thirdPartyPhone2 = it.getStringValue("tp_phone2")
                                    thirdPartyAddress = it.getStringValue("tp_address")
                                    val timeStamp = it.getObjectValue("tp_timestamp")
                                    thirdPartyTimeStamp = when (timeStamp) {
                                        is Date -> timeStamp
                                        is String -> DateHelper.getDateFromString(
                                            timeStamp,
                                            "yyyy-MM-dd hh:mm:ss.SSS"
                                        )

                                        else -> null
                                    }
                                    thirdPartyDateTime = (thirdPartyTimeStamp ?: Date()).time
                                    thirdPartyUserStamp = it.getStringValue("tp_userstamp")
                                    thirdPartyDefault = true
                                }
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } else {
                        return DataModel(
                            null,
                            false,
                            dbResult.result as? String
                        )
                    }
                    return DataModel(thirdParty)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return DataModel(
                        null,
                        false,
                        e.message
                    )
                }
            }
        }
    }

    override suspend fun getOneThirdPartyByCompanyID(companyId: String): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("thirdParty")
                    .whereEqualTo(
                        "tp_cmp_id",
                        companyId
                    ).limit(1).get().await()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(ThirdParty::class.java)
                        if (obj.thirdPartyId.isNotEmpty()) {
                            obj.thirdPartyDocumentId = document.id
                            return DataModel(obj)
                        }
                    }
                }
                return DataModel(null)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(thirdPartyDao.getOneThirdPartyByCompanyID(companyId))
            }

            else -> {
                return DataModel(null)
            }
        }
    }

    override suspend fun getOneThirdPartyByUserID(userId: String): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("thirdParty")
                    .whereEqualTo(
                        "tp_userstamp",
                        userId
                    ).limit(1).get().await()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(ThirdParty::class.java)
                        if (obj.thirdPartyId.isNotEmpty()) {
                            obj.thirdPartyDocumentId = document.id
                            return DataModel(obj)
                        }
                    }
                }
                return DataModel(null)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(thirdPartyDao.getOneThirdPartyByUserID(userId))
            }

            else -> {
                return DataModel(null)
            }
        }
    }

    override suspend fun getDefaultThirdParty(): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("thirdParty")
                    .whereEqualTo(
                        "tp_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereEqualTo(
                        "tp_default",
                        true
                    ).limit(1).get().await()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(ThirdParty::class.java)
                        if (obj.thirdPartyId.isNotEmpty()) {
                            obj.thirdPartyDocumentId = document.id
                            return DataModel(obj)
                        }
                    }
                }
                return DataModel(null)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(thirdPartyDao.getDefaultThirdParties(SettingsModel.getCompanyID() ?: ""))
            }

            else -> {
                try {
                    var thirdParty: ThirdParty? = null
                    val where = if (SettingsModel.isSqlServerWebDb) "tp_cse in ('Receivable','Payable and Receivable') AND tp_cmp_id='${SettingsModel.getCompanyID()}'" else "tp_cse in ('Receivable','Payable and Receivable')"
                    val dbResult = SQLServerWrapper.getListOf(
                        "thirdparty",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            while (it.next()) {
                                thirdParty = ThirdParty().apply {
                                    thirdPartyId = it.getStringValue("tp_name")
                                    thirdPartyName = if (SettingsModel.isSqlServerWebDb) it.getStringValue("tp_newname") else it.getStringValue(
                                        "tp_name"
                                    )
                                    thirdPartyFn = it.getStringValue("tp_fn")
                                    thirdPartyCompId = if (SettingsModel.isSqlServerWebDb) it.getStringValue("tp_cmp_id") else SettingsModel.getCompanyID()
                                    thirdPartyType = it.getStringValue("tp_cse")
                                    thirdPartyPhone1 = it.getStringValue("tp_phone1")
                                    thirdPartyPhone2 = it.getStringValue("tp_phone2")
                                    thirdPartyAddress = it.getStringValue("tp_address")
                                    val timeStamp = it.getObjectValue("tp_timestamp")
                                    thirdPartyTimeStamp = when (timeStamp) {
                                        is Date -> timeStamp
                                        is String -> DateHelper.getDateFromString(
                                            timeStamp,
                                            "yyyy-MM-dd hh:mm:ss.SSS"
                                        )

                                        else -> null
                                    }
                                    thirdPartyDateTime = (thirdPartyTimeStamp ?: Date()).time
                                    thirdPartyUserStamp = it.getStringValue("tp_userstamp")
                                    thirdPartyDefault = true
                                }
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } else {
                        return DataModel(
                            null,
                            false,
                            dbResult.result as? String
                        )
                    }
                    return DataModel(thirdParty)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return DataModel(
                        null,
                        false,
                        e.message
                    )
                }
            }
        }
    }

    private fun insertByProcedure(thirdParty: ThirdParty): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//tp_name null_string_output
                thirdParty.thirdPartyType,//tp_cse
                null,//tp_reference
                null,//tp_tpc_name
                thirdParty.thirdPartyFn,//tp_fn
                null,//tp_disc
                null,//tp_contact
                thirdParty.thirdPartyPhone1,//tp_phone1
                thirdParty.thirdPartyPhone2,//tp_phone2
                null,//tp_phone3
                null,//tp_fax
                thirdParty.thirdPartyAddress,//tp_address
                null,//tp_activity
                null,//tp_web
                null,//tp_email
                null,//tp_gender
                Timestamp(System.currentTimeMillis()),//tp_date
                null,//tp_photo
                null,//tp_pathtodoc
                null,//tp_note
                null,//tp_cur_code
                null,//tp_cha_ch_code
                null,//@tp_cha_code
                null,//@tp_cha_ch_codetax
                null,//@tp_cha_codetax
                null,//@tp_tpl_name
                null,//@tp_pln_name
                null,//@tp_city
                null,//@tp_street
                null,//@tp_building
                null,//@tp_floor
                null,//@TPReferenceStartNumber
                null,//@tp_tpd_id
                null,//@tp_daystopay
                null,//@tp_daystoorder
                null,//@tp_maxdueamt
                null,//@tp_tp_name
                SettingsModel.currentUser?.userUsername,//@tp_userstamp
                thirdParty.thirdPartyName,//@tp_newname
                SettingsModel.defaultSqlServerWarehouse,//@tp_wa_name
                null,//@tp_prj_name
                null,//@tp_div_name
                SettingsModel.defaultSqlServerBranch,//@tp_bra_name
                SettingsModel.getCompanyID(),
                null
            )
        } else {
            listOf(
                thirdParty.thirdPartyName,//tp_name
                thirdParty.thirdPartyType,//tp_cse
                null,//tp_reference
                null,//tp_tpc_name
                thirdParty.thirdPartyFn,//tp_fn
                null,//tp_disc
                null,//tp_contact
                thirdParty.thirdPartyPhone1,//tp_phone1
                thirdParty.thirdPartyPhone2,//tp_phone2
                null,//tp_phone3
                null,//tp_fax
                thirdParty.thirdPartyAddress,//tp_address
                null,//tp_activity
                null,//tp_web
                null,//tp_email
                null,//tp_gender
                Timestamp(System.currentTimeMillis()),//tp_date
                null,//tp_photo
                null,//tp_pathtodoc
                null,//tp_note
                null,//tp_cur_code
                null,//tp_cha_ch_code
                null,//@tp_cha_code
                null,//@tp_cha_ch_codetax
                null,//@tp_cha_codetax
                null,//@tp_tpl_name
                null,//@tp_pln_name
                null,//@tp_city
                null,//@tp_street
                null,//@tp_building
                null,//@tp_floor
                null,//@TPReferenceStartNumber
                null,//@tp_tpd_id
                null,//@tp_daystopay
                null,//@tp_daystoorder
                null,//@tp_maxdueamt
                null,//@tp_tp_name
                SettingsModel.currentUser?.userUsername,//@tp_userstamp
                null,//@tp_displayname
                SettingsModel.defaultSqlServerWarehouse,//@tp_wa_name
                null,//@tp_prj_name
                null,//@tp_div_name
                SettingsModel.defaultSqlServerBranch,//@tp_bra_name
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addthirdparty",
            parameters
        )
        return if (queryResult.succeed) {
            thirdParty.thirdPartyId = (queryResult.result as? String) ?: ""
            DataModel(thirdParty)
        } else {
            DataModel(
                thirdParty,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun updateByProcedure(
            thirdpartyId: String,
            thirdParty: ThirdParty
    ): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                thirdParty.thirdPartyId,//tp_name
                thirdParty.thirdPartyType,//tp_cse
                null,//tp_reference
                null,//tp_tpc_name
                thirdParty.thirdPartyFn,//tp_fn
                null,//tp_disc
                null,//tp_contact
                thirdParty.thirdPartyPhone1,//tp_phone1
                thirdParty.thirdPartyPhone2,//tp_phone2
                null,//tp_phone3
                null,//tp_fax
                thirdParty.thirdPartyAddress,//tp_address
                null,//tp_activity
                null,//tp_web
                null,//tp_email
                null,//tp_gender
                Timestamp(System.currentTimeMillis()),//tp_date
                null,//tp_photo
                null,//tp_pathtodoc
                null,//tp_note
                null,//tp_cur_code
                null,//tp_cha_ch_code
                null,//@tp_cha_code
                null,//@tp_cha_ch_codetax
                null,//@tp_cha_codetax
                null,//@tp_tpl_name
                null,//@tp_pln_name
                null,//@tp_city
                null,//@tp_street
                null,//@tp_building
                null,//@tp_floor
                null,//@tp_tpd_id
                null,//@tp_daystopay
                null,//@tp_daystoorder
                null,//@tp_maxdueamt
                null,//@tp_tp_name
                SettingsModel.currentUser?.userUsername,//@tp_userstamp
                thirdParty.thirdPartyName,//@tp_newname
                SettingsModel.defaultSqlServerWarehouse,//@tp_wa_name
                null,//@tp_prj_name
                null,//@tp_div_name
                SettingsModel.defaultSqlServerBranch,//@tp_bra_name
                SettingsModel.getCompanyID(),
                null
            )
        } else {
            listOf(
                thirdpartyId,//oldtp_name
                thirdParty.thirdPartyType,//tp_cse
                thirdParty.thirdPartyName,//tp_name
                null,//tp_reference
                null,//tp_tpc_name
                thirdParty.thirdPartyFn,//tp_fn
                null,//tp_disc
                null,//tp_contact
                thirdParty.thirdPartyPhone1,//tp_phone1
                thirdParty.thirdPartyPhone2,//tp_phone2
                null,//tp_phone3
                null,//tp_fax
                thirdParty.thirdPartyAddress,//tp_address
                null,//tp_activity
                null,//tp_web
                null,//tp_email
                null,//tp_gender
                Timestamp(System.currentTimeMillis()),//tp_date
                null,//tp_photo
                null,//tp_pathtodoc
                null,//tp_note
                null,//tp_cur_code
                null,//tp_cha_ch_code
                null,//@tp_cha_code
                null,//@tp_cha_ch_codetax
                null,//@tp_cha_codetax
                null,//@tp_tpl_name
                null,//@tp_pln_name
                null,//@tp_city
                null,//@tp_street
                null,//@tp_building
                null,//@tp_floor
                null,//@tp_tpd_id
                null,//@tp_daystopay
                null,//@tp_daystoorder
                null,//@tp_maxdueamt
                null,//@tp_tp_name
                SettingsModel.currentUser?.userUsername,//@tp_userstamp
                null,//@tp_displayname
                SettingsModel.defaultSqlServerWarehouse,//@tp_wa_name
                null,//@tp_prj_name
                null,//@tp_div_name
                SettingsModel.defaultSqlServerBranch,//@tp_bra_name
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updthirdparty",
            parameters
        )
        return if (queryResult.succeed) {
            DataModel(thirdParty)
        } else {
            DataModel(
                thirdParty,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun deleteByProcedure(thirdParty: ThirdParty): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delthirdparty",
            listOf(thirdParty.thirdPartyId)
        )
        return if (queryResult.succeed) {
            DataModel(thirdParty)
        } else {
            DataModel(
                thirdParty,
                false,
                queryResult.result as? String
            )
        }
    }


}