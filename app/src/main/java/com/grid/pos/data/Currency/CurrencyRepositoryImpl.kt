package com.grid.pos.data.Currency

import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.Company.Company
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel

class CurrencyRepositoryImpl(
    private val currencyDao: CurrencyDao
) : CurrencyRepository {
    override suspend fun insert(currency: Currency, callback: OnResult?) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("currency")
                .add(currency)
                .addOnSuccessListener {
                    currency.currencyDocumentId = it.id
                    callback?.onSuccess(currency)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            currencyDao.insert(currency)
            callback?.onSuccess(currency)
        }
    }

    override suspend fun delete(currency: Currency, callback: OnResult?) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("currency")
                .document(currency.currencyDocumentId!!)
                .delete()
                .addOnSuccessListener {
                    callback?.onSuccess(currency)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            currencyDao.delete(currency)
            callback?.onSuccess(currency)
        }
    }

    override suspend fun update(currency: Currency, callback: OnResult?) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("currency")
                .document(currency.currencyDocumentId!!)
                .update(currency.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(currency)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            currencyDao.update(currency)
            callback?.onSuccess(currency)
        }
    }

    override suspend fun getCurrencyById(id: String): Currency {
        return currencyDao.getCurrencyById(id)
    }

    override suspend fun getAllCurrencies(callback: OnResult?) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("currency")
                .whereEqualTo("cur_cmp_id",SettingsModel.companyID)
                .get()
                .addOnSuccessListener { result ->
                    val currencies = mutableListOf<Currency>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(Currency::class.java)
                            if (obj.currencyId.isNotEmpty()) {
                                obj.currencyDocumentId = document.id
                                currencies.add(obj)
                            }
                        }
                    }
                    callback?.onSuccess(currencies)
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get currencies from remote."
                    )
                }
        } else {
            currencyDao.getAllCurrencies().collect {
                callback?.onSuccess(it)
            }
        }

    }
}