package de.hsfl.team34.capturetheflag

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.lokibt.bluetooth.BluetoothAdapter
import com.lokibt.bluetooth.BluetoothDevice
import com.lokibt.bluetooth.BluetoothServerSocket
import com.lokibt.bluetooth.BluetoothSocket
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.util.*

class BluetoothRepository {
    companion object {
        var instance1: BluetoothRepository? = null

        fun getInstance(): BluetoothRepository {
            instance1 = instance1 ?: BluetoothRepository()
            return instance1!!
        }
    }

    val uuid = UUID.fromString("ca97025c-e741-11ec-8fea-0242ac120002")
    val name = "CaptureTheFlag"
    var listen: BluetoothServerSocket? = null
    val adapter = BluetoothAdapter.getDefaultAdapter()

    var discoveryCallback: (BluetoothDevice) -> Unit = {}
    val discoveryFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    var serverCallback: (Boolean) -> Unit = {}

    private var isServerActive = false
        set(value) {
            field = value
            serverCallback(field)
        }

    val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    discoveryCallback(device)
                }
            }
        }
    }

    fun searchDevices(app: Application) {
        app.registerReceiver(discoveryReceiver, discoveryFilter)
        adapter.startDiscovery()
    }

    private fun stopDeviceSearch(app: Application) {
        adapter.cancelDiscovery()
        app.unregisterReceiver(discoveryReceiver)
    }

    fun sendMessage(
        other: BluetoothDevice,
        gameID: String,
        name: String,
        token: String,
        app : Application,
        responseCallback: (String) -> Unit,
        errorCallback: (Error) -> Unit
    ) = Thread {
        var socket: BluetoothSocket? = null
        try {
            socket = other.createRfcommSocketToServiceRecord(uuid)
            socket.connect()
            val writer = BufferedWriter(OutputStreamWriter(socket?.outputStream))
            val reader = BufferedReader(InputStreamReader(socket?.inputStream))

            val json = JSONObject(
                mapOf(
                    "addr" to other.address,
                    "game" to gameID,
                    "name" to name,
                    "token" to token
                )
            )

            writer.write("${json}\n")
            writer.flush()

            val line = reader.readLine()
            responseCallback(line)
            stopDeviceSearch(app)
            socket?.close()
        } catch (e: Exception) {
            errorCallback(Error(e.message))
            socket?.close()
        }
    }.start()


    fun startServer(gameID : String, receiveCallback: (JSONObject /*String,*/ ) -> Unit, errorCallback: (Error) -> Unit) = Thread {
        var socket: BluetoothSocket? = null
        try {
            isServerActive = true
            listen = adapter.listenUsingRfcommWithServiceRecord(name, uuid)
            while (true) {
                socket = listen?.accept()
                val writer = BufferedWriter(OutputStreamWriter(socket?.outputStream))
                val reader = BufferedReader(InputStreamReader(socket?.inputStream))
                val json = JSONObject(reader.readLine())

                var response : JSONObject

                val addressHost = socket?.remoteDevice?.address
                val player = JSONObject(
                    mapOf(
                        "game" to json.getString("game"),
                        "name" to json.getString("name"),
                        "addr" to addressHost,
                        "token" to json.getString("token"),
                        "serverAddr" to json.getString("addr")
                    ))

                if (json.getString("game") == gameID) {
                    receiveCallback(player)
                    response = JSONObject(mapOf("error" to null))
                } else {
                    response = JSONObject(mapOf("error" to "Authentication Error!"))
                }

                writer.write("${response}\n")
                writer.flush()

                socket?.close()
            }
        } catch (e: Exception) {
            errorCallback(Error(e.message))
            isServerActive = false
            listen?.close()
            socket?.close()
        }
    }.start()

    fun stopServer() {
        listen?.close()
    }
}