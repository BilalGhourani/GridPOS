package com.grid.pos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyDao
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyDao
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyDao
import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Invoice.InvoiceDao
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderDao
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemDao
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.data.PosPrinter.PosPrinterDao
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.data.PosReceipt.PosReceiptDao
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyDao
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserDao

@Database(
    entities = [Family::class, Item::class,
        PosPrinter::class, PosReceipt::class,
        ThirdParty::class, User::class,
        Currency::class, Company::class,
        Invoice::class, InvoiceHeader::class],
    version = 1,
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
}