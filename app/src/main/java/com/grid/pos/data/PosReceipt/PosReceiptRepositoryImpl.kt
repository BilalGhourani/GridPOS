package com.grid.pos.data.PosReceipt

import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.flow.Flow

class PosReceiptRepositoryImpl(
    private val posReceiptDao: PosReceiptDao
) : PosReceiptRepository {
    override suspend fun insert(posReceipt: PosReceipt) {
        if (SettingsModel.loadFromRemote) {

        } else {
            posReceiptDao.insert(posReceipt)
        }
    }

    override suspend fun delete(posReceipt: PosReceipt) {
        if (SettingsModel.loadFromRemote) {

        } else {
            posReceiptDao.delete(posReceipt)
        }
    }

    override suspend fun update(posReceipt: PosReceipt) {
        if (SettingsModel.loadFromRemote) {

        } else {
            posReceiptDao.update(posReceipt)
        }
    }

    override suspend fun getPosReceiptById(id: String): PosReceipt {
        return posReceiptDao.getPosReceiptById(id)
    }

    override fun getAllPosReceipts(): Flow<List<PosReceipt>> {
        if (SettingsModel.loadFromRemote) {
            return posReceiptDao.getAllPosReceipts()
        } else {
            return posReceiptDao.getAllPosReceipts()
        }
    }


}