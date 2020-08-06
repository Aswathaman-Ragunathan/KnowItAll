package com.ash.bluetooth

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult

object BluetoothManager {
    private var context: Context? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothProfileListener: BluetoothProfileListener? = null
    private var receiver: BroadcastReceiver? = null
    private var discoverabilityReceiver: BroadcastReceiver? = null

    interface BluetoothProfileListener {
        fun onServiceConnected(proxy: BluetoothProfile?) {
        }

        fun onServiceDisconnected(proxy: BluetoothProfile?) {
        }
    }

    fun initializeAdapter(activity: Activity) {
        this.context = activity.applicationContext
        bluetoothProfileListener = context as BluetoothProfileListener
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
        }
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(context, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
        }
    }

    fun enableBluetooth(activity: Activity, requestCode: Int) {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(activity, enableBtIntent, requestCode, null)
        }
    }

    fun enableBluetoothDiscoverability(){
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        context?.startActivity(discoverableIntent)
        registerDiscoverabilityBroadcast()
    }

    private fun registerDiscoverabilityBroadcast(){
        val filter = IntentFilter(ACTION_SCAN_MODE_CHANGED)
        if (discoverabilityReceiver == null)
            discoverabilityReceiver = getDiscoverabilityReceiver()
        context?.registerReceiver(discoverabilityReceiver, filter)
    }

    private fun getDiscoverabilityReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action: String? = intent.action
                action?.let {
                    when (action) {
                        ACTION_SCAN_MODE_CHANGED -> {
                            val scanMode : Int? =
                                intent.getIntExtra(EXTRA_SCAN_MODE,0)
                            when(scanMode){
                                SCAN_MODE_CONNECTABLE_DISCOVERABLE -> Toast.makeText(context,"The device is in discoverable mode.",Toast.LENGTH_SHORT).show()
                                SCAN_MODE_CONNECTABLE -> Toast.makeText(context,"The device isn't in discoverable mode but can still receive connections.",Toast.LENGTH_SHORT).show()
                                SCAN_MODE_NONE -> Toast.makeText(context,"The device isn't in discoverable mode and cannot receive connections.",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    fun getPairedDevices(): Set<BluetoothDevice>? {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
        }
        return pairedDevices
    }

    fun getProfileProxy(bluetoothProfileId: Int) {
        bluetoothAdapter?.getProfileProxy(
            context,
            getListener(bluetoothProfileId),
            bluetoothProfileId
        )
    }

    private fun getListener(currentProfileInSearch: Int): BluetoothProfile.ServiceListener {
        return object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == currentProfileInSearch) {
                    bluetoothProfileListener?.onServiceConnected(proxy)
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == currentProfileInSearch) {
                    bluetoothProfileListener?.onServiceDisconnected(null)
                }
            }
        }
    }

    fun closeConnection(bluetoothProfileId: Int?, proxy: BluetoothProfile?) {
        proxy?.let { bluetoothProfile ->
            bluetoothProfileId?.let {
                bluetoothAdapter?.closeProfileProxy(it, bluetoothProfile)
            }
        }
    }

    fun startBluetoothDiscovery() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        if (receiver == null)
            receiver = getDiscoveryReceiver()
        context?.registerReceiver(receiver, filter)
    }

    fun getDiscoveryReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action: String? = intent.action
                action?.let {
                    when (action) {
                        BluetoothDevice.ACTION_FOUND -> {
                            // Discovery has found a device. Get the BluetoothDevice
                            // object and its info from the Intent.
                            val device: BluetoothDevice? =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            val blc :BluetoothClass
                            val deviceName = device?.name
                            val deviceHardwareAddress = device?.address // MAC address
                        }
                    }
                }
            }
        }
    }

    fun finishFunctions() {
        receiver = null
        discoverabilityReceiver = null
        bluetoothAdapter?.let {
            bluetoothAdapter = null
        }

        bluetoothProfileListener?.let {
            bluetoothProfileListener = null
        }

        context = null
    }
}