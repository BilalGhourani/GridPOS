package com.grid.pos.data.purchaseHeader

import java.util.Date

data class PurchaseHeader(
    /**
     * hp_id
     */
    var purchaseHeaderId: String ="",

    /**
     * hp_no
     */
    var purchaseHeaderNo: String? = null,

    /**
     * hp_cmp_id
     */
    var purchaseHeaderCmpId: String? = null,

    /**
     * hp_category
     */
    var purchaseHeaderCategory: String? = null,

    /**
     * hp_date
     */
    var purchaseHeaderDate: String? = null,

    /**
     * hp_orderno
     */
    var purchaseHeaderOrderNo: String? = null,

    /**
     * hp_tt_code
     */
    var purchaseHeaderTtCode: String? = null,

    /**
     * hp_tt_code
     */
    var purchaseHeaderTtCodeName: String? = null,

    /**
     * hp_transno
     */
    var purchaseHeaderTransNo: String? = null,

    /**
     * hp_status
     */
    var purchaseHeaderStatus: String? = null,

    /**
     * hp_pln_name
     */
    var purchaseHeaderPlnName: String? = null,

    /**
     * hp_cur_code
     */
    var purchaseHeaderCurCode: String? = null,

    /**
     * hp_disc
     */
    var purchaseHeaderDisc: Double? = null,

    /**
     * hp_discamt
     */
    var purchaseHeaderDiscAmt: Double? = null,

    /**
     * hp_wa_name
     */
    var purchaseHeaderWaName: String? = null,

    /**
     * hp_bra_name
     */
    var purchaseHeaderBraName: String? = null,

    /**
     * hp_prj_name
     */
    var purchaseHeaderPrjName: String? = null,

    /**
     * hp_note
     */
    var purchaseHeaderNote: String? = null,

    /**
     * hp_tp_name
     */
    var purchaseHeaderTpName: String? = null,

    /**
     * hp_cashname
     */
    var purchaseHeaderCashName: String? = null,

    /**
     * hp_notpaid
     */
    var purchaseHeaderNotPaid: Boolean? = null,

    /**
     * hp_phoneorder
     */
    var purchaseHeaderPhoneOrder: Boolean? = null,

    /**
     * hp_netamt
     */
    var purchaseHeaderNetAmt: Double? = null,

    /**
     * hp_vatamt
     */
    var purchaseHeaderVatAmt: Double? = null,

    /**
     * hp_total1
     */
    var purchaseHeaderTotal1: Double? = null,

    /**
     * hp_ratef
     */
    var purchaseHeaderRateF: Double? = null,

    /**
     * hp_rates
     */
    var purchaseHeaderRateS: Double? = null,

    /**
     * hp_ratetax
     */
    var purchaseHeaderRateTax: Double? = null,

    /**
     * hp_employee
     */
    var purchaseHeaderEmployee: String? = null,

    /**
     * hp_delivered
     */
    var purchaseHeaderDelivered: Boolean? = null,

    /**
     * hp_timestamp
     */
    var purchaseHeaderTimestamp: Date? = null,

    /**
     * hp_userstamp
     */
    var purchaseHeaderUserStamp: String? = null,

    /**
     * hp_hsid
     */
    var purchaseHeaderHsId: String? = null,

    /**
     * hp_valuedate
     */
    var purchaseHeaderValueDate: Date? = null,

    /**
     * hp_hj_no
     */
    var purchaseHeaderHjNo: String? = null,

    /**
     * hp_pathtodoc
     */
    var purchaseHeaderPathToDoc: String? = null,

    /**
     * hp_duedate
     */
    var purchaseHeaderDueDate: Date? = null,

    /**
     * hp_total
     */
    var purchaseHeaderTotal: Double? = null,

    /**
     * hp_ratetaxf
     */
    var purchaseHeaderRateTaxF: Double? = null,

    /**
     * hp_ratetaxs
     */
    var purchaseHeaderRateTaxS: Double? = null,

    /**
     * hp_taxamt
     */
    var purchaseHeaderTaxAmt: Double? = null,

    /**
     * hp_tax1amt
     */
    var purchaseHeaderTax1Amt: Double? = null,

    /**
     * hp_tax2amt
     */
    var purchaseHeaderTax2Amt: Double? = null
)