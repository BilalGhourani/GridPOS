package com.grid.pos.data.stockInOut

import com.google.firebase.firestore.Exclude
import com.grid.pos.data.EntityModel
import java.util.Date

data class StockInOut(
    /**
     * io_id
     * */
    var stockInOutId: String,

    /**
     * io_hio_id
     * */
    var stockInOutHeaderId: String? = null,

    /**
     * io_it_id
     * */
    var stockInOutItemId: String? = null,

    /**
     * io_qty
     * */
    var stockInOutQty: Double = 0.0,

    /**
     * io_type
     * */
    var stockInOutType: String? = null,

    /**
     * io_wa_tp_name
     * */
    var stockInOutWaTpName: String? = null,

    /**
     * io_remqtywa
     * */
    var stockInOutRemQtyWa: Double = 0.0,


    /*
    * io_it_idinpack
    */
    var stockInOutItemIdInPack: String? = null,

    /*
    * io_qtyinpack
    */
    var stockInOutQtyInPack: Double = 0.0,

    /*
    * io_note
    */
    var stockInOutNote: String? = null,


    /**
     * io_timestamp
     * */
    var stockInOutTimeStamp: Date? = null,

    /**
     * io_userstamp
     * */
    var stockInOutUserStamp: String? = null,

    /*
    * io_div_name
    */
    var stockInOutDivName: String? = null,

    /*
    * io_remqtywahio
    * */
    var stockInOutQtyWaHio: Double = 0.0,

    /**
     * io_cost
     * */
    var stockInOutCost: Double = 0.0,

    /**
     * io_mcurratef
     * */
    var stockInOutCurRateF: Double = 0.0,

    /**
     * io_mcurrates
     * */
    var stockInOutCurRateS: Double = 0.0,

    /**
     * io_order
     * */
    var stockInOutOrder: Int? = null,

    ) : EntityModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return stockInOutId
    }

    @Exclude
    override fun getName(): String {
        return ""
    }

    @Exclude
    override fun isNew(): Boolean {
        return stockInOutId.isEmpty()
    }

    @Exclude
    override fun prepareForInsert() {
        stockInOutType = "Inter-Warehouse"
    }

    @Exclude
    override fun setDocumentId(documentId: String) {

    }

    @Exclude
    override fun getDocumentId(): String? {
        return null
    }

    @Exclude
    override fun getMap(): Map<String, Any?> {
        return mapOf()
    }


    @Exclude
    fun didChanged(stockinout: StockInOut): Boolean {
        return !stockinout.stockInOutNote.equals(stockInOutNote) ||
                !stockinout.stockInOutQty.equals(stockInOutQty) ||
                stockinout.stockInOutQtyInPack != stockInOutQtyInPack ||
                stockinout.stockInOutRemQtyWa != stockInOutRemQtyWa ||
                stockinout.stockInOutQtyWaHio != stockInOutQtyWaHio
    }
}
