package com.grid.pos.data.Company

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.grid.pos.data.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils

@Entity(tableName = "company")
data class Company(
        /**
         * Company id
         * */
        @PrimaryKey
        @ColumnInfo(name = "cmp_id")
        @set:PropertyName("cmp_id")
        @get:PropertyName("cmp_id")
        var companyId: String,

        @Ignore
        @get:Exclude
        var companyDocumentId: String? = null,

        @Ignore
        @get:Exclude
        var cmp_multibranchcode: String? = null,

        /**
         * Company name
         * */
        @ColumnInfo(name = "cmp_name")
        @set:PropertyName("cmp_name")
        @get:PropertyName("cmp_name")
        var companyName: String? = null,

        /**
         * Company Phone
         * */
        @ColumnInfo(name = "cmp_phone")
        @set:PropertyName("cmp_phone")
        @get:PropertyName("cmp_phone")
        var companyPhone: String? = null,

        /**
         * Company Address
         * */
        @ColumnInfo(name = "cmp_address")
        @set:PropertyName("cmp_address")
        @get:PropertyName("cmp_address")
        var companyAddress: String? = null,

        /**
         * Company VAT Regno
         * */
        @ColumnInfo(name = "cmp_taxregno")
        @set:PropertyName("cmp_taxregno")
        @get:PropertyName("cmp_taxregno")
        var companyTaxRegno: String? = null,

        /**
         * Company VAT
         * */
        @ColumnInfo(name = "cmp_tax")
        @set:PropertyName("cmp_tax")
        @get:PropertyName("cmp_tax")
        var companyTax: Double = 0.0,

        /**
         * Company currency code tax
         * */
        @ColumnInfo(name = "cmp_cur_codetax")
        @set:PropertyName("cmp_cur_codetax")
        @get:PropertyName("cmp_cur_codetax")
        var companyCurCodeTax: String? = null,

        /**
         * Company currency code tax
         * */
        @ColumnInfo(name = "cmp_upwithtax")
        @set:PropertyName("cmp_upwithtax")
        @get:PropertyName("cmp_upwithtax")
        var companyUpWithTax: Boolean = false,

        /**
         * Company Printer
         * */
        @ColumnInfo(name = "cmp_pp_id")
        @set:PropertyName("cmp_pp_id")
        @get:PropertyName("cmp_pp_id")
        var companyPrinterId: String? = null,

        /**
         * Company Email
         * */
        @ColumnInfo(name = "cmp_email")
        @set:PropertyName("cmp_email")
        @get:PropertyName("cmp_email")
        var companyEmail: String? = null,

        /**
         * Company Web
         * */
        @ColumnInfo(name = "cmp_web")
        @set:PropertyName("cmp_web")
        @get:PropertyName("cmp_web")
        var companyWeb: String? = null,

        /**
         * Company Logo
         * */
        @ColumnInfo(name = "cmp_logo")
        @set:PropertyName("cmp_logo")
        @get:PropertyName("cmp_logo")
        var companyLogo: String? = null,

        /**
         * Company SS
         * */
        @ColumnInfo(name = "cmp_ss")
        @set:PropertyName("cmp_ss")
        @get:PropertyName("cmp_ss")
        var companySS: Boolean = false,

        /**
         * Company Country
         * */
        @ColumnInfo(name = "cmp_country")
        @set:PropertyName("cmp_country")
        @get:PropertyName("cmp_country")
        var companyCountry: String? = null,

        /**
         * Company Tax 1
         * */
        @ColumnInfo(name = "cmp_tax1")
        @set:PropertyName("cmp_tax1")
        @get:PropertyName("cmp_tax1")
        var companyTax1: Double = 0.0,

        /**
         * Company Tax 1 Regno
         * */
        @ColumnInfo(name = "cmp_tax1regno")
        @set:PropertyName("cmp_tax1regno")
        @get:PropertyName("cmp_tax1regno")
        var companyTax1Regno: String? = null,

        /**
         * Company Tax 2
         * */
        @ColumnInfo(name = "cmp_tax2")
        @set:PropertyName("cmp_tax2")
        @get:PropertyName("cmp_tax2")
        var companyTax2: Double = 0.0,

        /**
         * Company Tax 2 Regno
         * */
        @ColumnInfo(name = "cmp_tax2regno")
        @set:PropertyName("cmp_tax2regno")
        @get:PropertyName("cmp_tax2regno")
        var companyTax2Regno: String? = null,

        ) : DataModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return companyId
    }

    @Exclude
    override fun getName(): String {
        return companyName ?: ""
    }

    @Exclude
    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }

    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            companyDocumentId.isNullOrEmpty()
        } else {
            companyId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (companyId.isEmpty()) {
            companyId = Utils.generateRandomUuidString()
        }
    }

    @Exclude
    fun didChanged(company: Company): Boolean {
        return !company.companyName.equals(companyName) || !company.companyPhone.equals(companyPhone) || !company.companyAddress.equals(companyAddress) || !company.companyCountry.equals(companyCountry) || !company.companyEmail.equals(companyEmail) || !company.companyWeb.equals(companyWeb) || !company.companyLogo.equals(companyLogo) || !company.companyTaxRegno.equals(companyTaxRegno) || !company.companyTax.equals(companyTax) || !company.companyTax1Regno.equals(companyTax1Regno) || !company.companyTax1.equals(companyTax1) || !company.companyTax2Regno.equals(companyTax2Regno) || !company.companyTax2.equals(companyTax2)
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "cmp_name" to companyName,
            "cmp_phone" to companyPhone,
            "cmp_country" to companyCountry,
            "cmp_address" to companyAddress,
            "cmp_taxregno" to companyTaxRegno,
            "cmp_tax" to companyTax,
            "cmp_cur_codetax" to companyCurCodeTax,
            "cmp_pp_id" to companyPrinterId,
            "cmp_email" to companyEmail,
            "cmp_web" to companyWeb,
            "cmp_logo" to companyLogo,
            "cmp_ss" to companySS,
            "cmp_tax1" to companyTax1,
            "cmp_tax1regno" to companyTax1Regno,
            "cmp_tax2" to companyTax2,
            "cmp_tax2regno" to companyTax2Regno,
        )
    }
}
