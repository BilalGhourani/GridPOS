package com.grid.pos.utils

import android.app.Activity
import androidx.core.app.ActivityCompat

object PermissionManager {

    private val CONTACTS_CODE = 1

    fun requestPermission(activity: Activity, permission: String) {
        val requestPermissionArray = arrayOf(permission)
        ActivityCompat.requestPermissions(
            activity,
            requestPermissionArray,
            CONTACTS_CODE
        )
    }

}