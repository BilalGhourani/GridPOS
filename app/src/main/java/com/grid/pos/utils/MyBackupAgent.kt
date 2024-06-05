package com.grid.pos.utils

import android.app.backup.BackupAgentHelper
import android.app.backup.BackupDataOutput
import android.app.backup.FileBackupHelper
import android.app.backup.SharedPreferencesBackupHelper
import android.os.ParcelFileDescriptor

class MyBackupAgent : BackupAgentHelper() {

    override fun onCreate() {
        super.onCreate()

        val prefsBackupHelper = SharedPreferencesBackupHelper(this, "MyAppPreferences")
        addHelper("prefs", prefsBackupHelper)

        val filesBackupHelper = FileBackupHelper(this, "../databases/user_database")
        addHelper("files", filesBackupHelper)
    }

    override fun onBackup(
            oldState: ParcelFileDescriptor?,
            data: BackupDataOutput?,
            newState: ParcelFileDescriptor?
    ) {
        if (data != null) {
            if ((data.transportFlags and
                        FLAG_CLIENT_SIDE_ENCRYPTION_ENABLED) != 0) {
                // Client-side backup encryption is enabled.
            }

            if ((data.transportFlags and FLAG_DEVICE_TO_DEVICE_TRANSFER) != 0) {
                // Local device-to-device transfer is enabled.
            }
        }
    }
}