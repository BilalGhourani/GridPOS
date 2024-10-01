package com.grid.pos.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinter {

    private var bluetoothSocket: BluetoothSocket? = null
    var outputStream: OutputStream? = null

    fun connectToPrinter(
            context: Context,
            printerName: String
    ): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter ?: BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val device: BluetoothDevice? = bluetoothAdapter.bondedDevices.firstOrNull { it.name == printerName }

        if (device == null) {
            Log.e(
                "BluetoothPrinter",
                "Printer not found"
            )
            return false
        }

        return try {
            val uuid: UUID = device.uuids[0].uuid
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: IOException) {
            Log.e(
                "BluetoothPrinter",
                "Failed to connect to printer",
                e
            )
            false
        }
    }

    fun printData(byteArray: ByteArray) {
        try {
            outputStream?.write(byteArray)
            outputStream?.flush()
        } catch (e: IOException) {
            Log.e(
                "BluetoothPrinter",
                "Failed to print data",
                e
            )
        }
    }

    fun disconnectPrinter() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(
                "BluetoothPrinter",
                "Failed to close connection",
                e
            )
        }
    }
}
