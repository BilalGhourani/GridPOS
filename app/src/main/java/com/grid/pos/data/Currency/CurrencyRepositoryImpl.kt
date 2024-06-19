package com.grid.pos.data.Currency

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.data.User.User
import com.grid.pos.model.CONNECTION_TYPE
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
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("currency")
                    .whereEqualTo(
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
                currencies
            }

            CONNECTION_TYPE.LOCAL.key -> {
                currencyDao.getAllCurrencies(SettingsModel.getCompanyID() ?: "")
            }

            else -> {
                val where = "cur_cmp_id='${SettingsModel.getCompanyID()}'"
                val dbResult = SQLServerWrapper.getListOf(
                    "currency",
                    mutableListOf("*"),
                    where
                )
                val currencies: MutableList<Currency> = mutableListOf()
                dbResult.forEach { obj ->
                    currencies.add(Currency().apply {
                        currencyId = obj.optString("cur_code")
                        currencyCode1 = obj.optString("cur_newcode")
                        currencyName1 = obj.optString("cur_name")
                        currencyName1Dec = obj.optInt("cur_decimal")
                        currencyCode2 = obj.optString("cur_newcode")
                        currencyName2 = obj.optString("cur_name")
                        currencyName2Dec = obj.optInt("cur_decimal")
                        currencyRate = 1.0//obj.optDouble("cur_round")
                    })
                }
                currencies
            }
        }

    }
}