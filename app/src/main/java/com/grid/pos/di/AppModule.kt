package com.grid.pos.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.grid.pos.data.AppDatabase
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Company.CompanyRepositoryImpl
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.Currency.CurrencyRepositoryImpl
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.Family.FamilyRepositoryImpl
import com.grid.pos.data.Invoice.InvoiceRepository
import com.grid.pos.data.Invoice.InvoiceRepositoryImpl
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepositoryImpl
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.Item.ItemRepositoryImpl
import com.grid.pos.data.MIGRATION_1_2
import com.grid.pos.data.MIGRATION_2_3
import com.grid.pos.data.MIGRATION_3_4
import com.grid.pos.data.MIGRATION_4_5
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.data.PosPrinter.PosPrinterRepositoryImpl
import com.grid.pos.data.PosReceipt.PosReceiptRepository
import com.grid.pos.data.PosReceipt.PosReceiptRepositoryImpl
import com.grid.pos.data.Settings.SettingsRepository
import com.grid.pos.data.Settings.SettingsRepositoryImpl
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.data.ThirdParty.ThirdPartyRepositoryImpl
import com.grid.pos.data.User.UserRepository
import com.grid.pos.data.User.UserRepositoryImpl
import com.grid.pos.ui.license.CheckLicenseUseCase
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
    fun provideLicenseUseCase(
            companyRepository: CompanyRepository,
            invoiceHeaderRepository: InvoiceHeaderRepository
    ): CheckLicenseUseCase {
        return CheckLicenseUseCase(
            companyRepository,
            invoiceHeaderRepository
        )
    }


}