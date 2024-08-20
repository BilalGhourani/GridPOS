package com.grid.pos.data.ThirdParty

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.tasks.await
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
                val where =
                    if (SettingsModel.isSqlServerWebDb) "tp_cmp_id='${SettingsModel.getCompanyID()}'" else ""
                val dbResult = SQLServerWrapper.getListOf(
                    "thirdparty",
                    "",
                    mutableListOf("*"),
                    where
                )
                val thirdParties: MutableList<ThirdParty> = mutableListOf()
                dbResult.forEach { obj ->
                    thirdParties.add(ThirdParty().apply {
                        thirdPartyId = obj.optString("tp_name")
                        thirdPartyName =
                            if (SettingsModel.isSqlServerWebDb) obj.optString("tp_newname") else obj.optString(
                                "tp_name"
                            )
                        thirdPartyFn = obj.optString("tp_fn")
                        thirdPartyCompId =
                            if (SettingsModel.isSqlServerWebDb) obj.optString("tp_cmp_id") else SettingsModel.getCompanyID()
                        thirdPartyPhone1 = obj.optString("tp_phone1")
                        thirdPartyPhone2 = obj.optString("tp_phone2")
                        thirdPartyAddress = obj.optString("tp_address")
                        val timeStamp = obj.opt("tp_timestamp")
                        thirdPartyTimeStamp =
                            if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                                timeStamp as String,
                                "yyyy-MM-dd hh:mm:ss.SSS"
                            )
                        thirdPartyDateTime = thirdPartyTimeStamp!!.time
                        thirdPartyUserStamp = obj.optString("tp_userstamp")
                    })
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


}