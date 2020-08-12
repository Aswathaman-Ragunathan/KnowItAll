package com.ash.bluetooth

import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast

object BluetoothManager {
    private var context: Context? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothProfileListener: BluetoothProfileListener? = null
    private var discoveryReceiver: BroadcastReceiver? = null
    private var discoverabilityReceiver: BroadcastReceiver? = null
    private var bluetoothEnableReceiver: BroadcastReceiver? = null

    interface BluetoothProfileListener {
        fun onServiceConnected(proxy: BluetoothProfile?) {
        }

        fun onServiceDisconnected(proxy: BluetoothProfile?) {
        }
    }

    fun initialize(context: Context) {
        this.context = context
        bluetoothProfileListener = context as BluetoothProfileListener
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
        }
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(context, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
        }
    }

    fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(ACTION_REQUEST_ENABLE)
            context?.startActivity(enableBtIntent)
        }
        registerBluetoothEnableBroadcast()
    }

    private fun registerBluetoothEnableBroadcast() {
        val filter = IntentFilter(ACTION_STATE_CHANGED)
        if (bluetoothEnableReceiver == null)
            bluetoothEnableReceiver = getBluetoothEnableReceiver()
        context?.registerReceiver(bluetoothEnableReceiver, filter)
    }

    private fun getBluetoothEnableReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action: String? = intent.action
                action?.let {
                    when (action) {
                        ACTION_STATE_CHANGED -> {
                            val enableMode: Int? =
                                intent.getIntExtra(EXTRA_STATE, 0)
                            when (enableMode) {
                                STATE_ON -> Toast.makeText(
                                    context,
                                    "Bluetooth is Turned On",
                                    Toast.LENGTH_SHORT
                                ).show()
                                STATE_OFF -> Toast.makeText(
                                    context,
                                    "Bluetooth is Turned Off",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    fun enableBluetoothDiscoverability() {
        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
        context?.startActivity(discoverableIntent)
        registerDiscoverabilityBroadcast()
    }


    private fun registerDiscoverabilityBroadcast() {
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
                            val scanMode: Int? =
                                intent.getIntExtra(EXTRA_SCAN_MODE, 0)
                            when (scanMode) {
                                SCAN_MODE_CONNECTABLE_DISCOVERABLE -> Toast.makeText(
                                    context,
                                    "The device is in discoverable mode",
                                    Toast.LENGTH_SHORT
                                ).show()
                                SCAN_MODE_CONNECTABLE -> Toast.makeText(
                                    context,
                                    "The device isn't in discoverable mode but can still receive connections",
                                    Toast.LENGTH_SHORT
                                ).show()
                                SCAN_MODE_NONE -> Toast.makeText(
                                    context,
                                    "The device isn't in discoverable mode and cannot receive connections",
                                    Toast.LENGTH_SHORT
                                ).show()
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
        if (discoveryReceiver == null)
            discoveryReceiver = getDiscoveryReceiver()
        context?.registerReceiver(discoveryReceiver, filter)
    }

    private fun getDiscoveryReceiver(): BroadcastReceiver {
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
                            val blc: BluetoothClass
                            val deviceName = device?.name
                            val deviceHardwareAddress = device?.address // MAC address
                        }
                    }
                }
            }
        }
    }

    fun finishAndClean() {
        bluetoothEnableReceiver = null
        discoverabilityReceiver = null
        discoveryReceiver = null
        bluetoothAdapter?.let {
            bluetoothAdapter = null
        }

        bluetoothProfileListener?.let {
            bluetoothProfileListener = null
        }

        context = null
    }
}