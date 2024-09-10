package com.grid.pos.data.InvoiceHeader

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getIntValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import com.grid.pos.utils.Utils
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class InvoiceHeaderRepositoryImpl(
        private val invoiceHeaderDao: InvoiceHeaderDao
) : InvoiceHeaderRepository {
    override suspend fun insert(
            invoiceHeader: InvoiceHeader,
            isFinished: Boolean
    ): InvoiceHeader {
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
                if (!invoiceHeader.invoiceHeadTableId.isNullOrEmpty()/* && !invoiceHeader.invoiceHeadTaName.isNullOrEmpty()*/) {
                    if (SettingsModel.isSqlServerWebDb) {
                        val tableId = Utils.generateRandomUuidString()
                        SQLServerWrapper.insert(
                            "pos_table",
                            listOf(
                                "ta_name",
                                "ta_hiid",
                                "ta_status",
                                "ta_newname",
                                "ta_cmp_id",
                                "ta_locked",
                                "ta_timestamp",
                                "ta_userstamp"
                            ),
                            listOf(
                                tableId,
                                invoiceHeader.invoiceHeadId,
                                if (isFinished) "Completed" else "Busy",
                                invoiceHeader.invoiceHeadTaName,
                                SettingsModel.getCompanyID(),
                                if (isFinished) "0" else "1",
                                DateHelper.getDateInFormat(
                                    Date(invoiceHeader.invoiceHeadDateTime),
                                    "yyyy-MM-dd hh:mm:ss.SSS"
                                ),
                                SettingsModel.currentUser?.userName
                            )
                        )
                        invoiceHeader.invoiceHeadTableId = tableId
                    } else {
                        SQLServerWrapper.insert(
                            "pos_table",
                            listOf(
                                "ta_name",
                                "ta_hiid",
                                "ta_status",
                                "ta_locked",
                                "ta_timestamp",
                                "ta_userstamp"
                            ),
                            listOf(
                                invoiceHeader.invoiceHeadTaName,
                                invoiceHeader.invoiceHeadId,
                                if (isFinished) "Completed" else "Busy",
                                if (isFinished) "0" else "1",
                                DateHelper.getDateInFormat(
                                    Date(invoiceHeader.invoiceHeadDateTime),
                                    "yyyy-MM-dd hh:mm:ss.SSS"
                                ),
                                SettingsModel.currentUser?.userName
                            )
                        )
                    }
                }
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
            invoiceHeader: InvoiceHeader,
            isFinished: Boolean
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
                if (!invoiceHeader.invoiceHeadTableId.isNullOrEmpty()) {
                    SQLServerWrapper.update(
                        "pos_table",
                        listOf(
                            "ta_status",
                            "ta_locked",
                            "ta_timestamp",
                            "ta_userstamp"
                        ),
                        listOf(
                            if (isFinished) "Completed" else "Busy",
                            if (isFinished) "0" else "1",
                            DateHelper.getDateInFormat(
                                Date(invoiceHeader.invoiceHeadDateTime),
                                "yyyy-MM-dd hh:mm:ss.SSS"
                            ),
                            SettingsModel.currentUser?.userName
                        ),
                        if (SettingsModel.isSqlServerWebDb) {
                            "ta_name = '${invoiceHeader.invoiceHeadTableId}'"
                        } else {
                            "ta_name = '${invoiceHeader.invoiceHeadTaName}'"
                        }
                    )
                }
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
                    ).whereNotEqualTo(
                        "hi_transno",
                        null
                    ).whereNotEqualTo(
                        "hi_transno",
                        ""
                    ).orderBy(
                        "hi_transno",
                        Query.Direction.DESCENDING
                    ).limit(100).get().await()
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
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                try {
                    val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' ORDER BY hi_transno DESC"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hinvoice",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            invoiceHeaders.add(fillParams(it))
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
                    ).orderBy(
                        "hi_timestamp",
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
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                try {
                    val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' AND hi_tt_code = '$type' ORDER BY hi_timestamp DESC"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hinvoice",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            invoiceHeaders.add(fillParams(it))
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return if (invoiceHeaders.size > 0) invoiceHeaders[0] else null
            }
        }
    }

    override suspend fun getLastInvoice(): InvoiceHeader? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_cmp_id",
                        SettingsModel.getCompanyID()
                    ).orderBy(
                        "hi_transno",
                        Query.Direction.DESCENDING
                    ).limit(1).get().await()
                val document = querySnapshot.firstOrNull()
                return document?.toObject(InvoiceHeader::class.java)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceHeaderDao.getLastInvoice(
                    SettingsModel.getCompanyID() ?: ""
                )
            }

            else -> {
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                try {
                    val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' ORDER BY hi_transno DESC"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hinvoice",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            invoiceHeaders.add(fillParams(it))
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return if (invoiceHeaders.size > 0) invoiceHeaders[0] else null
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
                    return document.toObject(InvoiceHeader::class.java)
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
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                try {
                    val tableId = getTableIdByNumber(tableNo)
                    val subQuery = if (!tableId.isNullOrEmpty()) "(hi_ta_name = '$tableId' OR hi_ta_name = '$tableNo')" else "hi_ta_name = '$tableNo'"
                    val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' AND $subQuery AND (hi_transno IS NULL OR hi_transno = '')"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hinvoice",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            val invoiceHeader = fillParams(it)
                            invoiceHeader.invoiceHeadTaName = tableNo
                            invoiceHeaders.add(invoiceHeader)
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return if (invoiceHeaders.size > 0) invoiceHeaders[0] else null
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

    override suspend fun getOneInvoiceByUserID(userId: String): InvoiceHeader? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_userstamp",
                        userId
                    ).limit(1).get().await()
                val document = querySnapshot.firstOrNull()
                if (document != null) {
                    return document.toObject(InvoiceHeader::class.java)
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceHeaderDao.getOneInvoiceByUserId(userId)
            }

            else -> {
                return null
            }
        }
    }

    override suspend fun getOneInvoiceByClientID(clientId: String): InvoiceHeader? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_tp_name",
                        clientId
                    ).limit(1).get().await()
                val document = querySnapshot.firstOrNull()
                if (document != null) {
                    return document.toObject(InvoiceHeader::class.java)
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceHeaderDao.getOneInvoiceByClientId(clientId)
            }

            else -> {
                return null
            }
        }
    }

    private fun fillParams(obj: ResultSet): InvoiceHeader {
        return InvoiceHeader().apply {
            invoiceHeadId = obj.getStringValue("hi_id")
            invoiceHeadCompId = obj.getStringValue("hi_cmp_id")
            invoiceHeadDate = obj.getString("hi_date")
            invoiceHeadOrderNo = obj.getStringValue("hi_orderno")
            invoiceHeadTtCode = obj.getStringValue("hi_tt_code")
            invoiceHeadTransNo = obj.getStringValue("hi_transno")
            invoiceHeadStatus = obj.getStringValue("hi_status")
            invoiceHeadNote = obj.getStringValue("hi_note")
            invoiceHeadThirdPartyName = obj.getStringValue("hi_tp_name")
            invoiceHeadCashName = obj.getStringValue("hi_cashname")
            invoiceHeadTotalNetAmount = obj.getDoubleValue("hi_total")
            invoiceHeadGrossAmount = obj.getDoubleValue("hi_netamt")
            invoiceHeadDiscount = obj.getDoubleValue("hi_disc")
            invoiceHeadDiscountAmount = obj.getDoubleValue("hi_discamt")
            invoiceHeadTaxAmt = obj.getDoubleValue("hi_taxamt")
            invoiceHeadTax1Amt = obj.getDoubleValue("hi_tax1amt")
            invoiceHeadTax2Amt = obj.getDoubleValue("hi_tax2amt")
            invoiceHeadTotalTax = invoiceHeadTaxAmt + invoiceHeadTax1Amt + invoiceHeadTax2Amt
            invoiceHeadTotal = obj.getDoubleValue("hi_total")
            invoiceHeadTotal1 = obj.getDoubleValue("hi_total1")
            invoiceHeadRate = obj.getDoubleValue("hi_rates")
            if (SettingsModel.isSqlServerWebDb) {
                invoiceHeadTableId = obj.getStringValue("hi_ta_name")
            } else {
                invoiceHeadTaName = obj.getStringValue("hi_ta_name")
            }
            invoiceHeadClientsCount = obj.getIntValue("hi_clientscount")
            invoiceHeadChange = obj.getDoubleValue("hi_change")
            val timeStamp = obj.getObjectValue("hi_timestamp")
            invoiceHeadTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            invoiceHeadDateTime = invoiceHeadTimeStamp!!.time
            invoiceHeadUserStamp = obj.getStringValue("hi_userstamp")
        }
    }

    private fun getColumns(): List<String> {
        return if (SettingsModel.isSqlServerWebDb) {
            listOf(
                "hi_id",
                "hi_cmp_id",
                "hi_date",
                "hi_orderno",
                "hi_tt_code",
                "hi_transno",
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
                "hi_total1",
                "hi_rates",
                "hi_ta_name",
                "hi_clientscount",
                "hi_change",
                "hi_wa_name",
                "hi_bra_name",
                "hi_timestamp",
                "hi_userstamp",
            )
        } else {
            listOf(
                "hi_id",
                "hi_cmp_id",
                "hi_date",
                "hi_orderno",
                "hi_tt_code",
                "hi_transno",
                "hi_status",
                "hi_note",
                "hi_tp_name",
                "hi_cashname",
                "hi_netamt",
                "hi_disc",
                "hi_discamt",
                "hi_taxamt",
                "hi_tax1amt",
                "hi_tax2amt",
                "hi_total1",
                "hi_rates",
                "hi_ta_name",
                "hi_clientscount",
                "hi_change",
                "hi_wa_name",
                "hi_bra_name",
                "hi_timestamp",
                "hi_userstamp",
            )
        }
    }

    private fun getValues(invoiceHeader: InvoiceHeader): List<Any?> {
        val dateTime = Timestamp.valueOf(
            DateHelper.getDateInFormat(
                Date(),
                "yyyy-MM-dd HH:mm:ss"
            )
        )
        return if (SettingsModel.isSqlServerWebDb) {
            listOf(
                invoiceHeader.invoiceHeadId,
                invoiceHeader.invoiceHeadCompId,
                dateTime,
                invoiceHeader.invoiceHeadOrderNo,
                invoiceHeader.invoiceHeadTtCode,
                invoiceHeader.invoiceHeadTransNo,
                invoiceHeader.invoiceHeadStatus,
                invoiceHeader.invoiceHeadNote,
                invoiceHeader.invoiceHeadThirdPartyName,
                invoiceHeader.invoiceHeadCashName,
                invoiceHeader.getTotalNetAmount(),
                invoiceHeader.invoiceHeadGrossAmount,
                invoiceHeader.invoiceHeadDiscount,
                invoiceHeader.invoiceHeadDiscountAmount,
                invoiceHeader.invoiceHeadTaxAmt,
                invoiceHeader.invoiceHeadTax1Amt,
                invoiceHeader.invoiceHeadTax2Amt,
                invoiceHeader.invoiceHeadTotal1,
                invoiceHeader.invoiceHeadRate,
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                invoiceHeader.invoiceHeadChange,
                SettingsModel.defaultWarehouse,
                SettingsModel.defaultBranch,
                dateTime,
                invoiceHeader.invoiceHeadUserStamp
            )
        } else {
            listOf(
                invoiceHeader.invoiceHeadId,
                invoiceHeader.invoiceHeadCompId,
                dateTime,
                invoiceHeader.invoiceHeadOrderNo,
                invoiceHeader.invoiceHeadTtCode,
                invoiceHeader.invoiceHeadTransNo,
                invoiceHeader.invoiceHeadStatus,
                invoiceHeader.invoiceHeadNote,
                invoiceHeader.invoiceHeadThirdPartyName,
                invoiceHeader.invoiceHeadCashName,
                invoiceHeader.invoiceHeadGrossAmount,
                invoiceHeader.invoiceHeadDiscount,
                invoiceHeader.invoiceHeadDiscountAmount,
                invoiceHeader.invoiceHeadTaxAmt,
                invoiceHeader.invoiceHeadTax1Amt,
                invoiceHeader.invoiceHeadTax2Amt,
                invoiceHeader.invoiceHeadTotal1,
                invoiceHeader.invoiceHeadRate,
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                invoiceHeader.invoiceHeadChange,
                SettingsModel.defaultWarehouse,
                SettingsModel.defaultBranch,
                dateTime,
                invoiceHeader.invoiceHeadUserStamp
            )
        }
    }

    private fun getTableIdByNumber(tableNo: String): String? {
        try {
            val where = if (SettingsModel.isSqlServerWebDb) "ta_cmp_id='${SettingsModel.getCompanyID()}' AND ta_newname = '$tableNo' AND ta_status = 'Busy'"
            else "ta_name = '$tableNo' AND ta_status = 'Busy'"
            val dbResult = SQLServerWrapper.getListOf(
                "pos_table",
                "TOP 1",
                mutableListOf("*"),
                where
            )
            dbResult?.let {
                if (it.next()) {
                    return it.getStringValue("ta_name")
                }
                SQLServerWrapper.closeResultSet(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}