package com.grid.pos.data.currency

import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.FirebaseWrapper
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getIntValue
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class CurrencyRepositoryImpl(
        private val currencyDao: CurrencyDao
) : CurrencyRepository {
    override suspend fun insert(currency: Currency): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.insert(
                "currency",
                currency
            )
        } else {
            currencyDao.insert(currency)
            DataModel(currency)
        }
    }

    override suspend fun delete(
            currency: Currency
    ): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.delete(
                "currency",
                currency
            )
        } else {
            currencyDao.delete(currency)
            DataModel(currency)
        }
    }

    override suspend fun update(
            currency: Currency
    ): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.update(
                "currency",
                currency
            )
        } else {
            currencyDao.update(currency)
            DataModel(currency)
        }
    }

    override suspend fun getAllCurrencies(): MutableList<Currency> {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "currency",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "cur_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val currencies = mutableListOf<Currency>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
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
                val currency = Currency()
                try {
                    val companyID = SettingsModel.getCompanyID()
                    val where = if (SettingsModel.isSqlServerWebDb) {
                        "cur_cmp_id='$companyID'"
                    } else {
                        "(cur_order='1' OR cur_order = '2')"
                    }

                    val dbResult = SQLServerWrapper.getListOf(
                        "currency",
                        "TOP 2",
                        mutableListOf("*"),
                        where,
                        "ORDER BY cur_order ASC"
                    )
                    currency.currencyCompId = companyID
                    dbResult?.let {
                        while (it.next()) {
                            if (it.getIntValue("cur_order") == 1) {
                                currency.currencyId = it.getStringValue("cur_code")
                                currency.currencyCode1 = if (SettingsModel.isSqlServerWebDb) it.getStringValue("cur_newcode") else it.getStringValue("cur_code")
                                currency.currencyName1 = it.getStringValue("cur_name")
                                currency.currencyName1Dec = it.getIntValue("cur_decimal")
                            } else {
                                currency.currencyDocumentId = it.getStringValue("cur_code")
                                currency.currencyCode2 = if (SettingsModel.isSqlServerWebDb) it.getStringValue("cur_newcode") else it.getStringValue("cur_code")
                                currency.currencyName2 = it.getStringValue("cur_name")
                                currency.currencyName2Dec = it.getIntValue("cur_decimal")
                            }
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (currency.currencyId.isNotEmpty() && !currency.currencyDocumentId.isNullOrEmpty()) {
                    currency.currencyRate = getRate(
                        currency.currencyId,
                        currency.currencyDocumentId!!
                    )
                }

                mutableListOf(currency)
            }
        }
    }

    override suspend fun getRate(
            firstCurr: String,
            secondCurr: String
    ): Double {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return SettingsModel.currentCurrency?.currencyRate ?: 1.0
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return SettingsModel.currentCurrency?.currencyRate ?: 1.0
            }

            else -> {
                var result = 1.0
                val timestamp = Timestamp.valueOf(
                    DateHelper.getDateInFormat(
                        Date(),
                        "yyyy-MM-dd HH:mm:ss"
                    )
                )
                val rateDbResult = SQLServerWrapper.selectFromProcedure(
                    "getrate",
                    listOf(
                        "'${firstCurr}'",
                        "'${secondCurr}'",
                        "'$timestamp'"
                    )
                )
                try {
                    rateDbResult?.let {
                        if (it.next()) {
                            result = it.getDoubleValue("getrate")
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                result
            }
        }
    }

    override suspend fun getAllCurrencyModels(): MutableList<CurrencyModel> {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.LOCAL.key, CONNECTION_TYPE.FIRESTORE.key -> {
                val currencies = mutableListOf<CurrencyModel>()
                SettingsModel.currentCurrency?.let {
                    if (!it.currencyCode1.isNullOrEmpty()) {
                        currencies.add(
                            CurrencyModel(
                                it.currencyCode1!!,
                                it.currencyCode1!!,
                                it.currencyName1!!
                            )
                        )
                    }
                    if (!it.currencyCode2.isNullOrEmpty()) {
                        currencies.add(
                            CurrencyModel(
                                it.currencyCode2!!,
                                it.currencyCode2!!,
                                it.currencyName2!!
                            )
                        )
                    }
                }
                currencies
            }

            else -> {
                val currencyModels = mutableListOf<CurrencyModel>()
                try {
                    val companyID = SettingsModel.getCompanyID()
                    val where = if (SettingsModel.isSqlServerWebDb) {
                        "cur_cmp_id='$companyID'"
                    } else ""

                    val dbResult = SQLServerWrapper.getListOf(
                        "currency",
                        "",
                        mutableListOf("*"),
                        where,
                        "ORDER BY cur_order ASC"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            currencyModels.add(
                                CurrencyModel(
                                    currencyId = it.getStringValue("cur_code"),
                                    currencyCode = if (SettingsModel.isSqlServerWebDb) it.getStringValue("cur_newcode") else it.getStringValue("cur_code"),
                                    currencyName = it.getStringValue("cur_name")
                                )
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return currencyModels
            }
        }
    }
}