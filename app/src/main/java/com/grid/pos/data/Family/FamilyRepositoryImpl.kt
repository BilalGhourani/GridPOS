package com.grid.pos.data.Family

import com.google.firebase.firestore.FirebaseFirestore
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
                FirebaseFirestore.getInstance().collection("st_family")
                    .document(it).delete().await()
            }
        } else {
            familyDao.delete(family)
        }
    }

    override suspend fun update(family: Family) {
        if (SettingsModel.isConnectedToFireStore()) {
            family.familyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("st_family")
                    .document(it).update(family.getMap()).await()
            }

        } else {
            familyDao.update(family)
        }
    }

    override suspend fun getFamilyById(id: String): Family {
        return familyDao.getFamilyById(id)
    }

    override suspend fun getAllFamilies(): MutableList<Family> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("st_family")
                .whereEqualTo(
                    "fa_cmp_id",
                    SettingsModel.companyID
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
            return families
        } else {
            return familyDao.getAllFamilies()
        }
    }

}