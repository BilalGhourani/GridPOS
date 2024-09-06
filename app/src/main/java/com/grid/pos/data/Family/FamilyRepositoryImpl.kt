package com.grid.pos.data.Family

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

class FamilyRepositoryImpl(
        private val familyDao: FamilyDao
) : FamilyRepository {
    override suspend fun insert(family: Family): Family {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("st_family").add(family).await()
            family.familyDocumentId = docRef.id
        } else {
            familyDao.insert(family)
        }
        return family
    }

    override suspend fun delete(family: Family) {
        if (SettingsModel.isConnectedToFireStore()) {
            family.familyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("st_family").document(it).delete()
                    .await()
            }
        } else {
            familyDao.delete(family)
        }
    }

    override suspend fun update(family: Family) {
        if (SettingsModel.isConnectedToFireStore()) {
            family.familyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("st_family").document(it)
                    .update(family.getMap()).await()
            }

        } else {
            familyDao.update(family)
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
                    dbResult?.let {
                        while (it.next()) {
                            families.add(Family().apply {
                                familyId = it.getString("fa_name")
                                familyName = if (SettingsModel.isSqlServerWebDb) it.getString("fa_newname") else it.getString(
                                    "fa_name"
                                )
                                //familyImage = obj.optString("fa_name")
                                familyCompanyId = if (SettingsModel.isSqlServerWebDb) it.getString("fa_cmp_id") else SettingsModel.getCompanyID()
                            })
                        }
                        SQLServerWrapper.closeResultSet(it)
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
                    dbResult?.let {
                        if (it.next()) {
                            return Family().apply {
                                familyId = it.getString("fa_name")
                                familyName = if (SettingsModel.isSqlServerWebDb) it.getString("fa_newname") else it.getString(
                                    "fa_name"
                                )
                                //familyImage = obj.optString("fa_name")
                                familyCompanyId = if (SettingsModel.isSqlServerWebDb) it.getString("fa_cmp_id") else SettingsModel.getCompanyID()
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

}