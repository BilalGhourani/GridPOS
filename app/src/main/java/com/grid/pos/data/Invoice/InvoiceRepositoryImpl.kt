package com.grid.pos.data.Invoice

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
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
                SQLServerWrapper.insert(
                    "in_invoice",
                    getColumns(),
                    getValues(invoice)
                )
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
                SQLServerWrapper.delete(
                    "in_invoice",
                    "in_id = '${invoice.invoiceId}'"
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
                SQLServerWrapper.update(
                    "in_invoice",
                    getColumns(),
                    getValues(invoice),
                    "in_id = '${invoice.invoiceId}'"
                )
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
                val where = "in_hi_id = '$invoiceHeaderId'"
                val dbResult = SQLServerWrapper.getListOf(
                    "in_invoice",
                    "",
                    mutableListOf("*"),
                    where
                )
                val invoices: MutableList<Invoice> = mutableListOf()
                dbResult.forEach { obj ->
                    invoices.add(fillParams(obj))
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
                val where = "in_hi_id IN (${ids.joinToString(", ")})"
                val dbResult = SQLServerWrapper.getListOf(
                    "in_invoice",
                    "",
                    mutableListOf("*"),
                    where
                )
                val invoices: MutableList<Invoice> = mutableListOf()
                dbResult.forEach { obj ->
                    invoices.add(fillParams(obj))
                }
                return invoices
            }
        }
    }

    private fun fillParams(obj: JSONObject): Invoice {
        return Invoice().apply {
            invoiceId = obj.optString("in_id")
            invoiceHeaderId = obj.optString("in_hi_id")
            invoiceItemId = obj.optString("in_it_id")
            invoiceQuantity = obj.optDouble("in_qty")
            invoicePrice = obj.optDouble("in_price")
            invoiceDiscount = obj.optDouble("in_disc")
            invoiceDiscamt = obj.optDouble("in_discamt")
            invoiceTax = obj.optDouble("in_vat")
            invoiceTax1 = obj.optDouble("in_tax1")
            invoiceTax2 = obj.optDouble("in_tax2")
            invoiceNote = obj.optString("in_note")
            invoiceCost = obj.optDouble("in_cost")
            invoiceRemQty = obj.optDouble("in_remqty")
            invoiceExtraName = obj.optString("in_extraname")
            val timeStamp = obj.opt("in_timestamp")
            invoiceTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            invoiceDateTime = invoiceTimeStamp!!.time
            invoiceUserStamp = obj.optString("in_userstamp")
        }
    }

    private fun getColumns(): List<String> {
        return listOf(
            "in_id",
            "in_hi_id",
            "in_it_id",
            "in_qty",
            "in_price",
            "in_disc",
            "in_discamt",
            "in_vat",
            "in_tax1",
            "in_tax2",
            "in_note",
            "in_cost",
            "in_remqty",
            "in_extraname",
            "in_wa_name",
            "in_timestamp",
            "in_userstamp"
        )
    }

    private fun getValues(invoice: Invoice): List<Any?> {
        return listOf(
            invoice.invoiceId,
            invoice.invoiceHeaderId,
            invoice.invoiceItemId,
            invoice.invoiceQuantity,
            invoice.invoicePrice,
            invoice.invoiceDiscount,
            invoice.invoiceDiscamt,
            invoice.invoiceTax,
            invoice.invoiceTax1,
            invoice.invoiceTax2,
            invoice.invoiceNote,
            invoice.invoiceCost,
            invoice.invoiceRemQty,
            invoice.invoiceExtraName,
            SettingsModel.defaultWarehouse,
            invoice.invoiceTimeStamp,
            invoice.invoiceUserStamp,
        )
    }
}