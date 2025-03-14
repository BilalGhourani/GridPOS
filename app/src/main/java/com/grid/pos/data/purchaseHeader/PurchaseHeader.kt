package com.grid.pos.data.purchaseHeader

import java.util.Date

data class PurchaseHeader(
    /**
     * hp_id
     */
    val purchaseHeaderId: String,

    /**
     * hp_no
     */
    val purchaseHeaderNo: String? = null,

    /**
     * hp_cmp_id
     */
    val purchaseHeaderCmpId: Int? = null,

    /**
     * hp_category
     */
    val purchaseHeaderCategory: String? = null,

    /**
     * hp_date
     */
    val purchaseHeaderDate: String? = null,

    /**
     * hp_orderno
     */
    val purchaseHeaderOrderNo: String? = null,

    /**
     * hp_tt_code
     */
    val purchaseHeaderTtCode: String? = null,

    /**
     * hp_transno
     */
    val purchaseHeaderTransNo: String? = null,

    /**
     * hp_status
     */
    val purchaseHeaderStatus: String? = null,

    /**
     * hp_pln_name
     */
    val purchaseHeaderPlnName: String? = null,

    /**
     * hp_cur_code
     */
    val purchaseHeaderCurCode: String? = null,

    /**
     * hp_disc
     */
    val purchaseHeaderDisc: Double? = null,

    /**
     * hp_discamt
     */
    val purchaseHeaderDiscAmt: Double? = null,

    /**
     * hp_wa_name
     */
    val purchaseHeaderWaName: String? = null,

    /**
     * hp_bra_name
     */
    val purchaseHeaderBraName: String? = null,

    /**
     * hp_prj_name
     */
    val purchaseHeaderPrjName: String? = null,

    /**
     * hp_note
     */
    val purchaseHeaderNote: String? = null,

    /**
     * hp_tp_name
     */
    val purchaseHeaderTpName: String? = null,

    /**
     * hp_cashname
     */
    val purchaseHeaderCashName: String? = null,

    /**
     * hp_notpaid
     */
    val purchaseHeaderNotPaid: Boolean? = null,

    /**
     * hp_phoneorder
     */
    val purchaseHeaderPhoneOrder: Boolean? = null,

    /**
     * hp_netamt
     */
    val purchaseHeaderNetAmt: Double? = null,

    /**
     * hp_vatamt
     */
    val purchaseHeaderVatAmt: Double? = null,

    /**
     * hp_total1
     */
    val purchaseHeaderTotal1: Double? = null,

    /**
     * hp_ratef
     */
    val purchaseHeaderRateF: Double? = null,

    /**
     * hp_rates
     */
    val purchaseHeaderRateS: Double? = null,

    /**
     * hp_ratetax
     */
    val purchaseHeaderRateTax: Double? = null,

    /**
     * hp_employee
     */
    val purchaseHeaderEmployee: String? = null,

    /**
     * hp_delivered
     */
    val purchaseHeaderDelivered: Boolean? = null,

    /**
     * hp_timestamp
     */
    val purchaseHeaderTimestamp: Date? = null,

    /**
     * hp_userstamp
     */
    val purchaseHeaderUserStamp: String? = null,

    /**
     * hp_hsid
     */
    val purchaseHeaderHsId: String? = null,

    /**
     * hp_valuedate
     */
    val purchaseHeaderValueDate: Date? = null,

    /**
     * hp_hj_no
     */
    val purchaseHeaderHjNo: String? = null,

    /**
     * hp_pathtodoc
     */
    val purchaseHeaderPathToDoc: String? = null,

    /**
     * hp_duedate
     */
    val purchaseHeaderDueDate: Date? = null,

    /**
     * hp_total
     */
    val purchaseHeaderTotal: Double? = null,

    /**
     * hp_ratetaxf
     */
    val purchaseHeaderRateTaxF: Double? = null,

    /**
     * hp_ratetaxs
     */
    val purchaseHeaderRateTaxS: Double? = null,

    /**
     * hp_taxamt
     */
    val purchaseHeaderTaxAmt: Double? = null,

    /**
     * hp_tax1amt
     */
    val purchaseHeaderTax1Amt: Double? = null,

    /**
     * hp_tax2amt
     */
    val purchaseHeaderTax2Amt: Double? = null
)