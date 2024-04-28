package com.grid.pos.data.Currency

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.grid.pos.data.DataModel
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import org.jetbrains.annotations.NotNull

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
    var currencyDocumentId: String? = null,

    /**
     * Currency company Id
     * */
    @ColumnInfo(name = "cur_cmp_id")
    @set:PropertyName("cur_cmp_id")
    @get:PropertyName("cur_cmp_id")
    var currencyCompId: String?=null,

    /**
     * Currency code 1
     * */
    @ColumnInfo(name = "cur_code1")
    @set:PropertyName("cur_code1")
    @get:PropertyName("cur_code1")
    var currencyCode1: String?=null,

    /**
     * Currency name 1
     * */
    @ColumnInfo(name = "cur_name1")
    @set:PropertyName("cur_name1")
    @get:PropertyName("cur_name1")
    var currencyName1: String?=null,


    /**
     * Currency Code 2
     * */
    @ColumnInfo(name = "cur_code2")
    @set:PropertyName("cur_code2")
    @get:PropertyName("cur_code2")
    var currencyCode2: String?=null,

    /**
     * Currency Name 2
     * */
    @ColumnInfo(name = "cur_name2")
    @set:PropertyName("cur_name2")
    @get:PropertyName("cur_name2")
    var currencyName2: String?=null,

    /**
     * Currency Rate
     * */
    @ColumnInfo(name = "cur_rate")
    @set:PropertyName("cur_rate")
    @get:PropertyName("cur_rate")
    var currencyRate: String?=null,
    ): DataModel() {
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
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "cur_cmp_id" to currencyCompId,
            "cur_code1" to currencyCode1,
            "cur_name1" to currencyName1,
            "cur_code2" to currencyCode2,
            "cur_name2" to currencyName2,
            "cur_rate" to currencyRate,
        )
    }
}
