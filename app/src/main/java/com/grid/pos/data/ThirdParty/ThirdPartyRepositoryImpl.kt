package com.grid.pos.data.ThirdParty

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

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
                FirebaseFirestore.getInstance().collection("thirdParty")
                    .document(it).delete().await()
            }
        } else {
            thirdPartyDao.delete(thirdParty)
        }
    }

    override suspend fun update(thirdParty: ThirdParty) {
        if (SettingsModel.isConnectedToFireStore()) {
            thirdParty.thirdPartyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("thirdParty")
                    .document(it).update(thirdParty.getMap()).await()
            }
        } else {
            thirdPartyDao.update(thirdParty)
        }
    }

    override suspend fun getThirdPartyById(id: String): ThirdParty {
        return thirdPartyDao.getThirdPartyById(id)
    }

    override suspend fun getAllThirdParties(): MutableList<ThirdParty> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("thirdParty")
                .whereEqualTo(
                    "tp_cmp_id",
                    SettingsModel.companyID
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
            return thirdParties
        } else {
            return thirdPartyDao.getAllThirdParties()
        }
    }


}