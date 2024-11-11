package com.grid.pos.utils

import android.provider.MediaStore

object Constants {
    const val DATABASE_NAME = "collections_db"
    const val LICENSE_FILE_CONTENT = "2r6xqq4nMdry0uvLQRpRxQ=="
    const val LICENSE_WARNING_TITLE ="CheckLicense"
    const val LICENSE_WARNING_DESCRIPTION = "Local server is not responding, contact your system administrator"

    /*
    * license result value
    * Integer
     */
    const val SUCCEEDED = 1
    const val LICENSE_NOT_FOUND = -2
    const val LICENSE_EXPIRED = -3
    const val LICENSE_ACCESS_DENIED = -4

    const val SQL_USER_POS_MODE = true

    const val SHOW_ALL_SCREENS_FOR_SQL_SERVER = false
}