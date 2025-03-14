package com.grid.pos.data.purchase

import java.util.Date

data class Purchase(
    /**
     * pu_id
     */
    val purchaseId: String? = null,

    /**
     * pu_hp_id
     */
    val purchaseHpId: String? = null,

    /**
     * pu_it_id
     */
    val purchaseItId: String? = null,

    /**
     * pu_qty
     */
    val purchaseQty: Double? = null,

    /**
     * pu_price
     */
    val purchasePrice: Double? = null,

    /**
     * pu_disc
     */
    val purchaseDisc: Double? = null,

    /**
     * pu_discamt
     */
    val purchaseDiscAmt: Double? = null,

    /**
     * pu_wa_name
     */
    val purchaseWaName: String? = null,

    /**
     * pu_note
     */
    val purchaseNote: String? = null,

    /**
     * pu_it_idinpack
     */
    val purchaseItIdInPack: String? = null,

    /**
     * pu_qtyinpack
     */
    val purchaseQtyInPack: Double? = null,

    /**
     * pu_cost
     */
    val purchaseCost: Double? = null,

    /**
     * pu_mcurrate
     */
    val purchaseMcurRate: Double? = null,

    /**
     * pu_mcurratef
     */
    val purchaseMcurRateF: Double? = null,

    /**
     * pu_mcurrates
     */
    val purchaseMcurRateS: Double? = null,

    /**
     * pu_remqty
     */
    val purchaseRemQty: Double? = null,

    /**
     * pu_remqtywa
     */
    val purchaseRemQtyWa: Double? = null,

    /**
     * frompu_id
     */
    val purchaseFromPuId: String? = null,

    /**
     * pu_timestamp
     */
    val purchaseTimestamp: Date? = null,

    /**
     * pu_userstamp
     */
    val purchaseUserStamp: String? = null,

    /**
     * pu_div_name
     */
    val purchaseDivName: String? = null,

    /**
     * pu_qtyratio
     */
    val purchaseQtyRatio: Double? = null,

    /**
     * pu_packs
     */
    val purchasePacks: Double? = null,

    /**
     * pu_hsid
     */
    val purchaseHsId: String? = null,

    /**
     * pu_vat
     */
    val purchaseVat: Double? = null,

    /**
     * pu_order
     */
    val purchaseOrder: Int? = null,

    /**
     * pu_tax1
     */
    val purchaseTax1: Double? = null,

    /**
     * pu_tax2
     */
    val purchaseTax2: Double? = null,

    /**
     * pu_un_id
     */
    val purchaseUnId: String? = null,

    /**
     * pu_lineno
     */
    val purchaseLineNo: Int? = null
)
