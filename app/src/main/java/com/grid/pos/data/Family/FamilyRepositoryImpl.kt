package com.grid.pos.data.Family

import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.Currency.Currency
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel

class FamilyRepositoryImpl(
    private val familyDao: FamilyDao
) : FamilyRepository {
    override suspend fun insert(family: Family, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("family")
                .add(family)
                .addOnSuccessListener {
                    family.familyDocumentId = it.id
                    callback?.onSuccess(family)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            familyDao.insert(family)
            callback?.onSuccess(family)
        }

    }

    override suspend fun delete(family: Family, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("family")
                .document(family.familyDocumentId!!)
                .delete()
                .addOnSuccessListener {
                    callback?.onSuccess(family)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            familyDao.delete(family)
            callback?.onSuccess(family)
        }
    }

    override suspend fun update(family: Family, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("family")
                .document(family.familyDocumentId!!)
                .update(family.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(family)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            familyDao.update(family)
            callback?.onSuccess(family)
        }
    }

    override suspend fun getFamilyById(id: String): Family {
        return familyDao.getFamilyById(id)
    }

    override suspend fun getAllFamilies(callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("family").get()
                .addOnSuccessListener { result ->
                    val families = mutableListOf<Family>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(Family::class.java)
                            if (!obj.familyId.isNullOrEmpty()) {
                                obj.familyDocumentId = document.id
                                families.add(obj)
                            }
                        }
                    }
                    callback?.onSuccess(families)
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get families from remote."
                    )
                }
        } else {
            familyDao.getAllFamilies().collect {
                callback?.onSuccess(it)
            }
        }
    }

}