package com.grid.pos.data.ThirdParty

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.POSUtils
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
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("thirdParty")
                .add(thirdParty.getMap()).await()
            thirdParty.thirdPartyDocumentId = docRef.id
        } else {
            thirdPartyDao.insert(thirdParty)
        }
        return thirdParty
    }

    override suspend fun delete(thirdParty: ThirdParty) {
        if (SettingsModel.isConnectedToFireStore()) {
            thirdParty.thirdPartyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("thirdParty").document(it).delete()
                    .await()
            }
        } else {
            thirdPartyDao.delete(thirdParty)
        }
    }

    override suspend fun update(thirdParty: ThirdParty) {
        if (SettingsModel.isConnectedToFireStore()) {
            thirdParty.thirdPartyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("thirdParty").document(it)
                    .update(thirdParty.getMap()).await()
            }
        } else {
            thirdPartyDao.update(thirdParty)
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

    /*
    *
    *    @tp_name
  ,@tp_cse
  ,@tp_reference
  ,@tp_tpc_name
  ,@tp_fn
  ,@tp_disc
  ,@tp_contact
  ,@tp_phone1
  ,@tp_phone2
  ,@tp_phone3
  ,@tp_fax
  ,@tp_address
  ,@tp_activity
  ,@tp_web
  ,@tp_email
  ,@tp_gender
  ,@tp_date
  ,@tp_photo
  ,@tp_pathtodoc
  ,@tp_note
  ,@tp_cur_code
  ,@tp_cha_ch_code
  ,@tp_cha_code
  ,@tp_cha_ch_codetax
  ,@tp_cha_codetax
  ,@tp_tpl_name
  ,@tp_pln_name
  ,@tp_city
  ,@tp_street
  ,@tp_building
  ,@tp_floor
  ,@TPReferenceStartNumber
  ,@tp_tpd_id
  ,@tp_daystopay
  ,@tp_daystoorder
  ,@tp_maxdueamt
  ,@tp_tp_name
  ,@tp_userstamp
  ,@tp_displayname
  ,@tp_wa_name
  ,@tp_prj_name
  ,@tp_div_name
  ,@tp_bra_name
    * */
    private fun insertByProcedure(thirdParty: ThirdParty): String {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                thirdParty.thirdPartyName,//tp_name
                "Payable",//tp_cse
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
                null,//tp_email
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
                SettingsModel.defaultWarehouse,//@tp_wa_name
                null,//@tp_prj_name
                null,//@tp_div_name
                SettingsModel.defaultBranch,//@tp_bra_name
            )
        } else {
            listOf(
                thirdParty.thirdPartyName,//tp_name
                "Payable",//tp_cse
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
                null,//tp_email
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
                SettingsModel.defaultWarehouse,//@tp_wa_name
                null,//@tp_prj_name
                null,//@tp_div_name
                SettingsModel.defaultBranch,//@tp_bra_name
            )
        }
        return SQLServerWrapper.executeProcedure(
            "addthirdparty",
            parameters
        ) ?: ""
    }


}