package com.grid.pos.data.invoiceHeader

import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.grid.pos.App
import com.grid.pos.data.FirebaseWrapper
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.TableInvoiceModel
import com.grid.pos.model.TableModel
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getIntValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import com.grid.pos.utils.Utils
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class InvoiceHeaderRepositoryImpl(
        private val invoiceHeaderDao: InvoiceHeaderDao
) : InvoiceHeaderRepository {
    override suspend fun insert(
            invoiceHeader: InvoiceHeader,
            willPrint: Boolean,
            isFinished: Boolean
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return FirebaseWrapper.insert(
                    "in_hinvoice",
                    invoiceHeader
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceHeaderDao.insert(invoiceHeader)
                return DataModel(invoiceHeader)
            }

            else -> {
                val dataModel = insertByProcedure(invoiceHeader)
                if (!invoiceHeader.invoiceHeadTableId.isNullOrEmpty()) {
                    if (SettingsModel.isSqlServerWebDb) {
                        if (isFinished && invoiceHeader.invoiceHeadTableType?.equals("temp") == true) {
                            deleteTable(invoiceHeader.invoiceHeadTableId!!)
                        } else {
                            updateTable(
                                if (isFinished) null else invoiceHeader.invoiceHeadId,
                                invoiceHeader.invoiceHeadTableId!!,
                                if (isFinished) "Free" else if (willPrint) "RTL" else "Busy",
                                0
                            )
                        }
                    } else {
                        updateTable(
                            if (isFinished) null else invoiceHeader.invoiceHeadId,
                            invoiceHeader.invoiceHeadTableId!!,
                            if (isFinished) null else if (willPrint) "RTL" else null,
                            0
                        )
                    }
                }
                return dataModel
            }
        }
    }

    override suspend fun delete(
            invoiceHeader: InvoiceHeader
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return FirebaseWrapper.delete(
                    "in_hinvoice",
                    invoiceHeader
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceHeaderDao.delete(invoiceHeader)
                return DataModel(invoiceHeader)
            }

            else -> {
                val queryResult = SQLServerWrapper.executeProcedure(
                    "delin_hinvoice",
                    listOf(
                        invoiceHeader.invoiceHeadId,
                        SettingsModel.currentUser?.userUsername
                    )
                )
                return if (queryResult.succeed) {
                    DataModel(invoiceHeader)
                } else {
                    DataModel(
                        invoiceHeader,
                        false
                    )
                }
            }
        }
    }

    override suspend fun updateInvoiceHeader(
            invoiceHeader: InvoiceHeader
    ): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                FirebaseWrapper.update(
                    "in_hinvoice",
                    invoiceHeader
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceHeaderDao.update(invoiceHeader)
                DataModel(invoiceHeader)
            }

            else -> {
                updateByProcedure(invoiceHeader)
            }
        }
    }

    override suspend fun update(
            invoiceHeader: InvoiceHeader,
            willPrint: Boolean,
            isFinished: Boolean
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return FirebaseWrapper.update(
                    "in_hinvoice",
                    invoiceHeader
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceHeaderDao.update(invoiceHeader)
                return DataModel(invoiceHeader)
            }

            else -> {
                val dataModel = updateByProcedure(invoiceHeader)
                if (!invoiceHeader.invoiceHeadTableId.isNullOrEmpty()) {
                    if (isFinished && invoiceHeader.invoiceHeadTableType?.equals("temp") == true) {
                        deleteTable(invoiceHeader.invoiceHeadTableId!!)
                    } else {
                        if (SettingsModel.isSqlServerWebDb) {
                            updateTable(
                                if (isFinished) null else invoiceHeader.invoiceHeadId,
                                invoiceHeader.invoiceHeadTableId!!,
                                if (isFinished) "Free" else if (willPrint) "RTL" else "Busy",
                                0
                            )
                        } else {
                            updateTable(
                                if (isFinished) null else invoiceHeader.invoiceHeadId,
                                invoiceHeader.invoiceHeadTableId!!,
                                if (isFinished) null else if (willPrint) "RTL" else null,
                                0
                            )
                        }

                    }
                }
                return dataModel
            }
        }
    }

    override suspend fun getAllInvoiceHeaders(): MutableList<InvoiceHeader> {
        val limit = 100
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    limit = limit.toLong(),
                    filters = mutableListOf(
                        Filter.equalTo(
                            "hi_cmp_id",
                            SettingsModel.getCompanyID()
                        ),
                        Filter.notEqualTo(
                            "hi_transno",
                            null
                        )
                    ),
                    orderBy = mutableListOf(
                        "hi_transno" to Query.Direction.DESCENDING
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val invoices = mutableListOf<InvoiceHeader>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(InvoiceHeader::class.java)
                        if (obj.invoiceHeadId.isNotEmpty()) {
                            obj.invoiceHeadDocumentId = document.id
                            invoices.add(obj)
                        }
                    }
                }

                val tablesQuerySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    limit = limit.toLong(),
                    filters = mutableListOf(
                        Filter.equalTo(
                            "hi_cmp_id",
                            SettingsModel.getCompanyID()
                        ),
                        Filter.equalTo(
                            "hi_transno",
                            null
                        ),
                        Filter.equalTo(
                            "hi_ta_name",
                            null
                        )
                    )
                )
                val tableSize = tablesQuerySnapshot?.size() ?: 0
                if (tableSize > 0) {
                    for (document in tablesQuerySnapshot!!) {
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
                        if (SettingsModel.isSqlServerWebDb) mutableListOf("*,tt.tt_newcode") else mutableListOf("*"),
                        where,
                        "ORDER BY hi_date DESC",
                        if (SettingsModel.isSqlServerWebDb) "INNER JOIN acc_transactiontype tt on hi_tt_code = tt.tt_code" else ""
                    )
                    dbResult?.let {
                        while (it.next()) {
                            invoiceHeaders.add(
                                fillParams(
                                    it,
                                    "tt_newcode"
                                )
                            )
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

    override suspend fun getAllInvoicesByIds(ids: List<String>): MutableList<InvoiceHeader> {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    filters = mutableListOf(
                        Filter.inArray(
                            "hi_id",
                            ids
                        ),
                        Filter.equalTo(
                            "hi_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val invoices = mutableListOf<InvoiceHeader>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(InvoiceHeader::class.java)
                        if (obj.invoiceHeadId.isNotEmpty()) {
                            obj.invoiceHeadDocumentId = document.id
                            invoices.add(obj)
                        }
                    }
                }

                invoices
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceHeaderDao.getInvoicesWithIds(
                    ids,
                    SettingsModel.getCompanyID() ?: ""
                )
            }

            else -> {
                mutableListOf()
            }
        }
    }

    override suspend fun getLastOrderByType(): InvoiceHeader? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "hi_cmp_id",
                            SettingsModel.getCompanyID()
                        ),
                        Filter.notEqualTo(
                            "hi_orderno",
                            null
                        )
                    ),
                    orderBy = mutableListOf(
                        "hi_timestamp" to Query.Direction.DESCENDING
                    )
                )
                val document = querySnapshot?.firstOrNull()
                val invoiceHeader = document?.toObject(InvoiceHeader::class.java)
                invoiceHeader?.invoiceHeadDocumentId = document?.id
                return invoiceHeader
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
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "hi_cmp_id",
                            SettingsModel.getCompanyID()
                        ),
                        Filter.equalTo(
                            "hi_tt_code",
                            type
                        ),
                        Filter.notEqualTo(
                            "hi_transno",
                            null
                        )
                    ),
                    orderBy = mutableListOf(
                        "hi_transno" to Query.Direction.DESCENDING
                    )
                )
                val document = querySnapshot?.firstOrNull()
                val invoiceHeader = document?.toObject(InvoiceHeader::class.java)
                invoiceHeader?.invoiceHeadDocumentId = document?.id
                return invoiceHeader
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
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "hi_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    ),
                    orderBy = mutableListOf(
                        "hi_transno" to Query.Direction.DESCENDING
                    )
                )
                val document = querySnapshot?.firstOrNull()
                val invoiceHeader = document?.toObject(InvoiceHeader::class.java)
                invoiceHeader?.invoiceHeadDocumentId = document?.id
                return invoiceHeader
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
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "hi_cmp_id",
                            SettingsModel.getCompanyID()
                        ),Filter.notEqualTo(
                            "hi_ta_name",
                            null
                        ),Filter.equalTo(
                            "hi_transno",
                            null
                        )
                    )
                )
                val size = querySnapshot?.size()?:0
                val tables = mutableListOf<TableModel>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
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
                        val where = "ta_cmp_id='${SettingsModel.getCompanyID()}' AND ta_hiid IS NOT NULL"
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
                                        it.getStringValue("ta_type"),
                                        it.getStringValue("ta_hiid"),
                                        it.getStringValue("ta_userstamp"),
                                        it.getIntValue("ta_locked")
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
                        val where = " ta_hiid IS NOT NULL"
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
                                        it.getStringValue("ta_type"),
                                        it.getStringValue("ta_hiid"),
                                        it.getStringValue("ta_userstamp"),
                                        it.getIntValue("ta_locked")
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

    override suspend fun lockTable(
            tableId: String?,
            tableName: String
    ): String? {
        if (SettingsModel.isConnectedToSqlServer()) {
            if (!tableId.isNullOrEmpty()) {
                if (SettingsModel.isSqlServerWebDb) {
                    updateTableLock(
                        tableId,
                        "Busy",
                        1
                    )
                } else {
                    updateTableLock(
                        tableId,
                        null,
                        1
                    )
                }
                return tableId
            } else if (tableName.isNotEmpty()) {
                return if (SettingsModel.isSqlServerWebDb) {
                    insertTable(
                        null,
                        tableName,
                        "Busy",
                        1
                    )
                } else {
                    insertTable(
                        null,
                        tableName,
                        null,
                        1
                    )
                }
            }
        }
        return null
    }

    override suspend fun unLockTable(
            invoiceId: String,
            tableId: String,
            tableType: String?
    ) {
        if (SettingsModel.isConnectedToSqlServer()) {
            if (tableId.isNotEmpty()) {
                if (SettingsModel.isSqlServerWebDb) {
                    if (invoiceId.isEmpty() && tableType?.equals("temp") == true) {
                        deleteTable(tableId)
                    } else {
                        updateTableLock(
                            tableId,
                            "Free",
                            0
                        )
                    }
                } else {
                    updateTableLock(
                        tableId,
                        null,
                        0
                    )
                }
            }
        }
    }

    override suspend fun getInvoiceByTable(
            tableModel: TableModel,
    ): TableInvoiceModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "hi_cmp_id",
                            SettingsModel.getCompanyID()
                        ),Filter.equalTo(
                            "hi_ta_name",
                            tableModel.table_name
                        ),Filter.equalTo(
                            "hi_transno",
                            null
                        )
                    )
                )
                val document = querySnapshot?.firstOrNull()
                if (document != null) {
                    val obj = document.toObject(InvoiceHeader::class.java)
                    obj.invoiceHeadDocumentId = document.id
                    return TableInvoiceModel(obj)
                }
                return TableInvoiceModel(InvoiceHeader())
            }

            CONNECTION_TYPE.LOCAL.key -> {
                val invoice = invoiceHeaderDao.getInvoiceByTable(
                    tableModel.table_name,
                    SettingsModel.getCompanyID() ?: ""
                ) ?: InvoiceHeader()
                return TableInvoiceModel(invoice)
            }

            else -> {
                val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                var finalTableModel = tableModel
                try {
                    if (tableModel.table_id.isEmpty()) {
                        finalTableModel = getTableIdByNumber(tableModel.table_name) ?: tableModel
                    }

                    if (finalTableModel.table_locked == 1 && finalTableModel.table_id.isNotEmpty()) {
                        // if table is locked but not related to any invoice
                        return if (finalTableModel.table_user == SettingsModel.currentUser?.userUsername) {
                            //same user
                            TableInvoiceModel(
                                InvoiceHeader(
                                    invoiceHeadTableId = finalTableModel.table_id,
                                    invoiceHeadTaName = finalTableModel.table_name,
                                    invoiceHeadTableType = finalTableModel.table_type
                                )
                            )
                        } else {
                            //another user
                            TableInvoiceModel(lockedByUser = finalTableModel.table_user)
                        }
                    }
                    val subQuery = if (!finalTableModel.table_inv_id.isNullOrEmpty()) {
                        "hi_id = '${finalTableModel.table_inv_id}'"
                    } else if (finalTableModel.table_id.isNotEmpty()) {
                        "(hi_ta_name = '${finalTableModel.table_id}' OR hi_ta_name = '${finalTableModel.table_name}')"
                    } else {
                        "hi_ta_name = '${finalTableModel.table_name}'"
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
                            invoiceHeader.invoiceHeadTableId = finalTableModel.table_id
                            invoiceHeader.invoiceHeadTaName = finalTableModel.table_name
                            invoiceHeader.invoiceHeadTableType = finalTableModel.table_type
                            invoiceHeaders.add(invoiceHeader)
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return if (invoiceHeaders.size > 0) {
                    TableInvoiceModel(invoiceHeaders[0])
                } else {
                    TableInvoiceModel(
                        InvoiceHeader(
                            invoiceHeadTableId = finalTableModel.table_id,
                            invoiceHeadTaName = finalTableModel.table_name,
                            invoiceHeadTableType = finalTableModel.table_type
                        )
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
            val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                collection = "in_hinvoice",
                filters = mutableListOf(
                    Filter.equalTo(
                        "hi_cmp_id",
                        SettingsModel.getCompanyID()
                    ),Filter.greaterThanOrEqualTo(
                        "hi_timestamp",
                        from
                    ),Filter.lessThan(
                        "hi_timestamp",
                        to
                    )
                )
            )
          val size = querySnapshot?.size()?:0
            val invoices = mutableListOf<InvoiceHeader>()
            if (size > 0) {
                for (document in querySnapshot!!) {
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
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "hi_userstamp",
                            userId
                        )
                    )
                )
                val document = querySnapshot?.firstOrNull()
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
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "in_hinvoice",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "hi_tp_name",
                            clientId
                        )
                    )
                )
                val document = querySnapshot?.firstOrNull()
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

    private fun fillParams(
            obj: ResultSet,
            ttCodeKey: String = "hi_tt_code"
    ): InvoiceHeader {
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
                            it.getStringValue("ta_hiid"),
                            it.getStringValue("ta_userstamp"),
                            it.getIntValue("ta_locked")
                        )
                    } else {
                        TableModel(
                            it.getStringValue("ta_name"),
                            it.getStringValue("ta_name"),
                            it.getStringValue("ta_type"),
                            it.getStringValue("ta_hiid"),
                            it.getStringValue("ta_userstamp"),
                            it.getIntValue("ta_locked")
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

    private fun insertByProcedure(invoiceHeader: InvoiceHeader): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
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
                SettingsModel.defaultSqlServerWarehouse,
                SettingsModel.defaultSqlServerBranch,
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
                getRateTax(),//rate tax
                0,//tips
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                0,//mincharge
                null,//hi_employee
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
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTaxAmt,
                    decimal
                ),
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTax1Amt,
                    decimal
                ),
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTax2Amt,
                    decimal
                )
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
                SettingsModel.defaultSqlServerWarehouse,
                SettingsModel.defaultSqlServerBranch,
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
                getRateTax(),//rate tax
                0,//tips
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                0,//mincharge
                null,//hi_employee
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) null else 1,//delivered
                SettingsModel.currentUser?.userUsername,//hi_userstamp
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
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTaxAmt,
                    decimal
                ),
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTax1Amt,
                    decimal
                ),
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTax2Amt,
                    decimal
                )
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_hinvoice",
            parameters
        )
        return if (queryResult.succeed) {
            invoiceHeader.invoiceHeadId = queryResult.result ?: ""
            DataModel(invoiceHeader)
        } else {
            DataModel(
                invoiceHeader,
                false
            )
        }
    }

    private fun updateByProcedure(invoiceHeader: InvoiceHeader): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
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
                SettingsModel.defaultSqlServerWarehouse,
                SettingsModel.defaultSqlServerBranch,
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
                getRateTax(),//rate tax
                0,//tips
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                0,//mincharge
                null,//hi_employee
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) null else 1,//delivered
                SettingsModel.currentUser?.userUsername,//hi_userstamp
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
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTaxAmt,
                    decimal
                ),
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTax1Amt,
                    decimal
                ),
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTax2Amt,
                    decimal
                )
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
                SettingsModel.defaultSqlServerWarehouse,
                SettingsModel.defaultSqlServerBranch,
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
                getRateTax(),//rate tax
                0,//tips
                invoiceHeader.invoiceHeadTableId ?: invoiceHeader.invoiceHeadTaName,
                invoiceHeader.invoiceHeadClientsCount,
                0,//mincharge
                null,//hi_employee
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) null else 1,//delivered
                SettingsModel.currentUser?.userUsername,//hi_userstamp
                invoiceHeader.invoiceHeadChange,
                Timestamp(System.currentTimeMillis()),//hi_valuedate
                0,//hi_smssent
                null,//hi_pathtodoc
                null,//hi_cashback
                1,//hi_ratetaxf
                invoiceHeader.invoiceHeadRate,//hi_ratetaxs
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTaxAmt,
                    decimal
                ),
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTax1Amt,
                    decimal
                ),
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTax2Amt,
                    decimal
                )
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updin_hinvoice",
            parameters
        )
        return if (queryResult.succeed) {
            DataModel(invoiceHeader)
        } else {
            DataModel(
                invoiceHeader,
                false
            )
        }
    }

    private fun getRateTax(): Double {
        val fallback = 1.0
        val taxCurrency = SettingsModel.currentCompany?.companyCurCodeTax ?: return fallback
        val secondCurrency = SettingsModel.currentCurrency?.currencyDocumentId ?: return fallback
        if (taxCurrency == secondCurrency) {
            return 1.0 / (SettingsModel.currentCurrency?.currencyRate ?: 1.0)
        }
        return fallback
    }

    private fun insertTable(
            invoiceHeaderId: String?,
            tableName: String,
            tableStatus: String?,
            locked: Int,
    ): String {
        var fallback = ""
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                "null_string_output",//ta_name
                null,//ta_ps_section
                "temp",//type
                0,//ta_x1
                0,//ta_y1
                1,//ta_x2
                1,//ta_y2
                invoiceHeaderId,//ta_hiid
                tableStatus,//ta_status
                tableName,//ta_newname
                SettingsModel.getCompanyID(),//ta_cmp_id
                SettingsModel.currentUser?.userGrpDesc,//ta_grp_desc
                locked,//ta_locked
                Timestamp(System.currentTimeMillis()),//ta_timestamp
                SettingsModel.currentUser?.userUsername,//ta_userstamp
                null,//ta_rotationangle
            )
        } else {
            fallback = tableName
            listOf(
                tableName,//ta_name
                null,//ta_ps_section
                "table",//type
                0,//ta_x1
                0,//ta_y1
                0,//ta_x2
                0,//ta_y2
                invoiceHeaderId,//ta_hiid
                tableStatus,//ta_status
                Timestamp(System.currentTimeMillis()),//ta_timestamp
                SettingsModel.currentUser?.userUsername,//ta_userstamp
                Utils.getDeviceID(App.getInstance()),//ta_station
                locked,//ta_locked
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addpos_table",
            parameters
        )
        if (queryResult.succeed) {
            return queryResult.result ?: fallback
        }
        return fallback
    }

    private fun updateTable(
            invoiceHeaderId: String?,
            tableId: String,
            tableStatus: String?,
            locked: Int,
    ) {
        if (SettingsModel.isSqlServerWebDb) {
            SQLServerWrapper.update(
                "pos_table",
                listOf(
                    "ta_status",
                    "ta_hiid",
                    "ta_locked",
                    "ta_timestamp",
                    "ta_userstamp"
                ),
                listOf(
                    tableStatus,
                    invoiceHeaderId,
                    locked,
                    Timestamp(System.currentTimeMillis()),
                    SettingsModel.currentUser?.userUsername
                ),
                "ta_name = '$tableId'"
            )
        } else {
            SQLServerWrapper.update(
                "pos_table",
                listOf(
                    "ta_status",
                    "ta_hiid",
                    "ta_locked",
                    "ta_station",
                    "ta_timestamp",
                    "ta_userstamp"
                ),
                listOf(
                    tableStatus,
                    invoiceHeaderId,
                    locked,
                    Utils.getDeviceID(App.getInstance()),
                    Timestamp(System.currentTimeMillis()),
                    SettingsModel.currentUser?.userUsername
                ),
                "ta_name = '$tableId'"
            )
        }

        /*val parameters = if (SettingsModel.isSqlServerWebDb) {
        listOf(
            tableId,
            null,//ta_ps_section
            "temp",//type
            0,//ta_x1
            0,//ta_y1
            1,//ta_x2
            1,//ta_y2
            invoiceHeaderId,//ta_hiid
            tableStatus,//ta_status
            tableName,//ta_newname
            SettingsModel.currentUser?.userGrpDesc,//ta_grp_desc
            locked,//ta_locked
            Timestamp(System.currentTimeMillis()),//ta_timestamp
            SettingsModel.currentUser?.userUsername,//ta_userstamp
            null,//ta_rotationangle
        )
    } else {
        listOf(
            tableName,//ta_name
            tableName,//ta_name
            null,//ta_ps_section
            "table",//type
            0,//ta_x1
            0,//ta_y1
            0,//ta_x2
            0,//ta_y2
            invoiceHeaderId,//ta_hiid
            tableStatus,//ta_status
            locked,//ta_locked
        )
    }
     SQLServerWrapper.executeProcedure(
        "updpos_table",
        parameters
    ) */
    }

    private fun updateTableLock(
            tableId: String,
            tableStatus: String?,
            locked: Int,
    ) {
        if (SettingsModel.isSqlServerWebDb) {
            SQLServerWrapper.update(
                "pos_table",
                listOf(
                    "ta_status",
                    "ta_locked",
                    "ta_timestamp",
                    "ta_userstamp"
                ),
                listOf(
                    tableStatus,
                    locked,
                    Timestamp(System.currentTimeMillis()),
                    SettingsModel.currentUser?.userUsername
                ),
                "ta_name = '$tableId'"
            )
        } else {
            SQLServerWrapper.update(
                "pos_table",
                listOf(
                    "ta_status",
                    "ta_locked",
                    "ta_station",
                    "ta_timestamp",
                    "ta_userstamp"
                ),
                listOf(
                    tableStatus,
                    locked,
                    Utils.getDeviceID(App.getInstance()),
                    Timestamp(System.currentTimeMillis()),
                    SettingsModel.currentUser?.userUsername
                ),
                "ta_name = '$tableId'"
            )
        }
    }

    private fun deleteTable(
            tableName: String
    ) {
        SQLServerWrapper.executeProcedure(
            "delpos_table",
            listOf(
                tableName,//ta_name
            )
        )
    }
}