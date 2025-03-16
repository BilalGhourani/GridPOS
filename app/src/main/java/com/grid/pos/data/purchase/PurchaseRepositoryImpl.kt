package com.grid.pos.data.purchase

import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getIntValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import java.sql.ResultSet
import java.util.Date

class PurchaseRepositoryImpl : PurchaseRepository {
    override suspend fun insert(purchase: Purchase): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(purchase)
            }

            else -> {
                insertByProcedure(purchase)
            }
        }
    }

    override suspend fun delete(purchase: Purchase): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(purchase)
            }

            else -> {
                deleteByProcedure(purchase)
            }
        }
    }

    override suspend fun update(purchase: Purchase): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(purchase)
            }

            else -> {
                updateByProcedure(purchase)
            }
        }
    }

    override suspend fun getAllPurchases(purchaseHeaderId: String): MutableList<Purchase> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                return mutableListOf()
            }

            else -> {
                val purchases: MutableList<Purchase> = mutableListOf()
                try {
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_purchase",
                        "",
                        mutableListOf("*"),
                        "pu_hp_id='$purchaseHeaderId'",
                       if(SettingsModel.isSqlServerWebDb) "ORDER BY pu_lineno ASC" else "ORDER BY pu_timestamp ASC"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            purchases.add(
                                fillParams(it)
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return purchases
            }
        }
    }

    private fun fillParams(
        obj: ResultSet
    ): Purchase {
        return Purchase().apply {
            purchaseId = obj.getStringValue("pu_id")
            purchaseHpId = obj.getStringValue("pu_hp_id")
            purchaseItId = obj.getStringValue("pu_it_id")
            purchaseQty = obj.getDoubleValue("pu_qty")
            purchasePrice = obj.getDoubleValue("pu_price")
            purchaseDisc = obj.getDoubleValue("pu_disc")
            purchaseDiscAmt = obj.getDoubleValue("pu_discamt")
            purchaseWaName= obj.getStringValue("pu_wa_name")
            purchaseNote= obj.getStringValue("pu_note")
            purchaseItIdInPack = obj.getStringValue("pu_it_idinpack")
            purchaseQtyInPack = obj.getDoubleValue("pu_qtyinpack")
            purchaseCost = obj.getDoubleValue("pu_cost")
            purchaseMcurRate = obj.getDoubleValue("pu_mcurrate")
            purchaseMcurRateF = obj.getDoubleValue("pu_mcurratef")
            purchaseMcurRateS = obj.getDoubleValue("pu_mcurrates")
            purchaseRemQty = obj.getDoubleValue("pu_remqty")
            purchaseRemQtyWa = obj.getDoubleValue("pu_remqtywa")
            purchaseFromPuId = obj.getStringValue("frompu_id")

            val timeStamp = obj.getObjectValue("pu_timestamp")
            purchaseTimestamp =
                if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                    timeStamp as String,
                    "yyyy-MM-dd hh:mm:ss.SSS"
                )
            purchaseUserStamp = obj.getStringValue("pu_userstamp")
            purchaseDivName = obj.getStringValue("pu_div_name")
            purchaseQtyRatio = obj.getDoubleValue("pu_qtyratio")
            purchasePacks = obj.getDoubleValue("pu_packs")
            purchaseHsId = obj.getStringValue("pu_hsid")
            purchaseVat = obj.getDoubleValue("pu_vat")
            purchaseOrder = obj.getIntValue("pu_order")
            purchaseTax1 = obj.getDoubleValue("pu_tax1")
            purchaseTax2 = obj.getDoubleValue("pu_tax2")
            purchaseUnId = obj.getStringValue("pu_un_id")
            purchaseLineNo = obj.getIntValue("pu_lineno")
        }
    }

    private fun insertByProcedure(purchase: Purchase): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//@u_id
                purchase.purchaseHpId,//@pu_hp_id
                purchase.purchaseItId,//@pu_it_id
                purchase.purchaseQty,//@pu_qty
                purchase.purchasePrice,//@pu_price
                purchase.purchaseDisc,//@pu_disc
                purchase.purchaseDiscAmt,//@pu_discamt
                purchase.purchaseWaName,//@pu_wa_name
                purchase.purchaseNote,//@pu_note
                purchase.purchaseFromPuId,//@frompu_id
                SettingsModel.currentUser?.userUsername,//@pu_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                purchase.purchaseDivName,//@pu_div_name
                purchase.purchaseQtyRatio,//@pu_qtyratio
                purchase.purchasePacks,//@pu_packs
                purchase.purchaseVat,//@pu_vat
                purchase.purchaseTax1,//@pu_tax1
                purchase.purchaseTax2,//@pu_tax2
                purchase.purchaseUnId,//@pu_un_id
                purchase.purchaseLineNo,//@pu_lineno
            )
        } else {
            listOf(
                null,//@u_id
                purchase.purchaseHpId,//@pu_hp_id
                purchase.purchaseItId,//@pu_it_id
                purchase.purchaseQty,//@pu_qty
                purchase.purchasePrice,//@pu_price
                purchase.purchaseDisc,//@pu_disc
                purchase.purchaseDiscAmt,//@pu_discamt
                purchase.purchaseWaName,//@pu_wa_name
                purchase.purchaseNote,//@pu_note
                purchase.purchaseFromPuId,//@frompu_id
                SettingsModel.currentUser?.userUsername,//@pu_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                purchase.purchaseDivName,//@pu_div_name
                purchase.purchaseQtyRatio,//@pu_qtyratio
                purchase.purchasePacks,//@pu_packs
                purchase.purchaseVat,//@pu_vat
                purchase.purchaseTax1,//@pu_tax1
                purchase.purchaseTax2,//@pu_tax2
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_purchase",
            parameters
        )
        return if (queryResult.succeed) {
            purchase.purchaseId = queryResult.result ?: ""
            DataModel(purchase)
        } else {
            DataModel(
                purchase,
                false
            )
        }
    }

    private fun updateByProcedure(purchase: Purchase): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                purchase.purchaseId,//@u_id
                purchase.purchaseHpId,//@pu_hp_id
                purchase.purchaseItId,//@pu_it_id
                purchase.purchaseQty,//@pu_qty
                purchase.purchasePrice,//@pu_price
                purchase.purchaseDisc,//@pu_disc
                purchase.purchaseDiscAmt,//@pu_discamt
                purchase.purchaseWaName,//@pu_wa_name
                purchase.purchaseNote,//@pu_note
                purchase.purchaseFromPuId,//@frompu_id
                SettingsModel.currentUser?.userUsername,//@pu_userstamp
                purchase.purchaseDivName,//@pu_div_name
                purchase.purchaseQtyRatio,//@pu_qtyratio
                purchase.purchasePacks,//@pu_packs
                purchase.purchaseVat,//@pu_vat
                null,//@pu_tax3
                null,//@pu_disc1
                null,//@pu_disc2
                null,//@pu_disc3
                purchase.purchaseOrder,//@pu_order
                purchase.purchaseTax1,//@pu_tax1
                purchase.purchaseTax2,//@pu_tax2
                purchase.purchaseUnId,//@pu_un_id
                purchase.purchaseLineNo,//@pu_lineno
            )
        } else {
            listOf(
                purchase.purchaseId,//@u_id
                purchase.purchaseHpId,//@pu_hp_id
                purchase.purchaseItId,//@pu_it_id
                purchase.purchaseQty,//@pu_qty
                purchase.purchasePrice,//@pu_price
                purchase.purchaseDisc,//@pu_disc
                purchase.purchaseDiscAmt,//@pu_discamt
                purchase.purchaseWaName,//@pu_wa_name
                purchase.purchaseNote,//@pu_note
                purchase.purchaseFromPuId,//@frompu_id
                SettingsModel.currentUser?.userUsername,//@pu_userstamp
                purchase.purchaseDivName,//@pu_div_name
                purchase.purchaseQtyRatio,//@pu_qtyratio
                purchase.purchasePacks,//@pu_packs
                purchase.purchaseVat,//@pu_vat
                purchase.purchaseTax1,//@pu_tax1
                purchase.purchaseTax2,//@pu_tax2
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updin_purchase",
            parameters
        )
        return if (queryResult.succeed) {
            DataModel(purchase)
        } else {
            DataModel(
                purchase,
                false
            )
        }
    }

    private fun deleteByProcedure(purchase: Purchase): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delin_purchase",
            listOf(purchase.purchaseId)
        )
        return if (queryResult.succeed) {
            DataModel(purchase)
        } else {
            DataModel(
                purchase,
                false
            )
        }
    }
}