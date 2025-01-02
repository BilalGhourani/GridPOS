package com.grid.pos.data.posPrinter

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PosPrinterDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posPrinter: PosPrinter)

    // insert list of  POS Printers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<PosPrinter>)

    // Delete a POS Printer
    @Delete
    suspend fun delete(posPrinter: PosPrinter)

    // Delete all POS Printers
    @Query("DELETE FROM pos_printer")
    suspend fun deleteAll()

    // Update a POS Printer
    @Update
    suspend fun update(posPrinter: PosPrinter)

    // Get all POS Printers as stream.
    @Query("SELECT * FROM `pos_printer` WHERE pp_cmp_id = :companyId")
    fun getAllPosPrinters(companyId: String): MutableList<PosPrinter>

    // Get One POS Printer as stream.
    @Query("SELECT * FROM `pos_printer` WHERE pp_cmp_id = :companyId LIMIT 1")
    fun getOnePosPrinter(companyId: String): PosPrinter?

}