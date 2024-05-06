package com.grid.pos.data.ThirdParty

import androidx.lifecycle.asLiveData
import com.grid.pos.data.Family.Family
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ThirdPartyRepositoryImpl(
    private val thirdPartyDao: ThirdPartyDao
) : ThirdPartyRepository {
    override suspend fun insert(thirdParty: ThirdParty, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("thirdParty")
                .add(thirdParty)
                .addOnSuccessListener {
                    thirdParty.thirdPartyDocumentId = it.id
                    callback?.onSuccess(thirdParty)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            thirdPartyDao.insert(thirdParty)
            callback?.onSuccess(thirdParty)
        }

    }

    override suspend fun delete(thirdParty: ThirdParty, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("thirdParty")
                .document(thirdParty.thirdPartyDocumentId!!)
                .delete()
                .addOnSuccessListener {
                    callback?.onSuccess(thirdParty)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            thirdPartyDao.delete(thirdParty)
            callback?.onSuccess(thirdParty)
        }
    }

    override suspend fun update(thirdParty: ThirdParty, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("thirdParty")
                .document(thirdParty.thirdPartyDocumentId!!)
                .update(thirdParty.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(thirdParty)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            thirdPartyDao.update(thirdParty)
            callback?.onSuccess(thirdParty)
        }
    }

    override suspend fun getThirdPartyById(id: String): ThirdParty {
        return thirdPartyDao.getThirdPartyById(id)
    }

    override suspend fun getAllThirdParties(callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("thirdParty")
                .whereEqualTo("tp_cmp_id",SettingsModel.companyID)
                .get()
                .addOnSuccessListener { result ->
                    val thirdParties = mutableListOf<ThirdParty>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(ThirdParty::class.java)
                            if (!obj.thirdPartyId.isNullOrEmpty()) {
                                obj.thirdPartyDocumentId = document.id
                                thirdParties.add(obj)
                            }
                        }
                    }
                    callback?.onSuccess(thirdParties)
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get Third Parties from remote."
                    )
                }
        } else {
            thirdPartyDao.getAllThirdParties().collect {
                callback?.onSuccess(it)
            }
        }

    }


}