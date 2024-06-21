package com.grid.pos.data.InvoiceHeader

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.Date

class InvoiceHeaderRepositoryImpl(
        private val invoiceHeaderDao: InvoiceHeaderDao
) : InvoiceHeaderRepository {
    override suspend fun insert(invoiceHeader: InvoiceHeader): InvoiceHeader {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .add(invoiceHeader.getMap()).await()
                invoiceHeader.invoiceHeadDocumentId = docRef.id
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceHeaderDao.insert(invoiceHeader)
            }

            else -> {
                SQLServerWrapper.insert(
                    "in_hinvoice",
                    getColumns(),
                    getValues(invoiceHeader)
                )
            }
        }
        return invoiceHeader
    }

    override suspend fun delete(
            invoiceHeader: InvoiceHeader
    ) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                invoiceHeader.invoiceHeadDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("in_hinvoice").document(it).delete()
                        .await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceHeaderDao.delete(invoiceHeader)
            }

            else -> {
                SQLServerWrapper.delete(
                    "in_hinvoice",
                    "hi_id = '${invoiceHeader.invoiceHeadId}'"
                )
            }
        }
    }

    override suspend fun update(
            invoiceHeader: InvoiceHeader
    ) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                invoiceHeader.invoiceHeadDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("in_hinvoice").document(it)
                        .update(invoiceHeader.getMap()).await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceHeaderDao.update(invoiceHeader)
            }

            else -> {
                SQLServerWrapper.update(
                    "in_hinvoice",
                    getColumns(),
                    getValues(invoiceHeader),
                    "hi_id = '${invoiceHeader.invoiceHeadId}'"
                )
            }
        }
    }

    override suspend fun getAllInvoiceHeaders(): MutableList<InvoiceHeader> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_cmp_id",
                        SettingsModel.getCompanyID()
                    ).get().await()
                val invoices = mutableListOf<InvoiceHeader>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(InvoiceHeader::class.java)
                        if (obj.invoiceHeadId.isNotEmpty()) {
                            obj.invoiceHeadDocumentId = document.id
                            invoices.add(obj)
                        }
                    }
                }
                return invoices
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceHeaderDao.getAllInvoiceHeaders(SettingsModel.getCompanyID() ?: "")
            }

            else -> {
                val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' ORDER BY hi_orderno DESC LIMIT 1"
                val dbResult = SQLServerWrapper.getListOf(
                    "in_hinvoice",
                    mutableListOf("*"),
                    where
                )
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                dbResult.forEach { obj ->
                    invoiceHeaders.add(fillParams(obj))
                }
                return invoiceHeaders
            }
        }
    }

    override suspend fun getLastInvoiceByType(
            type: String
    ): InvoiceHeader? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereEqualTo(
                        "hi_tt_code",
                        type
                    ).whereNotEqualTo(
                        "hi_orderno",
                        null
                    ).orderBy(
                        "hi_orderno",
                        Query.Direction.DESCENDING
                    ).limit(1).get().await()
                val document = querySnapshot.firstOrNull()
                return document?.toObject(InvoiceHeader::class.java)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceHeaderDao.getLastInvoiceNo(
                    type,
                    SettingsModel.getCompanyID() ?: ""
                )
            }

            else -> {
                val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' AND hi_tt_code = '$type' ORDER BY hi_orderno DESC LIMIT 1"
                val dbResult = SQLServerWrapper.getListOf(
                    "in_hinvoice",
                    mutableListOf("*"),
                    where
                )
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                dbResult.forEach { obj ->
                    invoiceHeaders.add(fillParams(obj))
                }
                return invoiceHeaders[0]
            }
        }
    }

    override suspend fun getInvoiceByTable(
            tableNo: String
    ): InvoiceHeader? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereEqualTo(
                        "hi_ta_name",
                        tableNo
                    ).whereEqualTo(
                        "hi_transno",
                        null
                    ).limit(1).get().await()
                val document = querySnapshot.firstOrNull()
                if (document != null) {
                    val obj = document.toObject(InvoiceHeader::class.java)
                    return obj
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceHeaderDao.getInvoiceByTable(
                    tableNo,
                    SettingsModel.getCompanyID() ?: ""
                )
            }

            else -> {
                val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' AND hi_ta_name = '$tableNo' AND (hi_transno IS NULL OR hi_transno = '') LIMIT 1"
                val dbResult = SQLServerWrapper.getListOf(
                    "in_hinvoice",
                    mutableListOf("*"),
                    where
                )
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                dbResult.forEach { obj ->
                    invoiceHeaders.add(fillParams(obj))
                }
                return invoiceHeaders[0]
            }
        }
    }

    override suspend fun getInvoicesBetween(
            from: Date,
            to: Date
    ): MutableList<InvoiceHeader> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                .whereEqualTo(
                    "hi_cmp_id",
                    SettingsModel.getCompanyID()
                )/*  .whereGreaterThanOrEqualTo(
                      "hi_timestamp",
                      from
                  ).whereLessThan(
                      "hi_timestamp",
                      to
                  )*/.get().await()
            val invoices = mutableListOf<InvoiceHeader>()
            if (querySnapshot.size() > 0) {
                for (document in querySnapshot) {
                    val obj = document.toObject(InvoiceHeader::class.java)
                    if (obj.invoiceHeadId.isNotEmpty()) {
                        obj.invoiceHeadDocumentId = document.id
                        invoices.add(obj)
                    }
                }
            }
            return invoices
        } else {
            return invoiceHeaderDao.getInvoicesBetween(
                from.time * 1000,
                to.time * 1000,
                SettingsModel.getCompanyID() ?: ""
            )
        }
    }

    private fun fillParams(obj: JSONObject): InvoiceHeader {
        return InvoiceHeader().apply {
            invoiceHeadId = obj.optString("hi_id")
            invoiceHeadCompId = obj.optString("hi_cmp_id")
            invoiceHeadDate = obj.optString("hi_date")
            invoiceHeadOrderNo = obj.optString("hi_orderno")
            invoiceHeadTtCode = obj.optString("hi_tt_code")
            invoiceHeadTransNo = obj.optString("hi_transno")
            invoiceHeadStatus = obj.optString("hi_status")
            invoiceHeadNote = obj.optString("hi_note")
            invoiceHeadThirdPartyName = obj.optString("hi_tp_name")
            invoiceHeadCashName = obj.optString("hi_cashname")
            invoiceHeadTotalNetAmount = obj.optDouble("hi_total")
            invoiceHeadGrossAmount = obj.optDouble("hi_netamt")
            invoiceHeadDiscount = obj.optDouble("hi_disc")
            invoiceHeadDiscountAmount = obj.optDouble("hi_discamt")
            invoiceHeadTaxAmt = obj.optDouble("hi_taxamt")
            invoiceHeadTax1Amt = obj.optDouble("hi_tax1amt")
            invoiceHeadTax2Amt = obj.optDouble("hi_tax2amt")
            invoiceHeadTotalTax = invoiceHeadTaxAmt + invoiceHeadTax1Amt + invoiceHeadTax2Amt
            invoiceHeadTotal = obj.optDouble("hi_total")
            invoiceHeadTotal1 = obj.optDouble("hi_total1")
            invoiceHeadRate = obj.optDouble("hi_rates")
            invoiceHeadTaName = obj.optString("hi_ta_name")
            invoiceHeadClientsCount = obj.optInt("hi_clientscount")
            invoiceHeadChange = obj.optDouble("hi_change")
            val timeStamp = obj.opt("hi_timestamp")
            invoiceHeadTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            invoiceHeadDateTime = invoiceHeadTimeStamp!!.time
            invoiceHeadUserStamp = obj.optString("hi_userstamp")
        }
    }

    private fun getColumns(): List<String> {
        return listOf(
            "hi_id",
            "hi_cmp_id",
            "hi_date",
            "hi_orderno",
            "hi_tt_code",
            "hi_transno",
            "hi_status",
            "hi_status",
            "hi_note",
            "hi_tp_name",
            "hi_cashname",
            "hi_total",
            "hi_netamt",
            "hi_disc",
            "hi_discamt",
            "hi_taxamt",
            "hi_tax1amt",
            "hi_tax2amt",
            "hi_total",
            "hi_total1",
            "hi_rates",
            "hi_ta_name",
            "hi_clientscount",
            "hi_change",
            "hi_timestamp",
            "hi_userstamp",
        )
    }

    private fun getValues(invoiceHeader: InvoiceHeader): List<Any?> {
        return listOf(
            invoiceHeader.invoiceHeadId,
            invoiceHeader.invoiceHeadCompId,
            invoiceHeader.invoiceHeadDate,
            invoiceHeader.invoiceHeadOrderNo,
            invoiceHeader.invoiceHeadTtCode,
            invoiceHeader.invoiceHeadTransNo,
            invoiceHeader.invoiceHeadStatus,
            invoiceHeader.invoiceHeadNote,
            invoiceHeader.invoiceHeadThirdPartyName,
            invoiceHeader.invoiceHeadCashName,
            invoiceHeader.invoiceHeadTotalNetAmount,
            invoiceHeader.invoiceHeadGrossAmount,
            invoiceHeader.invoiceHeadDiscount,
            invoiceHeader.invoiceHeadDiscountAmount,
            invoiceHeader.invoiceHeadTaxAmt,
            invoiceHeader.invoiceHeadTax1Amt,
            invoiceHeader.invoiceHeadTax2Amt,
            invoiceHeader.invoiceHeadTotal,
            invoiceHeader.invoiceHeadTotal1,
            invoiceHeader.invoiceHeadRate,
            invoiceHeader.invoiceHeadTaName,
            invoiceHeader.invoiceHeadClientsCount,
            invoiceHeader.invoiceHeadChange,
            invoiceHeader.invoiceHeadTimeStamp,
            invoiceHeader.invoiceHeadUserStamp
        )
    }
}