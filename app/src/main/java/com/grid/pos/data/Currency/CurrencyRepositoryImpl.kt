package com.grid.pos.data.Currency

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.tasks.await
import java.sql.Timestamp
import java.util.Date

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
                val companyID = SettingsModel.getCompanyID()
                val where = if (SettingsModel.isSqlServerWebDb) {
                    "cur_cmp_id='$companyID' ORDER BY cur_order ASC"
                } else {
                    "(cur_order='1' OR cur_order = '2') ORDER BY cur_order ASC"
                }

                val dbResult = SQLServerWrapper.getListOf(
                    "currency",
                    "TOP 2",
                    mutableListOf("*"),
                    where
                )
                val currency = Currency()
                currency.currencyCompId = companyID
                dbResult.forEach { obj ->
                    if (obj.optInt("cur_order") == 1) {
                        currency.currencyId = obj.optString("cur_code")
                        currency.currencyCode1 = if(SettingsModel.isSqlServerWebDb) obj.optString("cur_newcode") else obj.optString("cur_code")
                        currency.currencyName1 = obj.optString("cur_name")
                        currency.currencyName1Dec = obj.optInt("cur_decimal")
                    } else {
                        currency.currencyDocumentId = obj.optString("cur_code")
                        currency.currencyCode2 = if(SettingsModel.isSqlServerWebDb) obj.optString("cur_newcode") else obj.optString("cur_code")
                        currency.currencyName2 = obj.optString("cur_name")
                        currency.currencyName2Dec = obj.optInt("cur_decimal")
                    }
                }

                /*val rateWhere =  "rate_cur_code1 = '${currency.currencyId}' AND rate_cur_code2 = '${currency.currencyDocumentId}' ORDER BY rate_date DESC"

                val rateDbResult = SQLServerWrapper.getListOf(
                    "crate",
                    "TOP 1",
                    mutableListOf("rate_rate"),
                    rateWhere
                )*/
                if (currency.currencyId.isNotEmpty() && !currency.currencyDocumentId.isNullOrEmpty()) {
                   val timestamp =  Timestamp.valueOf(
                       DateHelper.getDateInFormat(
                           Date(),
                           "yyyy-MM-dd HH:mm:ss"
                       )
                   )
                    val rateDbResult = SQLServerWrapper.executeProcedure(
                        "getrate",
                        listOf(
                            "'${currency.currencyId}'",
                            "'${currency.currencyDocumentId!!}'",
                            "'$timestamp'"
                        )
                    )
                    if (rateDbResult.isNotEmpty()) {
                        currency.currencyRate = rateDbResult[0].optDouble("getrate")
                    }
                }

                mutableListOf(currency)
            }
        }

    }
}