package com.grid.pos.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class ConnectivityReceiver : BroadcastReceiver() {

    var listener: ConnectivityChangeListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            listener?.onNetworkConnected()
        } else {
            listener?.onNetworkDisconnected()
        }
    }

    interface ConnectivityChangeListener {
        fun onNetworkConnected()
        fun onNetworkDisconnected()
    }
}