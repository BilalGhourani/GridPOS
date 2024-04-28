package com.grid.pos.data.PosReceipt

import kotlinx.coroutines.flow.Flow

class PosReceiptRepositoryImpl(
    private val posReceiptDao: PosReceiptDao
) : PosReceiptRepository {
    override suspend fun insert(posReceipt: PosReceipt) {
        posReceiptDao.insert(posReceipt)
    }

    override suspend fun delete(posReceipt: PosReceipt) {
        posReceiptDao.delete(posReceipt)
    }

    override suspend fun update(posReceipt: PosReceipt) {
        posReceiptDao.update(posReceipt)
    }

    override suspend fun getPosReceiptById(id: String): PosReceipt {
        return posReceiptDao.getPosReceiptById(id)
    }

    override fun getAllPosReceipts(): Flow<List<PosReceipt>> {
        return posReceiptDao.getAllPosReceipts()
    }


}