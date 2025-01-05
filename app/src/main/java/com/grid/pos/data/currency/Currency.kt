package com.grid.pos.data.currency

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.grid.pos.data.EntityModel
import com.grid.pos.data.item.Item
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils

@Entity(tableName = "currency")
data class Currency(
        /**
         * Currency Id
         * */
        @PrimaryKey
        @ColumnInfo(name = "cur_id")
        @set:PropertyName("cur_id")
        @get:PropertyName("cur_id")
        var currencyId: String,

        @Ignore
        @get:Exclude
        var currencyDocumentId: String? = null,//second curr id for sql server

        /**
         * Currency company Id
         * */
        @ColumnInfo(name = "cur_cmp_id")
        @set:PropertyName("cur_cmp_id")
        @get:PropertyName("cur_cmp_id")
        var currencyCompId: String? = null,

        /**
         * Currency code 1
         * */
        @ColumnInfo(name = "cur_code1")
        @set:PropertyName("cur_code1")
        @get:PropertyName("cur_code1")
        var currencyCode1: String? = null,

        /**
         * Currency name 1
         * */
        @ColumnInfo(name = "cur_name1")
        @set:PropertyName("cur_name1")
        @get:PropertyName("cur_name1")
        var currencyName1: String? = null,

        /**
         * Currency Code 2
         * */
        @ColumnInfo(name = "cur_code2")
        @set:PropertyName("cur_code2")
        @get:PropertyName("cur_code2")
        var currencyCode2: String? = null,

        /**
         * Currency Name 2
         * */
        @ColumnInfo(name = "cur_name2")
        @set:PropertyName("cur_name2")
        @get:PropertyName("cur_name2")
        var currencyName2: String? = null,

        /**
         * Currency Name 1 Decimal
         * */
        @ColumnInfo(name = "cur_name1decimal")
        @set:PropertyName("cur_name1decimal")
        @get:PropertyName("cur_name1decimal")
        var currencyName1Dec: Int = 2,

        /**
         * Currency Name 2 Decimal
         * */
        @ColumnInfo(name = "cur_name2decimal")
        @set:PropertyName("cur_name2decimal")
        @get:PropertyName("cur_name2decimal")
        var currencyName2Dec: Int = 2,

        /**
         * Currency Rate
         * */
        @ColumnInfo(name = "cur_rate")
        @set:PropertyName("cur_rate")
        @get:PropertyName("cur_rate")
        var currencyRate: Double = 1.0,
) : EntityModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return currencyId
    }

    @Exclude
    override fun getName(): String {
        return currencyName1 ?: ""
    }

    @Exclude
    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }

    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            currencyDocumentId.isNullOrEmpty()
        } else {
            currencyId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (currencyId.isEmpty()) {
            currencyId = Utils.generateRandomUuidString()
        }
        currencyCompId = SettingsModel.getCompanyID()
    }

    @Exclude
    fun didChanged(currency: Currency): Boolean {
        return !currency.currencyCode1.equals(currencyCode1) || !currency.currencyName1.equals(currencyName1) || !currency.currencyName1Dec.equals(currencyName1Dec) || !currency.currencyCode2.equals(currencyCode2) || !currency.currencyName2.equals(currencyName2) || !currency.currencyName2Dec.equals(currencyName2Dec) || !currency.currencyRate.equals(currencyRate)
    }

    @Exclude
    fun didChangedCurrencyCode(currency: Currency): Boolean {
        return !currency.currencyCode1.equals(currencyCode1) || !currency.currencyCode2.equals(currencyCode2)
                ||!currency.currencyName1.equals(currencyName1) || !currency.currencyName2.equals(currencyName2)
    }

    @Exclude
    fun getCurrencyCode(item: Item): String? {
        if (!item.itemCurrencyCode.isNullOrEmpty()) {
            return item.itemCurrencyCode
        }
        if (item.itemCurrencyId.isNullOrEmpty()) {
            return currencyCode1
        }
        if (item.itemCurrencyId == currencyCode1 || item.itemCurrencyId == currencyId) {
            return currencyCode1
        } else if (item.itemCurrencyId == currencyCode2 || item.itemCurrencyId == currencyDocumentId) {
            return currencyCode2
        }
        return currencyCode1
    }

    @Exclude
    override fun setDocumentId(documentId: String) {
        currencyDocumentId = documentId
    }

    @Exclude
    override fun getDocumentId(): String? {
        return currencyDocumentId
    }

    @Exclude
    override fun getMap(): Map<String, Any?> {
        return mapOf(
            "cur_cmp_id" to currencyCompId,
            "cur_code1" to currencyCode1,
            "cur_name1" to currencyName1,
            "cur_code2" to currencyCode2,
            "cur_name2" to currencyName2,
            "cur_name1decimal" to currencyName1Dec,
            "cur_name2decimal" to currencyName2Dec,
            "cur_rate" to currencyRate,
        )
    }
}
