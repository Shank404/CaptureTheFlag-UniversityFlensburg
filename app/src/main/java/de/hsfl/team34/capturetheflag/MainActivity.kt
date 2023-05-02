package de.hsfl.team34.capturetheflag

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.lokibt.bluetooth.BluetoothAdapter


class MainActivity : AppCompatActivity() {

    val viewModel: MainViewModel by viewModels()

    private val LOCATION_PERMISSION_CODE = 4
    private val PERMISSION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private val BLUETOOTH_ACTIVE_CODE = 5
    private val BLUETOOTH_DISCOVERABLE_CODE = 6
    private val BLUETOOTH_DISCOVERABLE_CODE_GAME = 7

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            for (location in p0.locations) {
                viewModel.setLocation(location.latitude, location.longitude)
            }
        }
    }
    private val locationRequest = LocationRequest.create().apply {
        interval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val locationSettingRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()

    lateinit var client: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        client = LocationServices.getFusedLocationProviderClient(this)

        // Make a toast of last received message
        viewModel.getLastMessage().observe(this) {
            it?.let {
                Toast.makeText(this, "Message: $it", Toast.LENGTH_LONG).show()
            }
        }

        // Make a toast of last error message
        viewModel.getLastError().observe(this) {
            it?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }

        // Make a toast of all found devices in list
        viewModel.getDevices().observe(this) {
            it?.let {
                Log.d("MainActivity", "Devices changed $it")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check result of bluetooth-activation intent
        if (requestCode == BLUETOOTH_ACTIVE_CODE) {

            // If Bluetooth has been enabled
            if (resultCode == RESULT_OK) {
                viewModel.searchForDevices()

            // If Bluetooth still is disabled
            } else {
                Toast.makeText(this, "Bluetooth has to be enabled!", Toast.LENGTH_LONG).show()
            }
        }

        // Check result of bluetooth-discoverable intent
        if (requestCode == BLUETOOTH_DISCOVERABLE_CODE) {

            // If device is discoverable
            if (resultCode == RESULT_OK) {
                viewModel.startService()

            // If device is still not discoverable
            } else {
                Toast.makeText(this, "Device needs to be discoverable!", Toast.LENGTH_LONG).show()
            }
        }

        // Check result of bluetooth-discoverable-intent in game mode
        if (requestCode == BLUETOOTH_DISCOVERABLE_CODE_GAME) {

            // If device is still not discoverable
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Device needs to be discoverable!", Toast.LENGTH_LONG).show()
            }
        }

    }

    fun activateBluetoothOrSearchForDevices() {

        // If Bluetooth isn't active, activate it.
        if (BluetoothAdapter.getDefaultAdapter().state != BluetoothAdapter.STATE_ON) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, BLUETOOTH_ACTIVE_CODE)

            // If Bluetooth is active, search for other devices.
        } else {
            viewModel.searchForDevices()
        }
    }

    fun startServer() {

        // If device isn't discoverable, activate it.
        if (BluetoothAdapter.getDefaultAdapter().scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            startActivityForResult(intent, BLUETOOTH_DISCOVERABLE_CODE)

        // If device is discoverable, start service.
        } else {
            viewModel.startService()
        }
    }

    fun setDiscoverableGameMode(){
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 6000)
        startActivityForResult(intent, BLUETOOTH_DISCOVERABLE_CODE_GAME)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation()
            } else {
                Toast.makeText(this, "Location permissions required", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun startLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingRequest)
                .addOnSuccessListener {
                    client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                }
                .addOnFailureListener() {
                    Toast.makeText(this, "Location permissions required", Toast.LENGTH_LONG).show()
                    Thread.sleep(2000)
                    openLocationSettings()
                }

        } else {
            requestPermissions(PERMISSION, LOCATION_PERMISSION_CODE)
        }
    }

    fun stopLocation() {
        client.removeLocationUpdates(locationCallback)
    }

    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }


}