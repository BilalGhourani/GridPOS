package com.grid.pos.data.Family

import androidx.lifecycle.asLiveData
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FamilyRepositoryImpl(
    private val familyDao: FamilyDao
) : FamilyRepository {
    override suspend fun insert(family: Family, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("family")
            .add(family)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    familyDao.insert(family)
                    callback?.onSuccess(family)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }

    }

    override suspend fun delete(family: Family, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("family")
            .document(family.familyDocumentId!!)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    familyDao.delete(family)
                    callback?.onSuccess(family)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun update(family: Family, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("family")
            .document(family.familyDocumentId!!)
            .update(family.getMap())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    familyDao.update(family)
                    callback?.onSuccess(family)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun getFamilyById(id: String): Family {
        return familyDao.getFamilyById(id)
    }

    override fun getAllFamilies(callback: OnResult?) {
        val localFamilies = familyDao.getAllFamilies().asLiveData().value
        if (!localFamilies.isNullOrEmpty()) {
            callback?.onSuccess(localFamilies)
        }
        FirebaseFirestore.getInstance().collection("family").get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    val families = mutableListOf<Family>()
                    familyDao.deleteAll()
                    for (document in result) {
                        val obj = document.toObject(Family::class.java)
                        if (!obj.familyId.isNullOrEmpty()) {
                            obj.familyDocumentId = document.id
                            families.add(obj)
                        }
                    }
                    familyDao.insertAll(families.toList())
                    callback?.onSuccess(families)
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get families from remote."
                )
            }
    }

}