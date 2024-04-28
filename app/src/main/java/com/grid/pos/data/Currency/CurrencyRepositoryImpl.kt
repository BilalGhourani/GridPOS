package com.grid.pos.data.Currency

import androidx.lifecycle.asLiveData
import com.grid.pos.data.Company.Company
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CurrencyRepositoryImpl(
    private val currencyDao: CurrencyDao
) : CurrencyRepository {
    override suspend fun insert(currency: Currency, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("currency")
            .add(currency)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    currencyDao.insert(currency)
                    currency.currencyDocumentId = it.id
                    callback?.onSuccess(currency)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun delete(currency: Currency, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("currency")
            .document(currency.currencyDocumentId!!)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    currencyDao.delete(currency)
                    callback?.onSuccess(currency)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun update(currency: Currency, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("currency")
            .document(currency.currencyDocumentId!!)
            .update(currency.getMap())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    currencyDao.update(currency)
                    callback?.onSuccess(currency)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun getCurrencyById(id: String): Currency {
        return currencyDao.getCurrencyById(id)
    }

    override fun getAllCurrencies(callback: OnResult?) {
        val localCurrencies = currencyDao.getAllCurrencies().asLiveData().value
        if (!localCurrencies.isNullOrEmpty()) {
            callback?.onSuccess(localCurrencies)
        }
        FirebaseFirestore.getInstance().collection("currency").get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    val currencies = mutableListOf<Currency>()
                    currencyDao.deleteAll()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(Currency::class.java)
                            if (obj.currencyId.isNotEmpty()) {
                                obj.currencyDocumentId = document.id
                                currencies.add(obj)
                            }
                        }
                        currencyDao.insertAll(currencies.toList())
                    }
                    callback?.onSuccess(currencies)
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get currencies from remote."
                )
            }
    }
}