package com.grid.pos.data.Invoice

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class InvoiceRepositoryImpl(
        private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    override suspend fun insert(
            invoice: Invoice
    ): Invoice {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("in_invoice")
                    .add(invoice.getMap()).await()
                invoice.invoiceDocumentId = docRef.id
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceDao.insert(invoice)
            }

            else -> {
                insertByProcedure(invoice)
            }
        }

        return invoice
    }

    override suspend fun delete(
            invoice: Invoice
    ) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                invoice.invoiceDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("in_invoice").document(it).delete()
                        .await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceDao.delete(invoice)
            }

            else -> {
                SQLServerWrapper.executeProcedure(
                    "delin_invoice",
                    listOf(
                        invoice.invoiceId,
                        SettingsModel.currentUser?.userUsername
                    )
                )
            }
        }
    }

    override suspend fun update(
            invoice: Invoice
    ) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                invoice.invoiceDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("in_invoice").document(it)
                        .update(invoice.getMap()).await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                invoiceDao.update(invoice)
            }

            else -> {
                updateByProcedure(invoice)
            }
        }
    }

    override suspend fun getAllInvoices(
            invoiceHeaderId: String
    ): MutableList<Invoice> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_invoice")
                    .whereEqualTo(
                        "in_hi_id",
                        invoiceHeaderId
                    ).get().await()
                val invoiceItems = mutableListOf<Invoice>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Invoice::class.java)
                        if (obj.invoiceId.isNotEmpty()) {
                            obj.invoiceDocumentId = document.id
                            invoiceItems.add(obj)
                        }
                    }
                }
                return invoiceItems
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceDao.getAllInvoiceItems(invoiceHeaderId)
            }

            else -> {
                val invoices: MutableList<Invoice> = mutableListOf()
                try {
                    val where = "in_hi_id = '$invoiceHeaderId'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_invoice",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            invoices.add(fillParams(it))
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return invoices
            }
        }
    }

    override suspend fun getInvoicesByIds(
            ids: List<String>
    ): MutableList<Invoice> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_invoice")
                    .whereIn(
                        "in_hi_id",
                        ids
                    ).get().await()
                val invoiceItems = mutableListOf<Invoice>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Invoice::class.java)
                        if (obj.invoiceId.isNotEmpty()) {
                            obj.invoiceDocumentId = document.id
                            invoiceItems.add(obj)
                        }
                    }
                }
                return invoiceItems
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceDao.getInvoicesByIds(ids)
            }

            else -> {
                val invoices: MutableList<Invoice> = mutableListOf()
                try {
                    val where = "in_hi_id IN (${ids.joinToString(", ")})"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_invoice",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            invoices.add(fillParams(it))
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return invoices
            }
        }
    }

    override suspend fun getOneInvoiceByItemID(itemId: String): Invoice? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("in_invoice")
                    .whereEqualTo(
                        "in_it_id",
                        itemId
                    ).limit(1).get().await()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Invoice::class.java)
                        if (obj.invoiceId.isNotEmpty()) {
                            obj.invoiceDocumentId = document.id
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return invoiceDao.getOneInvoiceByItemId(itemId)
            }

            else -> {
                return null
            }
        }
    }

    private fun fillParams(obj: ResultSet): Invoice {
        return Invoice().apply {
            invoiceId = obj.getString("in_id")
            invoiceHeaderId = obj.getString("in_hi_id")
            invoiceItemId = obj.getString("in_it_id")
            invoiceQuantity = obj.getDouble("in_qty")
            invoicePrice = obj.getDouble("in_price")
            invoiceDiscount = obj.getDouble("in_disc")
            invoiceDiscamt = obj.getDouble("in_discamt")
            invoiceTax = obj.getDouble("in_vat")
            invoiceTax1 = obj.getDouble("in_tax1")
            invoiceTax2 = obj.getDouble("in_tax2")
            invoiceNote = obj.getString("in_note")
            invoiceCost = obj.getDouble("in_cost")
            invoiceRemQty = obj.getDouble("in_remqty")
            invoiceExtraName = obj.getString("in_extraname")
            val timeStamp = obj.getObject("in_timestamp")
            invoiceTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            invoiceDateTime = invoiceTimeStamp!!.time
            invoiceUserStamp = obj.getString("in_userstamp")
        }
    }

    private fun insertByProcedure(invoice: Invoice) {
        val parameters = mutableListOf(
            "null",//in_id
            invoice.invoiceHeaderId,
            1,//in_group
            invoice.invoiceItemId,
            invoice.invoiceQuantity,
            invoice.invoicePrice,
            invoice.getDiscount(),
            invoice.getDiscountAmount(),
            invoice.getVat(),
            SettingsModel.defaultWarehouse,
            invoice.invoiceNote,
            "null",//fromin_id
            SettingsModel.currentUser?.userUsername,//in_userstamp
            SettingsModel.currentCompany?.cmp_multibranchcode,//branchcode
            invoice.in_it_div_name,//it_div_name
            "null",//serialnumber
            "null",//in_qtyratio
            "null",//in_counter
            invoice.invoiceExtraName,
            "null",//in_expirydate
            "null",//in_packs
            "null",//in_commission
            "null",//counterenable
            false,//autocalccost
            invoice.in_cashback,
            invoice.getTax1(),
            invoice.getTax2()
        )
        invoice.invoiceId = SQLServerWrapper.executeProcedure(
            "addin_invoice",
            parameters,
            true
        ) ?: ""
    }

    private fun updateByProcedure(invoice: Invoice) {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            mutableListOf(
                invoice.invoiceId,
                invoice.invoiceHeaderId,
                1,//in_group
                invoice.invoiceItemId,
                invoice.invoiceQuantity,
                invoice.invoicePrice,
                invoice.getDiscount(),
                invoice.getDiscountAmount(),
                invoice.getVat(),
                SettingsModel.defaultWarehouse,
                invoice.invoiceNote,
                "null",//fromin_id
                SettingsModel.currentUser?.userUsername,//in_userstamp
                invoice.in_it_div_name,//it_div_name
                "null",//in_qtyratio
                "null",//in_counter
                invoice.invoiceExtraName,
                "null",//in_packs
                "null",//in_commission
                invoice.in_cashback,
                "null",//in_tax3
                "null",//in_disc1
                "null",//in_disc2
                "null",//in_disc3
                "null",//in_order
                invoice.getTax1(),
                invoice.getTax2()
            )
        } else {
            mutableListOf(
                invoice.invoiceId,
                invoice.invoiceHeaderId,
                1,//in_group
                invoice.invoiceItemId,
                invoice.invoiceQuantity,
                invoice.invoicePrice,
                invoice.getDiscount(),
                invoice.getDiscountAmount(),
                invoice.getVat(),
                SettingsModel.defaultWarehouse,
                invoice.invoiceNote,
                "null",//fromin_id
                SettingsModel.currentUser?.userUsername,//in_userstamp
                invoice.in_it_div_name,//it_div_name
                "null",//in_qtyratio
                "null",//in_counter
                invoice.invoiceExtraName,
                "null",//in_packs
                "null",//in_commission
                invoice.in_cashback,
                invoice.getTax1(),
                invoice.getTax2()
            )
        }
        SQLServerWrapper.executeProcedure(
            "updin_invoice",
            parameters
        )
    }
}