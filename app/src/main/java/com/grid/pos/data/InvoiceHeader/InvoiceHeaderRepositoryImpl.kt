package com.grid.pos.data.InvoiceHeader

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.TableModel
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
                if (!invoiceHeader.invoiceHeadTableId.isNullOrEmpty()) {
                    if (SettingsModel.isSqlServerWebDb) {
                        if (isFinished && invoiceHeader.invoiceHeadTableType?.equals("temp") == true) {
                            SQLServerWrapper.delete(
                                "pos_table",
                                " ta_name = '${invoiceHeader.invoiceHeadTableId}'"
                            )
                        } else {
                            SQLServerWrapper.update(
                                "pos_table",
                                listOf(
                                    "ta_hiid",
                                    "ta_status",
                                ),
                                listOf(
                                    if (isFinished) null else invoiceHeader.invoiceHeadId,
                                    if (isFinished) "Completed" else "Busy",
                                ),
                                " ta_name = '${invoiceHeader.invoiceHeadTableId}'"
                            )
                        }
                    } else {
                        SQLServerWrapper.update(
                            "pos_table",
                            listOf(
                                "ta_hiid",
                                "ta_status"
                            ),
                            listOf(
                                if (isFinished) null else invoiceHeader.invoiceHeadId,
                                if (isFinished) "Completed" else "Busy",
                            ),
                            " ta_name = '${invoiceHeader.invoiceHeadTableId}'"
                        )
                    }
                } else if (!invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) {
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
                                "ta_type",
                                "ta_timestamp",
                                "ta_userstamp"
                            ),
                            listOf(
                                tableId,
                                if (isFinished) null else invoiceHeader.invoiceHeadId,
                                if (isFinished) "Completed" else "Busy",
                                invoiceHeader.invoiceHeadTaName,
                                SettingsModel.getCompanyID(),
                                "temp",
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
                                "ta_type",
                                "ta_timestamp",
                                "ta_userstamp"
                            ),
                            listOf(
                                invoiceHeader.invoiceHeadTaName,
                                if (isFinished) null else invoiceHeader.invoiceHeadId,
                                if (isFinished) "Completed" else "Busy",
                                "table",
                                DateHelper.getDateInFormat(
                                    Date(invoiceHeader.invoiceHeadDateTime),
                                    "yyyy-MM-dd hh:mm:ss.SSS"
                                ),
                                SettingsModel.currentUser?.userName
                            )
                        )
                    }
                }
                invoiceHeader.invoiceHeadId = insertByProcedure(invoiceHeader)
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
                SQLServerWrapper.executeProcedure(
                    "delin_hinvoice",
                    listOf(
                        invoiceHeader.invoiceHeadId,
                        SettingsModel.currentUser?.userUsername
                    )
                )
            }
        }
    }

    override suspend fun updateInvoiceHeader(
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
                updateByProcedure(invoiceHeader)
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
                    if (isFinished && invoiceHeader.invoiceHeadTableType?.equals("temp") == true) {
                        SQLServerWrapper.delete(
                            "pos_table",
                            " ta_name = '${invoiceHeader.invoiceHeadTableId}'"
                        )
                    } else {
                        SQLServerWrapper.update(
                            "pos_table",
                            listOf(
                                "ta_status",
                                "ta_hiid",
                                "ta_timestamp",
                                "ta_userstamp"
                            ),
                            listOf(
                                if (isFinished) "Completed" else "Busy",
                                if (isFinished) null else invoiceHeader.invoiceHeadId,
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
                }
                updateByProcedure(invoiceHeader)
            }
        }
    }

    override suspend fun getAllInvoiceHeaders(): MutableList<InvoiceHeader> {
        val limit = 100
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereNotEqualTo(
                        "hi_transno",
                        null
                    ).orderBy(
                        "hi_transno",
                        Query.Direction.DESCENDING
                    ).limit(limit.toLong()).get().await()
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

                val tablesQuerySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereEqualTo(
                        "hi_transno",
                        null
                    ).whereEqualTo(
                        "hi_ta_name",
                        null
                    ).limit(limit.toLong()).get().await()
                if (tablesQuerySnapshot.size() > 0) {
                    for (document in tablesQuerySnapshot) {
                        val obj = document.toObject(InvoiceHeader::class.java)
                        if (obj.invoiceHeadId.isNotEmpty()) {
                            obj.invoiceHeadDocumentId = document.id
                            invoices.add(obj)
                        }
                    }
                }
                return invoices.sortedByDescending { it.invoiceHeadDate }.toMutableList()
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceHeaderDao.getAllInvoiceHeaders(
                    limit,
                    SettingsModel.getCompanyID() ?: ""
                )
            }

            else -> {
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                try {
                    val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' AND ((hi_transno IS NOT NULL AND hi_transno <> '' AND hi_transno <> '0') OR ((hi_transno IS NULL OR hi_transno = '' OR hi_transno = '0') AND (hi_ta_name IS NULL OR hi_ta_name = '')))"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hinvoice",
                        "TOP $limit",
                        if (SettingsModel.isSqlServerWebDb) mutableListOf("*,tt.tt_newcode") else  mutableListOf("*"),
                        where,
                        "ORDER BY hi_date DESC",
                        if (SettingsModel.isSqlServerWebDb) "INNER JOIN acc_transactiontype tt on hi_tt_code = tt.tt_code" else ""
                    )
                    dbResult?.let {
                        while (it.next()) {
                            invoiceHeaders.add(fillParams(it,"tt_newcode"))
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

    override suspend fun getLastOrderByType(): InvoiceHeader? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_cmp_id",
                        SettingsModel.getCompanyID()
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
                return invoiceHeaderDao.getLastOrderByType(SettingsModel.getCompanyID() ?: "")
            }

            else -> {
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                try {
                    val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' AND hi_orderno <> '' AND hi_orderno IS NOT NULL"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hinvoice",
                        "TOP 1",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hi_timestamp DESC"
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

    override suspend fun getLastTransactionByType(
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
                        "hi_transno",
                        null
                    ).orderBy(
                        "hi_transno",
                        Query.Direction.DESCENDING
                    ).limit(1).get().await()
                val document = querySnapshot.firstOrNull()
                return document?.toObject(InvoiceHeader::class.java)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceHeaderDao.getLastTransactionByType(
                    type,
                    SettingsModel.getCompanyID() ?: ""
                )
            }

            else -> {
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                try {
                    val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' AND hi_tt_code = '$type' AND hi_transno IS NOT NULL AND hi_transno <> '' AND hi_transno <> '0'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hinvoice",
                        "TOP 1",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hi_transno DESC"
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
                        "hi_timestamp",
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
                    val where = "hi_cmp_id='${SettingsModel.getCompanyID()}'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hinvoice",
                        "TOP 1",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hi_transno DESC"
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

    override suspend fun getAllOpenedTables(): MutableList<TableModel> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .whereEqualTo(
                        "hi_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereNotEqualTo(
                        "hi_ta_name",
                        null
                    ).whereEqualTo(
                        "hi_transno",
                        null
                    ).get().await()
                val tables = mutableListOf<TableModel>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(InvoiceHeader::class.java)
                        if (obj.invoiceHeadId.isNotEmpty()) {
                            obj.invoiceHeadDocumentId = document.id
                            if (!obj.invoiceHeadTaName.isNullOrEmpty()) {
                                tables.add(
                                    TableModel(
                                        obj.invoiceHeadTaName!!,
                                        obj.invoiceHeadTaName!!
                                    )
                                )
                            }
                        }
                    }
                }
                return tables
            }

            CONNECTION_TYPE.LOCAL.key -> {
                val tables = mutableListOf<TableModel>()
                val invoices = invoiceHeaderDao.getOpenedInvoicesTable(
                    SettingsModel.getCompanyID() ?: ""
                )
                for (invoice in invoices) {
                    if (!invoice.invoiceHeadTaName.isNullOrEmpty()) {
                        tables.add(
                            TableModel(
                                invoice.invoiceHeadTaName!!,
                                invoice.invoiceHeadTaName!!
                            )
                        )
                    }
                }
                return tables
            }

            else -> {
                val tables: MutableList<TableModel> = mutableListOf()
                if (SettingsModel.isSqlServerWebDb) {
                    try {
                        val where = "ta_cmp_id='${SettingsModel.getCompanyID()}' AND ta_hiid IS NOT NULL AND ta_hiid <> ''"
                        val dbResult = SQLServerWrapper.getListOf(
                            "pos_table",
                            "",
                            mutableListOf("*"),
                            where
                        )
                        dbResult?.let {
                            while (it.next()) {
                                tables.add(
                                    TableModel(
                                        it.getStringValue("ta_name"),
                                        it.getStringValue("ta_newname"),
                                        it.getStringValue("ta_type")
                                    )
                                )
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        val where = " ta_hiid IS NOT NULL AND ta_hiid <> ''"
                        val dbResult = SQLServerWrapper.getListOf(
                            "pos_table",
                            "",
                            mutableListOf("*"),
                            where
                        )
                        dbResult?.let {
                            while (it.next()) {
                                tables.add(
                                    TableModel(
                                        it.getStringValue("ta_name"),
                                        it.getStringValue("ta_name"),
                                        it.getStringValue("ta_type")
                                    )
                                )
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                return tables
            }
        }
    }

    override suspend fun getInvoiceByTable(
            tableNo: String
    ): InvoiceHeader {
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
                    obj.invoiceHeadDocumentId = document.id
                    return obj
                }
                return InvoiceHeader()
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceHeaderDao.getInvoiceByTable(
                    tableNo,
                    SettingsModel.getCompanyID() ?: ""
                ) ?: InvoiceHeader()
            }

            else -> {
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                var tableModel: TableModel? = null
                try {
                    tableModel = getTableIdByNumber(tableNo)
                    val subQuery = if (!tableModel?.table_inv_id.isNullOrEmpty()) {
                        "hi_id = '${tableModel?.table_inv_id}'"
                    } else if (!tableModel?.table_id.isNullOrEmpty()) {
                        "(hi_ta_name = '${tableModel?.table_id}' OR hi_ta_name = '$tableNo')"
                    } else {
                        "hi_ta_name = '$tableNo'"
                    }
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
                            invoiceHeader.invoiceHeadTableId = tableModel?.table_id
                            invoiceHeader.invoiceHeadTaName = tableNo
                            invoiceHeader.invoiceHeadTableType = tableModel?.table_type
                            invoiceHeaders.add(invoiceHeader)
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return if (invoiceHeaders.size > 0) {
                    invoiceHeaders[0]
                } else {
                    InvoiceHeader(
                        invoiceHeadTableId = tableModel?.table_id,
                        invoiceHeadTaName = tableNo,
                        invoiceHeadTableType = tableModel?.table_type
                    )
                }
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
                ).whereGreaterThanOrEqualTo(
                    "hi_timestamp",
                    from
                ).whereLessThan(
                    "hi_timestamp",
                    to
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
        } else {
            return invoiceHeaderDao.getInvoicesBetween(
                from.time,
                to.time,
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

    private fun fillParams(obj: ResultSet,ttCodeKey:String = "hi_tt_code"): InvoiceHeader {
        return InvoiceHeader().apply {
            invoiceHeadId = obj.getStringValue("hi_id")
            invoiceHeadNo = obj.getStringValue("hi_no")
            invoiceHeadCompId = obj.getStringValue("hi_cmp_id")
            invoiceHeadDate = obj.getStringValue("hi_date")
            invoiceHeadOrderNo = obj.getStringValue("hi_orderno")
            invoiceHeadTtCode = obj.getStringValue(ttCodeKey)
            invoiceHeadTransNo = obj.getStringValue("hi_transno")
            invoiceHeadStatus = obj.getStringValue("hi_status")
            invoiceHeadNote = obj.getStringValue("hi_note")
            invoiceHeadThirdPartyName = obj.getStringValue("hi_tp_name")
            invoiceHeadCashName = obj.getStringValue("hi_cashname")
            invoiceHeadTotalNetAmount = obj.getDoubleValue("hi_netamt")
            invoiceHeadGrossAmount = obj.getDoubleValue("hi_netamt")
            invoiceHeadDiscount = obj.getDoubleValue("hi_disc")
            invoiceHeadDiscountAmount = obj.getDoubleValue("hi_discamt")
            invoiceHeadTaxAmt = obj.getDoubleValue("hi_taxamt")
            invoiceHeadTax1Amt = obj.getDoubleValue("hi_tax1amt")
            invoiceHeadTax2Amt = obj.getDoubleValue("hi_tax2amt")
            invoiceHeadTotalTax = invoiceHeadTaxAmt + invoiceHeadTax1Amt + invoiceHeadTax2Amt
            invoiceHeadTotal1 = obj.getDoubleValue("hi_total1")
            invoiceHeadRate = obj.getDoubleValue("hi_rates")
            invoiceHeadTotal = obj.getDoubleValue(
                "hi_total",
                invoiceHeadTotal1.div(invoiceHeadRate)
            )
            if (SettingsModel.isSqlServerWebDb) {
                invoiceHeadTableId = obj.getStringValue("hi_ta_name")
            } else {
                invoiceHeadTableId = obj.getStringValue("hi_ta_name")
                invoiceHeadTaName = obj.getStringValue("hi_ta_name")
            }
            invoiceHeadClientsCount = obj.getIntValue("hi_clientscount")
            invoiceHeadChange = obj.getDoubleValue("hi_change")
            invoiceHeadPrinted = obj.getIntValue("hi_printed")
            val timeStamp = obj.getObjectValue("hi_timestamp")
            invoiceHeadTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            invoiceHeadDateTime = invoiceHeadTimeStamp!!.time
            invoiceHeadUserStamp = obj.getStringValue("hi_userstamp")
        }
    }

    private fun getTableIdByNumber(tableNo: String): TableModel? {
        try {
            val where = if (SettingsModel.isSqlServerWebDb) "ta_cmp_id='${SettingsModel.getCompanyID()}' AND ta_newname = '$tableNo'"
            else " ta_name = '$tableNo'"
            val dbResult = SQLServerWrapper.getListOf(
                "pos_table",
                "TOP 1",
                mutableListOf("*"),
                where
            )
            dbResult?.let {
                if (it.next()) {
                    return if (SettingsModel.isSqlServerWebDb) {
                        TableModel(
                            it.getStringValue("ta_name"),
                            it.getStringValue("ta_newname"),
                            it.getStringValue("ta_type"),
                            it.getStringValue("ta_hiid")
                        )
                    } else {
                        TableModel(
                            it.getStringValue("ta_name"),
                            it.getStringValue("ta_name"),
                            it.getStringValue("ta_type"),
                            it.getStringValue("ta_hiid")
                        )
                    }
                }
                SQLServerWrapper.closeResultSet(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun insertByProcedure(invoiceHeader: InvoiceHeader): String {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                invoiceHeader.invoiceHeadCompId,
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) "Proforma Invoice" else "Sale Invoice",
                Timestamp(System.currentTimeMillis()),
                Timestamp(System.currentTimeMillis()),
                if (!invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) "In" else "Carry",
                invoiceHeader.invoiceHeadOrderNo,
                invoiceHeader.invoiceHeadTtCode,
                invoiceHeader.invoiceHeadTransNo,
                null,
                null,
                SettingsModel.currentCurrency?.currencyId,
                invoiceHeader.invoiceHeadDiscount,
                invoiceHeader.invoiceHeadDiscountAmount,
                SettingsModel.defaultWarehouse,
                SettingsModel.defaultBranch,
                null,
                invoiceHeader.invoiceHeadNote,
                invoiceHeader.invoiceHeadThirdPartyName,
                invoiceHeader.invoiceHeadCashName,
                0,//notpaid
                0,//phoneorder
                invoiceHeader.invoiceHeadGrossAmount,
                invoiceHeader.getVatAmount(),
                null,//round
                invoiceHeader.invoiceHeadTotal1,
                1,//rate first
                invoiceHeader.invoiceHeadRate,//rate seconds
                1,//rate tax
                0,//tips
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                0,//mincharge
                SettingsModel.currentUser?.userUsername,//hi_employee
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) null else 1,//delivered
                invoiceHeader.invoiceHeadUserStamp,//hi_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//branchcode
                invoiceHeader.invoiceHeadChange,
                Timestamp(System.currentTimeMillis()),//hi_valuedate
                invoiceHeader.invoiceHeadPrinted,//hi_printed
                0,//hi_smssent
                null,//hi_pathtodoc
                null,//hi_cashback
                "null_string_output",//hi_id
                invoiceHeader.invoiceHeadTotal,//@hi_total
                1,//hi_ratetaxf
                invoiceHeader.invoiceHeadRate,//hi_ratetaxs
                invoiceHeader.invoiceHeadTaxAmt,
                invoiceHeader.invoiceHeadTax1Amt,
                invoiceHeader.invoiceHeadTax2Amt,
            )
        } else {
            listOf(
                invoiceHeader.invoiceHeadCompId,
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) "Proforma Invoice" else "Sale Invoice",
                Timestamp(System.currentTimeMillis()),
                Timestamp(System.currentTimeMillis()),
                if (!invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) "In" else "Carry",
                invoiceHeader.invoiceHeadOrderNo,
                invoiceHeader.invoiceHeadTtCode,
                invoiceHeader.invoiceHeadTransNo,
                null,
                null,
                SettingsModel.currentCurrency?.currencyId,
                invoiceHeader.invoiceHeadDiscount,
                invoiceHeader.invoiceHeadDiscountAmount,
                SettingsModel.defaultWarehouse,
                SettingsModel.defaultBranch,
                null,
                invoiceHeader.invoiceHeadNote,
                invoiceHeader.invoiceHeadThirdPartyName,
                invoiceHeader.invoiceHeadCashName,
                0,//notpaid
                0,//phoneorder
                invoiceHeader.invoiceHeadGrossAmount,
                invoiceHeader.getVatAmount(),
                null,//round
                invoiceHeader.invoiceHeadTotal1,
                1,//rate first
                invoiceHeader.invoiceHeadRate,//rate seconds
                1,//rate tax
                0,//tips
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                0,//mincharge
                SettingsModel.currentUser?.userUsername,//hi_employee
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) null else 1,//delivered
                invoiceHeader.invoiceHeadUserStamp,//hi_userstamp
                null,//hi_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//branchcode
                invoiceHeader.invoiceHeadChange,
                Timestamp(System.currentTimeMillis()),//hi_valuedate
                invoiceHeader.invoiceHeadPrinted,//hi_printed
                0,//hi_smssent
                null,//hi_pathtodoc
                null,//hi_cashback
                "null_int_output",//hi_id
                1,//hi_ratetaxf
                invoiceHeader.invoiceHeadRate,//hi_ratetaxs
                invoiceHeader.invoiceHeadTaxAmt,
                invoiceHeader.invoiceHeadTax1Amt,
                invoiceHeader.invoiceHeadTax2Amt,
            )
        }
        return SQLServerWrapper.executeProcedure(
            "addin_hinvoice",
            parameters
        ) ?: ""
    }

    private fun updateByProcedure(invoiceHeader: InvoiceHeader) {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                invoiceHeader.invoiceHeadId,
                invoiceHeader.invoiceHeadNo,
                invoiceHeader.invoiceHeadCompId,
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) "Proforma Invoice" else "Sale Invoice",
                Timestamp(System.currentTimeMillis()),
                Timestamp(System.currentTimeMillis()),
                if (!invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) "In" else "Carry",
                invoiceHeader.invoiceHeadOrderNo,
                invoiceHeader.invoiceHeadTtCode,
                invoiceHeader.invoiceHeadTransNo,
                null,
                null,
                SettingsModel.currentCurrency?.currencyId,
                invoiceHeader.invoiceHeadDiscount,
                invoiceHeader.invoiceHeadDiscountAmount,
                SettingsModel.defaultWarehouse,
                SettingsModel.defaultBranch,
                null,
                invoiceHeader.invoiceHeadNote,
                invoiceHeader.invoiceHeadThirdPartyName,
                invoiceHeader.invoiceHeadCashName,
                0,//notpaid
                0,//phoneorder
                invoiceHeader.invoiceHeadGrossAmount,
                invoiceHeader.getVatAmount(),
                null,//round
                invoiceHeader.invoiceHeadTotal1,
                1,//rate first
                invoiceHeader.invoiceHeadRate,//rate seconds
                1,//rate tax
                0,//tips
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                0,//mincharge
                SettingsModel.currentUser?.userUsername,//hi_employee
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) null else 1,//delivered
                invoiceHeader.invoiceHeadUserStamp,//hi_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//branchcode
                invoiceHeader.invoiceHeadChange,
                Timestamp(System.currentTimeMillis()),//hi_valuedate
                invoiceHeader.invoiceHeadPrinted,//hi_printed
                0,//hi_smssent
                null,//hi_pathtodoc
                null,//hi_cashback
                invoiceHeader.invoiceHeadTotal,//@hi_total
                1,//hi_ratetaxf
                invoiceHeader.invoiceHeadRate,//hi_ratetaxs
                invoiceHeader.invoiceHeadTaxAmt,
                invoiceHeader.invoiceHeadTax1Amt,
                invoiceHeader.invoiceHeadTax2Amt,
            )
        } else {
            listOf(
                invoiceHeader.invoiceHeadId,
                invoiceHeader.invoiceHeadNo,
                invoiceHeader.invoiceHeadCompId,
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) "Proforma Invoice" else "Sale Invoice",
                Timestamp(System.currentTimeMillis()),
                Timestamp(System.currentTimeMillis()),
                if (!invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) "In" else "Carry",
                invoiceHeader.invoiceHeadOrderNo,
                invoiceHeader.invoiceHeadTtCode,
                invoiceHeader.invoiceHeadTransNo,
                null,
                null,
                SettingsModel.currentCurrency?.currencyId,
                invoiceHeader.invoiceHeadDiscount,
                invoiceHeader.invoiceHeadDiscountAmount,
                SettingsModel.defaultWarehouse,
                SettingsModel.defaultBranch,
                null,
                invoiceHeader.invoiceHeadNote,
                invoiceHeader.invoiceHeadThirdPartyName,
                invoiceHeader.invoiceHeadCashName,
                0,//notpaid
                0,//phoneorder
                invoiceHeader.invoiceHeadGrossAmount,
                invoiceHeader.getVatAmount(),
                null,//round
                invoiceHeader.invoiceHeadTotal1,
                1,//rate first
                invoiceHeader.invoiceHeadRate,//rate seconds
                1,//rate tax
                0,//tips
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                0,//mincharge
                SettingsModel.currentUser?.userUsername,//hi_employee
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) null else 1,//delivered
                invoiceHeader.invoiceHeadUserStamp,//hi_userstamp
                invoiceHeader.invoiceHeadChange,
                Timestamp(System.currentTimeMillis()),//hi_valuedate
                0,//hi_smssent
                null,//hi_pathtodoc
                null,//hi_cashback
                1,//hi_ratetaxf
                invoiceHeader.invoiceHeadRate,//hi_ratetaxs
                invoiceHeader.invoiceHeadTaxAmt,
                invoiceHeader.invoiceHeadTax1Amt,
                invoiceHeader.invoiceHeadTax2Amt,
            )
        }
        SQLServerWrapper.executeProcedure(
            "updin_hinvoice",
            parameters
        )
    }
}