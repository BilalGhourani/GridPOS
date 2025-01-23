package com.grid.pos.data.stockHeadInOut.header

import com.google.firebase.firestore.Exclude
import com.grid.pos.data.EntityModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import java.util.Date

data class StockHeaderInOut(
    /**
     * hio_id
     * */
    var stockHeadInOutId: String,

    /**
     * hio_no
     * */
    var stockHeadInOutNo: String? = null,

    /**
     * hio_cmp_id
     * */
    var stockHeadInOutCmpId: String? = null,

    /**
     * hio_wa_name
     * */
    var stockHeadInOutWaName: String? = null,

    /**
     * hio_inout
     * */
    var stockHeadInOutInOut: String? = null,

    /**
     * hio_type
     * */
    var stockHeadInOutType: String? = null,

    /**
     * hio_wa_tp_name
     * */
    var stockHeadInOutWaTpName: String? = null,


    /*
    * hio_date
    */
    var stockHeadInOutDate: String = DateHelper.getDateInFormat(),

    /*
    * hio_tt_code
    */
    var stockHeadInOutTtCode: String? = null,

    /*
    * tt_newcode
    */
    var stockHeadInOutTtCodeName: String? = null,

    /*
    * hio_transno
    */
    var stockHeadInOutTransNo: String? = null,

    /*
    * hio_desc
    */
    var stockHeadInOutDesc: String? = null,

    /*
    * hio_prj_name
    */
    var stockHeadInOutPrjName: String? = null,

    /*
    * hio_bra_name
    */
    var stockHeadInOutBraName: String? = null,

    /*
    * hio_note
    */
    var stockHeadInOutNote: String? = null,


    /**
     * hio_timestamp
     * */
    var stockHeadInOutTimeStamp: Date? = null,

    /**
     * hio_userstamp
     * */
    var stockHeadInOutUserStamp: String? = null,

    /*
    * hio_valuedate
    */
    var stockHeadInOutValueDate: Date? = null,

    /*
    * hio_hj_no
    * */
    var stockHeadInOutHjNo: String? = null,

    ) : EntityModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return stockHeadInOutId
    }

    @Exclude
    override fun getName(): String {
        return "${stockHeadInOutTtCodeName ?: ""}${stockHeadInOutTransNo ?: ""}"
    }

    @Exclude
    override fun isNew(): Boolean {
        return stockHeadInOutId.isEmpty()
    }

    @Exclude
    override fun prepareForInsert() {
        stockHeadInOutInOut = "Out"
        stockHeadInOutType = "Inter-Warehouse"
        stockHeadInOutCmpId = SettingsModel.getCompanyID()
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
    fun didChanged(stockHeadInOut: StockHeaderInOut): Boolean {
        return !stockHeadInOut.stockHeadInOutNote.equals(stockHeadInOutNote) ||
                !stockHeadInOut.stockHeadInOutDesc.equals(stockHeadInOutDesc) ||
                stockHeadInOut.stockHeadInOutInOut != stockHeadInOutInOut ||
                stockHeadInOut.stockHeadInOutWaName != stockHeadInOutWaName ||
                stockHeadInOut.stockHeadInOutWaTpName != stockHeadInOutWaTpName
    }
}
