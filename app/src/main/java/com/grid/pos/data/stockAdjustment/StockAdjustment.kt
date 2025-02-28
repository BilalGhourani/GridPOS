package com.grid.pos.data.stockAdjustment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.grid.pos.data.EntityModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils
import java.util.Date

@Entity(tableName = "st_stockadjustment")
data class StockAdjustment(
    /**
     * Stock Adjustment Id
     * */
    @PrimaryKey
    @ColumnInfo(name = "sa_id")
    @set:PropertyName("sa_id")
    @get:PropertyName("sa_id")
    var stockAdjId: String,

    @Ignore
    @get:Exclude
    var stockAdjDocumentId: String? = null,

    /**
     * related Invoice header id
     * */
    @ColumnInfo(name = "sa_hsa_id")
    @set:PropertyName("sa_hsa_id")
    @get:PropertyName("sa_hsa_id")
    var stockAdjHeaderId: String? = null,

    /**
     * Stock Adjustment item id
     * */
    @ColumnInfo(name = "sa_it_id")
    @set:PropertyName("sa_it_id")
    @get:PropertyName("sa_it_id")
    var stockAdjItemId: String? = null,

    /**
     * Stock Adjustment reason
     * */
    @ColumnInfo(name = "sa_reason")
    @set:PropertyName("sa_reason")
    @get:PropertyName("sa_reason")
    var stockAdjReason: String? = null,

    /**
     * Stock Adjustment warehouse
     * */
    @ColumnInfo(name = "sa_wa_name")
    @set:PropertyName("sa_wa_name")
    @get:PropertyName("sa_wa_name")
    var stockAdjWaName: String? = null,

    /**
     * Stock Adjustment Qty
     * */
    @ColumnInfo(name = "sa_qty")
    @set:PropertyName("sa_qty")
    @get:PropertyName("sa_qty")
    var stockAdjQty: Double = 0.0,

    @Ignore
    @get:Exclude
    var stockAdjPuId: String? = null,

    @Ignore
    @get:Exclude
    var stockAdjItemIdInPack: String? = null,

    @Ignore
    @get:Exclude
    var stockAdjQtyInPack: Double = 0.0,

    /**
     * Stock Adjustment cost
     * */
    @ColumnInfo(name = "sa_cost")
    @set:PropertyName("sa_cost")
    @get:PropertyName("sa_cost")
    var stockAdjCost: Double = 0.0,

    /**
     * Stock Adjustment currency rate first
     * */
    @ColumnInfo(name = "sa_mcurratef")
    @set:PropertyName("sa_mcurratef")
    @get:PropertyName("sa_mcurratef")
    var stockAdjCurrRateF: Double = 0.0,

    /**
     * Stock Adjustment currency rate second
     * */
    @ColumnInfo(name = "sa_mcurrates")
    @set:PropertyName("sa_mcurrates")
    @get:PropertyName("sa_mcurrates")
    var stockAdjCurrRateS: Double = 0.0,

    /**
     * Stock Adjustment  remaining Qty
     * */
    @ColumnInfo(name = "sa_remqty")
    @set:PropertyName("sa_remqty")
    @get:PropertyName("sa_remqty")
    var stockAdjRemQty: Double = 0.0,

    /**
     * Stock Adjustment remaining Qty in Warehouse
     * */
    @ColumnInfo(name = "sa_remqtywa")
    @set:PropertyName("sa_remqtywa")
    @get:PropertyName("sa_remqtywa")
    var stockAdjRemQtyWa: Double = 0.0,

    /**
     * Stock Adjustment timestamp
     * */
    @Ignore
    @set:PropertyName("sa_timestamp")
    @get:PropertyName("sa_timestamp")
    @ServerTimestamp
    var stockAdjTimeStamp: Date? = null,

    /**
     * Stock Adjustment user stamp
     * */
    @ColumnInfo(name = "sa_userstamp")
    @set:PropertyName("sa_userstamp")
    @get:PropertyName("sa_userstamp")
    var stockAdjUserStamp: String? = null,

    @Ignore
    @get:Exclude
    var stockAdjRowguid: String? = null,

    @Ignore
    @get:Exclude
    var stockAdjDivName: String? = null,

    /**
     * Stock Adjustment date
     * */
    @Ignore
    @set:PropertyName("sa_date")
    @get:PropertyName("sa_date")
    @ServerTimestamp
    var stockAdjDate: Date? = null,

    /**
     * Stock Adjustment order
     * */
    @ColumnInfo(name = "sa_order")
    @set:PropertyName("sa_order")
    @get:PropertyName("sa_order")
    var stockAdjOrder: Int? = null,

    /**
     * Stock Adjustment order
     * */
    @ColumnInfo(name = "sa_order")
    @set:PropertyName("sa_order")
    @get:PropertyName("sa_order")
    var stockAdjLineNo: Int? = null,

    ) : EntityModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return stockAdjId
    }

    @Exclude
    override fun getName(): String {
        return ""
    }

    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            stockAdjDocumentId.isNullOrEmpty()
        } else {
            stockAdjId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (stockAdjId.isEmpty()) {
            stockAdjId = Utils.generateRandomUuidString()
        }
        stockAdjUserStamp = SettingsModel.currentUser?.userId
    }

    @Exclude
    override fun setDocumentId(documentId: String) {
        stockAdjDocumentId = documentId
    }

    @Exclude
    override fun getDocumentId(): String? {
        return stockAdjDocumentId
    }

    @Exclude
    override fun getMap(): Map<String, Any?> {
        return mapOf()
    }


    @Exclude
    fun didChanged(stockAdjustment: StockAdjustment): Boolean {
        return !stockAdjustment.stockAdjReason.equals(stockAdjReason) ||
                !stockAdjustment.stockAdjQty.equals(stockAdjQty)
    }
}
