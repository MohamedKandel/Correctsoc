package com.correct.correctsoc.ui.deviceScan

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.DevicesData
import com.correct.correctsoc.databinding.FragmentDeviceScanningBinding
import com.correct.correctsoc.helper.Constants.LIST
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.OnDataFetchedListener
import com.correct.correctsoc.helper.OnProgressUpdatedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import tej.wifitoolslib.DevicesFinder
import tej.wifitoolslib.interfaces.OnDeviceFindListener
import tej.wifitoolslib.models.DeviceItem

class DeviceScanningFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentDeviceScanningBinding
    private lateinit var helper: HelperClass
    private var progressJob: Job? = null
    private var fetchDevicesJob: Job? = null
    private lateinit var fragmentListener: FragmentChangedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangedListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDeviceScanningBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.deviceScanningFragment)

        helper.onBackPressed(this) {
            stopOperations()
            findNavController().navigate(R.id.homeFragment)
        }

        binding.progressCircular.startAnimation(helper.circularAnimation(3000))

        //Log.i("Vendor mohamed", "onCreateView: ${getVendorName("e6:61:c0:14:78:88")}")

        CoroutineScope(Dispatchers.Main).launch {
            var list = mutableListOf<DevicesData>()
            val progressJob = launch(Dispatchers.Main) {
                progress(object : OnProgressUpdatedListener {
                    @SuppressLint("SetTextI18n")
                    override fun onUpdateProgressLoad(progress: Int) {
                        // Optional: Update UI based on progress
                        binding.txtPercent.text = "$progress%"
                        if (progress == 100) {
                            Log.e("List Devices mohamed", "onCreateView: ${list.size}")
                            Log.i("Devices mohamed", "onUpdateProgressLoad: finished")
                            val bundle = Bundle()
                            bundle.putParcelableArrayList(LIST, ArrayList(list))
                            findNavController().navigate(R.id.devicesFragment, bundle)
                        }
                    }
                }) {
                    fetchDevicesJob?.isActive == false
                }
            }

            // Launch coroutine to fetch devices
            fetchDevicesJob = launch(Dispatchers.IO) {
                list = fetchDevices()
                Log.e("List Devices mohamed", "onCreateView: ${list.size}")
                // Wait for progress job to complete
                progressJob.join()
            }
        }

        binding.btnStop.setOnClickListener {
            stopOperations()
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnBack.setOnClickListener {
            stopOperations()
            findNavController().navigate(R.id.homeFragment)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }




    private fun stopOperations() {
        // Cancel progress job
        progressJob?.cancel()
        // Cancel fetch devices job
        fetchDevicesJob?.cancel()
        findNavController().navigate(R.id.homeFragment)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun fetchDevices(): MutableList<DevicesData> {
        return suspendCancellableCoroutine { continuation ->
            getDevices(object : OnDataFetchedListener<DevicesData> {
                override fun onAllDataFetched(data: MutableList<DevicesData>) {
                    continuation.resume(data, null)
                }
            })
        }
    }

    private fun getDevices(callback: OnDataFetchedListener<DevicesData>) {
        val list = mutableListOf<DevicesData>()
        val deviceFinder = DevicesFinder(requireContext(), object : OnDeviceFindListener {
            override fun onStart() {
            }

            override fun onDeviceFound(deviceItem: DeviceItem?) {
            }

            override fun onComplete(deviceItems: MutableList<DeviceItem>?) {
                if (deviceItems != null) {
                    for (device in deviceItems) {
                        list.add(
                            DevicesData(
                                device.ipAddress,
                                device.deviceName, device.macAddress, device.vendorName
                            )
                        )
                    }
                }
                callback.onAllDataFetched(list)
            }

            override fun onFailed(errorCode: Int) {
                Log.d("error mohamed", "onFailed: can't load")
                callback.onAllDataFetched(list)
            }
        })
        deviceFinder.start()
    }


    suspend fun progress(progress: OnProgressUpdatedListener, isCanceled: () -> Boolean) {
        withContext(Dispatchers.Main) {
            for (i in 0..100) {
                if (isCanceled()) {
                    break
                }
                progress.onUpdateProgressLoad(i)
                delay(150)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.deviceScanningFragment)
    }
}