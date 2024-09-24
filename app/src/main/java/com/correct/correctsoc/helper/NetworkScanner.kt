package com.correct.correctsoc.helper

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.correct.correctsoc.data.DevicesData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Collections

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
                val macAddress = if (ipAddress == getOwnIpAddress()) {      // Get the MAC address
                    getOwnMacAddress()
                } else {
                    getMacAddress(ipAddress)
                }
                Log.i("Mac address mohamed","${getOwnIpAddress()}\t${ipAddress}")

                return@withContext DevicesData(ipAddress, hostname, macAddress,"")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    // Get the IP address of the current device
    private fun getOwnIpAddress(): String? {
        try {
            val allInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in allInterfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') == -1) {
                        // Filter out loopback and IPv6 addresses, return the first valid IPv4 address
                        return address.hostAddress
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return ""
    }

    /*private fun getMacAddress(ipAddress: String): String {
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
    }*/

    private fun getOwnMacAddress(): String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in all) {
                if (networkInterface.name.equals("wlan0", ignoreCase = true)) {
                    val macBytes = networkInterface.hardwareAddress ?: return ""
                    return macBytes.joinToString(separator = ":") { String.format("%02X", it) }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return ""
    }
    private fun getMacAddress(ipAddress: String): String {
        try {
            val bufferedReader = BufferedReader(FileReader("/proc/net/arp"))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                val columns = line?.split("\\s+".toRegex()) ?: continue
                if (columns.size >= 4 && columns[0] == ipAddress) {
                    return columns[3] // The 4th column (index 3) contains the MAC address
                }
            }
            bufferedReader.close()
        } catch (e: IOException) {
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