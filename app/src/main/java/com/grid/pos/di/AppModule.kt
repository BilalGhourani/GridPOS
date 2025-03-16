package com.grid.pos.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.grid.pos.SharedViewModel
import com.grid.pos.data.AppDatabase
import com.grid.pos.data.company.CompanyRepository
import com.grid.pos.data.company.CompanyRepositoryImpl
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.currency.CurrencyRepositoryImpl
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.family.FamilyRepositoryImpl
import com.grid.pos.data.invoice.InvoiceRepository
import com.grid.pos.data.invoice.InvoiceRepositoryImpl
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepositoryImpl
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.item.ItemRepositoryImpl
import com.grid.pos.data.MIGRATION_1_2
import com.grid.pos.data.MIGRATION_2_3
import com.grid.pos.data.MIGRATION_3_4
import com.grid.pos.data.MIGRATION_4_5
import com.grid.pos.data.MIGRATION_5_6
import com.grid.pos.data.MIGRATION_6_7
import com.grid.pos.data.payment.PaymentRepository
import com.grid.pos.data.payment.PaymentRepositoryImpl
import com.grid.pos.data.posPrinter.PosPrinterRepository
import com.grid.pos.data.posPrinter.PosPrinterRepositoryImpl
import com.grid.pos.data.posReceipt.PosReceiptRepository
import com.grid.pos.data.posReceipt.PosReceiptRepositoryImpl
import com.grid.pos.data.purchase.PurchaseRepository
import com.grid.pos.data.purchase.PurchaseRepositoryImpl
import com.grid.pos.data.purchaseHeader.PurchaseHeaderRepository
import com.grid.pos.data.purchaseHeader.PurchaseHeaderRepositoryImpl
import com.grid.pos.data.receipt.ReceiptRepository
import com.grid.pos.data.receipt.ReceiptRepositoryImpl
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.data.settings.SettingsRepositoryImpl
import com.grid.pos.data.stockAdjustment.StockAdjustmentRepository
import com.grid.pos.data.stockAdjustment.StockAdjustmentRepositoryImpl
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustmentRepository
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustmentRepositoryImpl
import com.grid.pos.data.stockInOut.StockInOutRepository
import com.grid.pos.data.stockInOut.StockInOutRepositoryImpl
import com.grid.pos.data.stockHeaderInOut.StockHeaderInOutRepository
import com.grid.pos.data.stockHeaderInOut.StockHeaderInOutRepositoryImpl
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.data.thirdParty.ThirdPartyRepositoryImpl
import com.grid.pos.data.user.UserRepository
import com.grid.pos.data.user.UserRepositoryImpl
import com.grid.pos.useCases.CheckLicenseUseCase
import com.grid.pos.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        val callback = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.disableWriteAheadLogging()
            }
        }
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .addMigrations(MIGRATION_6_7)
            .addCallback(callback).build()
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(): SettingsRepository {
        return SettingsRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideFamilyRepository(db: AppDatabase): FamilyRepository {
        return FamilyRepositoryImpl(db.categoryDao)
    }

    @Provides
    @Singleton
    fun provideItemRepository(db: AppDatabase): ItemRepository {
        return ItemRepositoryImpl(db.itemDao)
    }

    @Provides
    @Singleton
    fun provideUserRepository(db: AppDatabase): UserRepository {
        return UserRepositoryImpl(db.userDao)
    }

    @Provides
    @Singleton
    fun provideCompanyRepository(db: AppDatabase): CompanyRepository {
        return CompanyRepositoryImpl(db.companyDao)
    }

    @Provides
    @Singleton
    fun provideCurrencyRepository(db: AppDatabase): CurrencyRepository {
        return CurrencyRepositoryImpl(db.currencyDao)
    }

    @Provides
    @Singleton
    fun provideThirdPartyRepository(db: AppDatabase): ThirdPartyRepository {
        return ThirdPartyRepositoryImpl(db.thirdPartyDao)
    }

    @Provides
    @Singleton
    fun providePosPrinterRepository(db: AppDatabase): PosPrinterRepository {
        return PosPrinterRepositoryImpl(db.posPrinterDao)
    }

    @Provides
    @Singleton
    fun provideInvoiceRepository(db: AppDatabase): InvoiceRepository {
        return InvoiceRepositoryImpl(db.invoiceDao)
    }

    @Provides
    @Singleton
    fun provideInvoiceHeaderRepository(db: AppDatabase): InvoiceHeaderRepository {
        return InvoiceHeaderRepositoryImpl(db.invoiceHeaderDao)
    }

    @Provides
    @Singleton
    fun providePosReceiptRepository(db: AppDatabase): PosReceiptRepository {
        return PosReceiptRepositoryImpl(db.posReceiptDao)
    }

    @Provides
    @Singleton
    fun providePaymentRepository(db: AppDatabase): PaymentRepository {
        return PaymentRepositoryImpl(db.paymentDao)
    }

    @Provides
    @Singleton
    fun provideReceiptRepository(db: AppDatabase): ReceiptRepository {
        return ReceiptRepositoryImpl(db.receiptDao)
    }

    @Provides
    @Singleton
    fun provideStockHeaderInOutRepository(): StockHeaderInOutRepository {
        return StockHeaderInOutRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideStockInOutRepository(): StockInOutRepository {
        return StockInOutRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideStockHeaderAdjustmentRepository(): StockHeaderAdjustmentRepository {
        return StockHeaderAdjustmentRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideStockAdjustmentRepository(): StockAdjustmentRepository {
        return StockAdjustmentRepositoryImpl()
    }

    @Provides
    @Singleton
    fun providePurchaseHeaderRepository(): PurchaseHeaderRepository {
        return PurchaseHeaderRepositoryImpl()
    }

    @Provides
    @Singleton
    fun providePurchaseRepository(): PurchaseRepository {
        return PurchaseRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideLicenseUseCase(
        companyRepository: CompanyRepository,
        invoiceHeaderRepository: InvoiceHeaderRepository
    ): CheckLicenseUseCase {
        return CheckLicenseUseCase(
            companyRepository,
            invoiceHeaderRepository
        )
    }

    @Provides
    @Singleton
    fun provideSharedViewModel(
        settingsRepository: SettingsRepository,
        currencyRepository: CurrencyRepository
    ): SharedViewModel {
        return SharedViewModel(settingsRepository, currencyRepository)
    }


}