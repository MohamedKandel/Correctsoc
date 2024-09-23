package com.correct.correctsoc.helper

import android.content.Context
import android.net.wifi.WifiManager
import com.correct.correctsoc.data.DevicesData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface

class NetworkScanner(private val context: Context) {
    private fun getLocalSubnetIPs(): List<String> {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ip = wifiInfo.ipAddress

        val ipString = (ip and 0xff).toString() + "." + (ip shr 8 and 0xff) + "." + (ip shr 16 and 0xff) + "."
        val subnet = ipString.dropLast(1)
        val ipList = mutableListOf<String>()

        for (i in 1..254) {
            ipList.add("$subnet.$i")
        }
        return ipList
    }

    // Function to check if an IP is reachable
    private suspend fun getDeviceInfo(ipAddress: String): DevicesData? = withContext(Dispatchers.IO) {
        try {
            val inetAddress = InetAddress.getByName(ipAddress)
            if (inetAddress.isReachable(500)) {
                val hostname = inetAddress.hostName  // Get the device hostname
                val macAddress = getMacAddress(ipAddress)  // Get the MAC address
                return@withContext DevicesData(ipAddress, hostname, macAddress,"")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun getMacAddress(ipAddress: String): String {
        try {
            val networkInterfaceList = NetworkInterface.getNetworkInterfaces()
            networkInterfaceList.iterator().forEach { networkInterface ->
                val mac = networkInterface.hardwareAddress ?: return@forEach
                val macString = mac.joinToString(":") { "%02X".format(it) }
                networkInterface.inetAddresses.iterator().forEach { inetAddress ->
                    if (inetAddress.hostAddress == ipAddress) {
                        return macString
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    // Coroutine to scan the network for reachable devices
    fun scanNetwork(onDeviceFound: (DevicesData) -> Unit) {
        val ipList = getLocalSubnetIPs()
        CoroutineScope(Dispatchers.IO).launch {
            val reachableDevices = ipList.map { ip ->
                async { getDeviceInfo(ip) }
            }.awaitAll().filterNotNull()

            reachableDevices.forEach { deviceInfo ->
                withContext(Dispatchers.Main) {
                    onDeviceFound(deviceInfo)
                }
            }
        }
    }
}