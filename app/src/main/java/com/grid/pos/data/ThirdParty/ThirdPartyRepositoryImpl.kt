package com.grid.pos.data.ThirdParty

import androidx.lifecycle.asLiveData
import com.grid.pos.data.Family.Family
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ThirdPartyRepositoryImpl(
    private val thirdPartyDao: ThirdPartyDao
) : ThirdPartyRepository {
    override suspend fun insert(thirdParty: ThirdParty, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("thirdParty")
            .add(thirdParty)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    thirdPartyDao.insert(thirdParty)
                    callback?.onSuccess(thirdParty)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }

    }

    override suspend fun delete(thirdParty: ThirdParty, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("thirdParty")
            .document(thirdParty.thirdPartyDocumentId!!)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    thirdPartyDao.delete(thirdParty)
                    callback?.onSuccess(thirdParty)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun update(thirdParty: ThirdParty, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("thirdParty")
            .document(thirdParty.thirdPartyDocumentId!!)
            .update(thirdParty.getMap())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    thirdPartyDao.update(thirdParty)
                    callback?.onSuccess(thirdParty)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun getThirdPartyById(id: String): ThirdParty {
        return thirdPartyDao.getThirdPartyById(id)
    }

    override suspend fun getAllThirdParties(callback: OnResult?) {
        val localThirdParties = thirdPartyDao.getAllThirdParties().asLiveData().value
        if (!localThirdParties.isNullOrEmpty()) {
            callback?.onSuccess(localThirdParties)
        }
        FirebaseFirestore.getInstance().collection("thirdParty").get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    val thirdParties = mutableListOf<ThirdParty>()
                    thirdPartyDao.deleteAll()
                    for (document in result) {
                        val obj = document.toObject(ThirdParty::class.java)
                        if (!obj.thirdPartyId.isNullOrEmpty()) {
                            obj.thirdPartyDocumentId = document.id
                            thirdParties.add(obj)
                        }
                    }
                    thirdPartyDao.insertAll(thirdParties.toList())
                    callback?.onSuccess(thirdParties)
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get families from remote."
                )
            }
    }


}