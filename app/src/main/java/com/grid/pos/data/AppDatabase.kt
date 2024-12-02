package com.grid.pos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.grid.pos.data.company.Company
import com.grid.pos.data.company.CompanyDao
import com.grid.pos.data.currency.Currency
import com.grid.pos.data.currency.CurrencyDao
import com.grid.pos.data.family.Family
import com.grid.pos.data.family.FamilyDao
import com.grid.pos.data.invoice.Invoice
import com.grid.pos.data.invoice.InvoiceDao
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.invoiceHeader.InvoiceHeaderDao
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemDao
import com.grid.pos.data.payment.Payment
import com.grid.pos.data.payment.PaymentDao
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.data.posPrinter.PosPrinterDao
import com.grid.pos.data.posReceipt.PosReceipt
import com.grid.pos.data.posReceipt.PosReceiptDao
import com.grid.pos.data.receipt.Receipt
import com.grid.pos.data.receipt.ReceiptDao
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.thirdParty.ThirdPartyDao
import com.grid.pos.data.user.User
import com.grid.pos.data.user.UserDao

@Database(
    entities = [Family::class, Item::class, PosPrinter::class, PosReceipt::class, ThirdParty::class, User::class, Currency::class, Company::class, Invoice::class, InvoiceHeader::class, Payment::class, Receipt::class],
    version = 7,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract val categoryDao: FamilyDao
    abstract val itemDao: ItemDao
    abstract val posReceiptDao: PosReceiptDao
    abstract val posPrinterDao: PosPrinterDao
    abstract val thirdPartyDao: ThirdPartyDao
    abstract val userDao: UserDao
    abstract val currencyDao: CurrencyDao
    abstract val companyDao: CompanyDao
    abstract val invoiceDao: InvoiceDao
    abstract val invoiceHeaderDao: InvoiceHeaderDao
    abstract val paymentDao: PaymentDao
    abstract val receiptDao: ReceiptDao
}

val MIGRATION_1_2: Migration = object : Migration(
    1,
    2
) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'company' ADD COLUMN 'cmp_upwithtax' INTEGER NOT NULL DEFAULT 0")
    }
}
val MIGRATION_2_3: Migration = object : Migration(
    2,
    3
) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'in_hinvoice' ADD COLUMN 'hi_printed' INTEGER NOT NULL DEFAULT 0")
    }
}
val MIGRATION_3_4: Migration = object : Migration(
    3,
    4
) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'st_item' ADD COLUMN 'it_cur_code' TEXT")
    }
}

val MIGRATION_4_5: Migration = object : Migration(
    4,
    5
) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'st_item' ADD COLUMN 'it_remqty' REAL NOT NULL DEFAULT 0.0")
    }
}

val MIGRATION_5_6: Migration = object : Migration(
    5,
    6
) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'thirdparty' ADD COLUMN 'tp_cse' TEXT")
    }
}

val MIGRATION_6_7 = object : Migration(
    6,
    7
) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the Payment table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `payment` (
                `pay_id` TEXT PRIMARY KEY NOT NULL,
                `pay_cmp_id` TEXT,
                `pay_type` TEXT,
                `pay_tt_code` TEXT,
                `pay_transno` TEXT,
                `pay_tp_name` TEXT,
                `pay_cur_code` TEXT,
                `pay_amt` REAL NOT NULL,
                `pay_amtf` REAL NOT NULL,
                `pay_amts` REAL NOT NULL,
                `pay_desc` TEXT,
                `pay_note` TEXT,
                `pay_datetime` INTEGER NOT NULL,
                `pay_userstamp` TEXT
            )
        """
        )

        // Create the Receipt table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `receipt` (
                `rec_id` TEXT PRIMARY KEY NOT NULL,
                `rec_cmp_id` TEXT,
                `rec_type` TEXT,
                `rec_tt_code` TEXT,
                `rec_transno` TEXT,
                `rec_tp_name` TEXT,
                `rec_cur_code` TEXT,
                `rec_amt` REAL NOT NULL,
                `rec_amtf` REAL NOT NULL,
                `rec_amts` REAL NOT NULL,
                `rec_desc` TEXT,
                `rec_note` TEXT,
                `rec_datetime` INTEGER NOT NULL,
                `rec_userstamp` TEXT
            )
        """
        )
    }
}
