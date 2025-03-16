package com.grid.pos.data.purchase

import com.google.firebase.firestore.Exclude
import com.grid.pos.data.EntityModel
import java.util.Date

data class Purchase(
    /**
     * pu_id
     */
    var purchaseId: String = "",

    /**
     * pu_hp_id
     */
    var purchaseHpId: String? = null,

    /**
     * pu_it_id
     */
    var purchaseItId: String? = null,

    /**
     * pu_qty
     */
    var purchaseQty: Double? = null,

    /**
     * pu_price
     */
    var purchasePrice: Double? = null,

    /**
     * pu_disc
     */
    var purchaseDisc: Double? = null,

    /**
     * pu_discamt
     */
    var purchaseDiscAmt: Double? = null,

    /**
     * pu_wa_name
     */
    var purchaseWaName: String? = null,

    /**
     * pu_note
     */
    var purchaseNote: String? = null,

    /**
     * pu_it_idinpack
     */
    var purchaseItIdInPack: String? = null,

    /**
     * pu_qtyinpack
     */
    var purchaseQtyInPack: Double? = null,

    /**
     * pu_cost
     */
    var purchaseCost: Double? = null,

    /**
     * pu_mcurrate
     */
    var purchaseMcurRate: Double? = null,

    /**
     * pu_mcurratef
     */
    var purchaseMcurRateF: Double? = null,

    /**
     * pu_mcurrates
     */
    var purchaseMcurRateS: Double? = null,

    /**
     * pu_remqty
     */
    var purchaseRemQty: Double? = null,

    /**
     * pu_remqtywa
     */
    var purchaseRemQtyWa: Double? = null,

    /**
     * frompu_id
     */
    var purchaseFromPuId: String? = null,

    /**
     * pu_timestamp
     */
    var purchaseTimestamp: Date? = null,

    /**
     * pu_userstamp
     */
    var purchaseUserStamp: String? = null,

    /**
     * pu_div_name
     */
    var purchaseDivName: String? = null,

    /**
     * pu_qtyratio
     */
    var purchaseQtyRatio: Double? = null,

    /**
     * pu_packs
     */
    var purchasePacks: Double? = null,

    /**
     * pu_hsid
     */
    var purchaseHsId: String? = null,

    /**
     * pu_vat
     */
    var purchaseVat: Double? = null,

    /**
     * pu_order
     */
    var purchaseOrder: Int? = null,

    /**
     * pu_tax1
     */
    var purchaseTax1: Double? = null,

    /**
     * pu_tax2
     */
    var purchaseTax2: Double? = null,

    /**
     * pu_un_id
     */
    var purchaseUnId: String? = null,

    /**
     * pu_lineno
     */
    var purchaseLineNo: Int? = null
) : EntityModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return purchaseId
    }

    @Exclude
    override fun getName(): String {
        return ""
    }

    @Exclude
    override fun isNew(): Boolean {
        return purchaseId.isEmpty()
    }

    @Exclude
    override fun prepareForInsert() {

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
    fun didChanged(purchase: Purchase): Boolean {
        return !purchase.purchaseNote.equals(purchaseNote) ||
                purchase.purchaseQty != purchaseQty ||
                purchase.purchaseQtyInPack != purchaseQtyInPack ||
                purchase.purchaseRemQtyWa != purchaseRemQtyWa ||
                purchase.purchaseDisc != purchaseDisc ||
                purchase.purchaseDiscAmt != purchaseDiscAmt ||
                purchase.purchasePrice != purchasePrice ||
                purchase.purchaseCost != purchaseCost
    }
}
