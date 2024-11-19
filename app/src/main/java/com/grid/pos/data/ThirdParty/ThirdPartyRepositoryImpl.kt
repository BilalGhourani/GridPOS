package com.grid.pos.data.ThirdParty

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.Timestamp
import java.util.Date

class ThirdPartyRepositoryImpl(
        private val thirdPartyDao: ThirdPartyDao
) : ThirdPartyRepository {
    override suspend fun insert(thirdParty: ThirdParty): ThirdParty {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("thirdParty")
                    .add(thirdParty.getMap()).await()
                thirdParty.thirdPartyDocumentId = docRef.id
            }

            CONNECTION_TYPE.LOCAL.key -> {
                thirdPartyDao.insert(thirdParty)
            }

            else -> {
                thirdParty.thirdPartyId = insertByProcedure(thirdParty)
            }
        }
        return thirdParty
    }

    override suspend fun delete(thirdParty: ThirdParty) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                thirdParty.thirdPartyDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("thirdParty").document(it).delete()
                        .await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                thirdPartyDao.delete(thirdParty)
            }

            else -> {
                deleteByProcedure(thirdParty)
            }
        }
    }

    override suspend fun update(
            thirdpartyId: String,
            thirdParty: ThirdParty
    ) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                thirdParty.thirdPartyDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("thirdParty").document(it)
                        .update(thirdParty.getMap()).await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                thirdPartyDao.update(thirdParty)
            }

            else -> {
                updateByProcedure(
                    thirdpartyId,
                    thirdParty
                )
            }
        }
    }

    override suspend fun getAllThirdParties(): MutableList<ThirdParty> {
        return when (SettingsModel.connectionType) {
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
                thirdParties
            }

            CONNECTION_TYPE.LOCAL.key -> {
                thirdPartyDao.getAllThirdParties(SettingsModel.getCompanyID() ?: "")
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
                    dbResult?.let {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                thirdParties
            }
        }

    }

    override suspend fun getAllThirdParties(types: List<String>): MutableList<ThirdParty> {
        return when (SettingsModel.connectionType) {
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
                thirdParties
            }

            CONNECTION_TYPE.LOCAL.key -> {
                thirdPartyDao.getAllThirdParties(
                    types,
                    SettingsModel.getCompanyID() ?: ""
                )
            }

            else -> {
                val thirdParties: MutableList<ThirdParty> = mutableListOf()
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "tp_cse IN (${types.joinToString(",")}) AND tp_cmp_id='${SettingsModel.getCompanyID()}'" else "tp_cse IN (${types.joinToString(",")})"
                    val dbResult = SQLServerWrapper.getListOf(
                        "thirdparty",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                thirdParties
            }
        }

    }

    override suspend fun getThirdPartyByID(thirdpartyId: String): ThirdParty? {
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
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return thirdPartyDao.getThirdPartyByID(thirdpartyId)
            }

            else -> {
                try {
                    val where = " tp_id='$thirdpartyId'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "thirdparty",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            return ThirdParty().apply {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }

    override suspend fun getOneThirdPartyByCompanyID(companyId: String): ThirdParty? {
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
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return thirdPartyDao.getOneThirdPartyByCompanyID(companyId)
            }

            else -> {
                return null
            }
        }
    }

    override suspend fun getOneThirdPartyByUserID(userId: String): ThirdParty? {
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
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return thirdPartyDao.getOneThirdPartyByUserID(userId)
            }

            else -> {
                return null
            }
        }
    }

    override suspend fun getDefaultThirdParty(): ThirdParty? {
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
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                thirdPartyDao.getDefaultThirdParties(SettingsModel.getCompanyID() ?: "")
            }

            else -> {
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "tp_cse in ('Receivable','Payable and Receivable') AND tp_cmp_id='${SettingsModel.getCompanyID()}'" else "tp_cse in ('Receivable','Payable and Receivable')"
                    val dbResult = SQLServerWrapper.getListOf(
                        "thirdparty",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            return ThirdParty().apply {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
        return null
    }

    private fun insertByProcedure(thirdParty: ThirdParty): String {
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
        return SQLServerWrapper.executeProcedure(
            "addthirdparty",
            parameters
        ) ?: ""
    }

    private fun updateByProcedure(
            thirdpartyId: String,
            thirdParty: ThirdParty
    ): String {
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
        return SQLServerWrapper.executeProcedure(
            "updthirdparty",
            parameters
        ) ?: ""
    }

    private fun deleteByProcedure(thirdParty: ThirdParty): String {
        return SQLServerWrapper.executeProcedure(
            "delthirdparty",
            listOf(thirdParty.thirdPartyId)
        ) ?: ""
    }


}