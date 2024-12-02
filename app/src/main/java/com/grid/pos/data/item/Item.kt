package com.grid.pos.data.item

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.grid.pos.data.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils
import java.util.Date

@Entity(tableName = "st_item")
data class Item(
        /**
         * Item Id
         * */
        @PrimaryKey
        @ColumnInfo(name = "it_id")
        @set:PropertyName("it_id")
        @get:PropertyName("it_id")
        var itemId: String,

        @Ignore
        @get:Exclude
        var itemDocumentId: String? = null,

        @Ignore
        @get:Exclude
        var it_div_name: String? = null,

        @Ignore
        @get:Exclude
        var it_cashback: Double = 0.0,

        /**
         * Item name
         * */
        @ColumnInfo(name = "it_cmp_id")
        @set:PropertyName("it_cmp_id")
        @get:PropertyName("it_cmp_id")
        var itemCompId: String? = null,

        /**
         * Item name
         * */
        @ColumnInfo(name = "it_fa_id")
        @set:PropertyName("it_fa_id")
        @get:PropertyName("it_fa_id")
        var itemFaId: String? = null,

        /**
         * Item name
         * */
        @ColumnInfo(name = "it_name")
        @set:PropertyName("it_name")
        @get:PropertyName("it_name")
        var itemName: String? = null,

        /**
         * Item barcode
         * */
        @ColumnInfo(name = "it_barcode")
        @set:PropertyName("it_barcode")
        @get:PropertyName("it_barcode")
        var itemBarcode: String? = null,

        /**
         * Item unit price
         * */
        @ColumnInfo(name = "it_unitprice")
        @set:PropertyName("it_unitprice")
        @get:PropertyName("it_unitprice")
        var itemUnitPrice: Double = 0.0,

        @Ignore
        @get:Exclude
        var itemRealUnitPrice: Double = 0.0,

        /**
         * Item unit price
         * */
        @ColumnInfo(name = "it_cur_code")
        @set:PropertyName("it_cur_code")
        @get:PropertyName("it_cur_code")
        var itemCurrencyId: String? = null,

        @Ignore
        @get:Exclude
        var itemCurrencyCode: String? = null,

        /**
         * Item tax
         * */
        @ColumnInfo(name = "it_tax")
        @set:PropertyName("it_tax")
        @get:PropertyName("it_tax")
        var itemTax: Double = 0.0,

        /**
         * Item tax 1
         * */
        @ColumnInfo(name = "it_tax1")
        @set:PropertyName("it_tax1")
        @get:PropertyName("it_tax1")
        var itemTax1: Double = 0.0,

        /**
         * Item tax 2
         * */
        @ColumnInfo(name = "it_tax2")
        @set:PropertyName("it_tax2")
        @get:PropertyName("it_tax2")
        var itemTax2: Double = 0.0,

        /**
         * related Item pos printer id
         * */
        @ColumnInfo(name = "it_printer")
        @set:PropertyName("it_printer")
        @get:PropertyName("it_printer")
        var itemPrinter: String? = null,

        /**
         * Item open quantity
         * */
        @ColumnInfo(name = "it_openqty")
        @set:PropertyName("it_openqty")
        @get:PropertyName("it_openqty")
        var itemOpenQty: Double = 0.0,

        /**
         * Item rem quantity
         * */
        @ColumnInfo(name = "it_remqty")
        @set:PropertyName("it_remqty")
        @get:PropertyName("it_remqty")
        var itemRemQty: Double = 0.0,

        /**
         * Item open cost
         * */
        @ColumnInfo(name = "it_opencost")
        @set:PropertyName("it_opencost")
        @get:PropertyName("it_opencost")
        var itemOpenCost: Double = 0.0,

        /**
         * related Item POS id
         * */
        @ColumnInfo(name = "it_pos")
        @set:PropertyName("it_pos")
        @get:PropertyName("it_pos")
        var itemPos: Boolean = false,

        /**
         * family image
         * */
        @ColumnInfo(name = "it_image")
        @set:PropertyName("it_image")
        @get:PropertyName("it_image")
        var itemImage: String? = null,

        /**
         * Item button color
         * */
        @ColumnInfo(name = "it_btncolor")
        @set:PropertyName("it_btncolor")
        @get:PropertyName("it_btncolor")
        var itemBtnColor: String? = null,

        /**
         * Item button text color
         * */
        @ColumnInfo(name = "it_btntextcolor")
        @set:PropertyName("it_btntextcolor")
        @get:PropertyName("it_btntextcolor")
        var itemBtnTextColor: String? = null,

        /**
         * Item userstamp
         * */
        @ColumnInfo(name = "it_userstamp")
        @set:PropertyName("it_userstamp")
        @get:PropertyName("it_userstamp")
        var itemUserStamp: String? = null,

        /**
         * Item timestamp
         * */
        @Ignore
        @set:PropertyName("it_timestamp")
        @get:PropertyName("it_timestamp")
        @ServerTimestamp
        var itemTimeStamp: Date? = null,

        /**
         * Item timestamp
         * */
        @ColumnInfo(name = "it_datetime")
        @set:PropertyName("it_datetime")
        @get:PropertyName("it_datetime")
        var itemDateTime: Long = System.currentTimeMillis(),

        @Ignore
        @get:Exclude
        var selected: Boolean = false,

        ) : DataModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return itemId
    }

    @Exclude
    override fun getName(): String {
        return itemName ?: ""
    }

    @Exclude
    override fun search(key: String): Boolean {
        val searchable = "${itemName ?: ""} ${itemBarcode ?: ""} "
        return searchable.contains(
            key,
            ignoreCase = true
        )
    }

    @Exclude
    fun getFullItemImage(): String {
        itemImage?.let {
            if (it.startsWith("/")) {
                return "file://$it"
            }
            return it
        }
        return ""
    }

    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            itemDocumentId.isNullOrEmpty()
        } else {
            itemId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (itemId.isEmpty()) {
            itemId = Utils.generateRandomUuidString()
        }
        itemCompId = SettingsModel.getCompanyID()
        itemUserStamp = SettingsModel.currentUserId
        itemRemQty = itemOpenQty
    }

    @Exclude
    fun didChanged(item: Item): Boolean {
        return !item.itemName.equals(itemName) || !item.itemFaId.equals(itemFaId) || !item.itemBarcode.equals(itemBarcode) || !item.itemUnitPrice.equals(itemUnitPrice) || !item.itemTax.equals(itemTax) || !item.itemTax1.equals(itemTax1) || !item.itemTax2.equals(itemTax2) || !item.itemPrinter.equals(itemPrinter) || !item.itemOpenQty.equals(itemOpenQty) || !item.itemRemQty.equals(itemRemQty) || !item.itemOpenCost.equals(itemOpenCost) || item.itemPos != itemPos || !item.itemImage.equals(itemImage) || !item.itemBtnColor.equals(itemBtnColor) || !item.itemBtnTextColor.equals(itemBtnTextColor)
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "it_id" to itemId,
            "it_cmp_id" to itemCompId,
            "it_fa_id" to itemFaId,
            "it_name" to itemName,
            "it_barcode" to itemBarcode,
            "it_unitprice" to itemUnitPrice,
            "it_cur_code" to itemCurrencyId,
            "it_tax" to itemTax,
            "it_tax1" to itemTax1,
            "it_tax2" to itemTax2,
            "it_printer" to itemPrinter,
            "it_openqty" to itemOpenQty,
            "it_remqty" to itemRemQty,
            "it_opencost" to itemOpenCost,
            "it_pos" to itemPos,
            "it_image" to itemImage,
            "it_btncolor" to itemBtnColor,
            "it_btntextcolor" to itemBtnTextColor,
            "it_userstamp" to itemUserStamp,
            "it_timestamp" to FieldValue.serverTimestamp(),
            "it_datetime" to itemDateTime,
        )
    }
}
