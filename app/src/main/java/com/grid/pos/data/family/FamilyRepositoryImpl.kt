package com.grid.pos.data.family

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet

class FamilyRepositoryImpl(
        private val familyDao: FamilyDao
) : FamilyRepository {
    override suspend fun insert(family: Family): Family {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("st_family").add(family)
                    .await()
                family.familyDocumentId = docRef.id
            }

            CONNECTION_TYPE.LOCAL.key -> {
                familyDao.insert(family)
            }

            else -> {
                insertByProcedure(family)
            }
        }
        return family
    }

    override suspend fun delete(family: Family) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                family.familyDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("st_family").document(it).delete()
                        .await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                familyDao.delete(family)
            }

            else -> {
                deleteByProcedure(family)
            }
        }
    }

    override suspend fun update(family: Family) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                family.familyDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("st_family").document(it)
                        .update(family.getMap()).await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                familyDao.update(family)
            }

            else -> {
                updateFamily(family)
            }
        }
    }

    override suspend fun getAllFamilies(): MutableList<Family> {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("st_family")
                    .whereEqualTo(
                        "fa_cmp_id",
                        SettingsModel.getCompanyID()
                    ).get().await()
                val families = mutableListOf<Family>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Family::class.java)
                        if (obj.familyId.isNotEmpty()) {
                            obj.familyDocumentId = document.id
                            families.add(obj)
                        }
                    }
                }
                families
            }

            CONNECTION_TYPE.LOCAL.key -> {
                familyDao.getAllFamilies(SettingsModel.getCompanyID() ?: "")
            }

            else -> {
                val families: MutableList<Family> = mutableListOf()
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "fa_cmp_id='${SettingsModel.getCompanyID()}'" else ""
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_family",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            while (it.next()) {
                                families.add(Family().apply {
                                    familyId = it.getStringValue("fa_name")
                                    familyName = if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_newname") else it.getStringValue(
                                        "fa_name"
                                    )
                                    //familyImage = obj.optString("fa_name")
                                    familyCompanyId = if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_cmp_id") else SettingsModel.getCompanyID()
                                })
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                families
            }
        }
    }

    override suspend fun getOneFamily(companyId: String): Family? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("st_family")
                    .whereEqualTo(
                        "fa_cmp_id",
                        companyId
                    ).limit(1).get().await()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Family::class.java)
                        if (obj.familyId.isNotEmpty()) {
                            obj.familyDocumentId = document.id
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return familyDao.getOneFamily(companyId)
            }

            else -> {
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "fa_cmp_id='$companyId'" else ""
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_family",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            if (it.next()) {
                                return Family().apply {
                                    familyId = it.getStringValue("fa_name")
                                    familyName = if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_newname") else it.getStringValue(
                                        "fa_name"
                                    )
                                    //familyImage = obj.optString("fa_name")
                                    familyCompanyId = if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_cmp_id") else SettingsModel.getCompanyID()
                                }
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }

    private fun insertByProcedure(family: Family) {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//@fa_name
                null,//@fa_parent
                null,//@fa_group
                null,//@fa_numberofyears
                null,//@fa_codelen
                null,//@fa_purchacc
                null,//@fa_depacc
                null,//@fa_depchargeacc
                null,//@fa_salesacc
                null,//@fa_costoffasoldacc
                null,//@fa_periodicityofprovision
                family.familyName,//@fa_newname
                SettingsModel.getCompanyID(),//@fa_cmp_id
                "null_string_output"//@output_message
            )
        } else {
            listOf(
                family.familyName,//@fa_name
                null,//@fa_parent
                null,//@fa_group
                null,//@fa_numberofyears
                null,//@fa_codelen
                null,//@fa_purchacc
                null,//@fa_depacc
                null,//@fa_depchargeacc
                null,//@fa_salesacc
                null,//@fa_costoffasoldacc
                null,//@fa_periodicityofprovision
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addst_family",
            parameters
        )
        if (!queryResult.succeed) return
        val id = queryResult.result as? String
        if (id.isNullOrEmpty() && SettingsModel.isSqlServerWebDb) {
            try {
                val dbResult = SQLServerWrapper.getQueryResult("select max(fa_name) as id from st_item")
                (dbResult.result as? ResultSet)?.let {
                    while (it.next()) {
                        family.familyId = it.getStringValue(
                            "id",
                            family.familyId
                        )
                    }
                    SQLServerWrapper.closeResultSet(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (!id.isNullOrEmpty()) {
            family.familyId = id
        }
    }

    private fun updateFamily(
            family: Family
    ) {
        val columnName = if (SettingsModel.isSqlServerWebDb) "fa_newname" else "fa_name"
        SQLServerWrapper.update(
            "st_family",
            listOf(
                columnName
            ),
            listOf(
                family.familyName
            ),
            "fa_name = '${family.familyId}'"
        )
    }

    private fun deleteByProcedure(family: Family) {
        SQLServerWrapper.executeProcedure(
            "delst_family",
            listOf(
                family.familyId
            )
        )
    }

}