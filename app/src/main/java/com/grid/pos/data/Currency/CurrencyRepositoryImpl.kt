package com.grid.pos.data.Currency

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

class CurrencyRepositoryImpl(
        private val currencyDao: CurrencyDao
) : CurrencyRepository {
    override suspend fun insert(currency: Currency): Currency {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("currency").add(currency)
                .await()
            currency.currencyDocumentId = docRef.id
        } else {
            currencyDao.insert(currency)
        }
        return currency
    }

    override suspend fun delete(
            currency: Currency
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            currency.currencyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("currency").document(it).delete().await()
            }
        } else {
            currencyDao.delete(currency)
        }
    }

    override suspend fun update(
            currency: Currency
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            currency.currencyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("currency").document(it)
                    .update(currency.getMap()).await()
            }
        } else {
            currencyDao.update(currency)
        }
    }

    override suspend fun getAllCurrencies(): MutableList<Currency> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("currency").whereEqualTo(
                "cur_cmp_id",
                SettingsModel.getCompanyID()
            ).get().await()
            val currencies = mutableListOf<Currency>()
            if (querySnapshot.size() > 0) {
                for (document in querySnapshot) {
                    val obj = document.toObject(Currency::class.java)
                    if (obj.currencyId.isNotEmpty()) {
                        obj.currencyDocumentId = document.id
                        currencies.add(obj)
                    }
                }
            }
            return currencies
        } else {
            return currencyDao.getAllCurrencies(SettingsModel.getCompanyID() ?: "")
        }

    }
}