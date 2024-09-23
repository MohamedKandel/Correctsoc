package com.correct.correctsoc.ui.deviceScan

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.DevicesData
import com.correct.correctsoc.databinding.FragmentDeviceScanningBinding
import com.correct.correctsoc.helper.Constants.LIST
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.NetworkScanner
import com.correct.correctsoc.helper.OnDataFetchedListener
import com.correct.correctsoc.helper.OnProgressUpdatedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class DeviceScanningFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentDeviceScanningBinding
    private lateinit var helper: HelperClass
    private var progressJob: Job? = null
    private var fetchDevicesJob: Job? = null
    private lateinit var mlist: MutableList<DevicesData>
    private var isCanceled = false
    private lateinit var fragmentListener: FragmentChangedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangedListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDeviceScanningBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        binding.root.keepScreenOn = true

        fragmentListener.onFragmentChangedListener(R.id.deviceScanningFragment)

        helper.onBackPressed(this) {
            stopOperations()
            findNavController().navigate(R.id.homeFragment)
        }

//        binding.progressCircular.startAnimation(helper.circularAnimation(3000))

        //Log.i("Vendor mohamed", "onCreateView: ${getVendorName("e6:61:c0:14:78:88")}")

        CoroutineScope(Dispatchers.Main).launch {
            val progressJob = launch {
                progress(object : OnProgressUpdatedListener {
                    override fun onUpdateProgressLoad(progress: Int) {
                        binding.txtPercent.text = "$progress%"
                    }
                }) {
                    fetchDevicesJob?.isCancelled == false
                }
                // Ensure progress reaches 100% when data fetching is done
                /*if (helper.getLang(requireContext()).equals("ar")) {
                    binding.txtPercent.text = "100%".mappingNumbers()
                } else {
                    binding.txtPercent.text = "100%"
                }*/
            }

            // Launch coroutine to fetch devices
            val fetchDevicesJob = launch(Dispatchers.IO) {
                this@DeviceScanningFragment.fetchDevicesJob?.start()
                mlist = fetchDevices() // Fetch data
                Log.e("List Devices mohamed fetch job", "onCreateView: ${mlist.size}")
                // Wait for progress to complete to 100%
                withContext(Dispatchers.Main) {
                    // This ensures that progressJob completes
                    progressJob.join()
                    // Ensure progress is 100%
                    binding.txtPercent.text = "100%"

                    // Navigate after devices are fetched and progress is completed
                    if (this@DeviceScanningFragment::mlist.isInitialized) {
                        if (!isCanceled) {
                            Log.e("List Devices mohamed fetch job", "onCreateView: ${mlist.size}")
                            val bundle = Bundle()
                            bundle.putParcelableArrayList(LIST, ArrayList(mlist))
                            findNavController().navigate(R.id.devicesFragment, bundle)
                        }
                    }
                }
            }

            // Wait for both jobs to complete
            fetchDevicesJob.join()
            progressJob.cancel() // Cancel progress updates after navigation
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
        isCanceled = true
        progressJob?.cancel("Job Cancelled")
        // Cancel fetch devices job
        fetchDevicesJob?.cancel("Job Cancelled")
        if (fetchDevicesJob?.isCancelled == true && progressJob?.isCancelled == true) {
            Log.i("List Device mohamed", "Jobs cancelled")
        }
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
        mlist = mutableListOf()
        val networkScanner = NetworkScanner(requireContext())
        networkScanner.scanNetwork {
            mlist.add(it)
        }
        callback.onAllDataFetched(mlist)
        /*NetworkScanner.init(requireContext())
        NetworkScanner.scan(object : OnNetworkScanListener {
            override fun onComplete(devices: MutableList<Device>?) {
                if (devices != null) {
                    for (device in devices) {
                        if (device != null) {
                            mlist.add(
                                DevicesData(
                                    device.ipAddress,
                                    device.hostname, device.macAddress, device.vendorName
                                )
                            )
                            Log.i("Device mohamed", device.macAddress)
                        }
                    }
                }
                callback.onAllDataFetched(mlist)
            }

            override fun onFailed() {
                Log.d("error mohamed", "onFailed: can't load")
                callback.onAllDataFetched(mlist)
            }
        })
        NetworkScanner.setShowMacAddress(true)*/
    }


    suspend fun progress(progress: OnProgressUpdatedListener, isCanceled: () -> Boolean) {
        withContext(Dispatchers.Main) {
            for (i in 0..100) {
                if (isCanceled()) {
                    break
                }
                progress.onUpdateProgressLoad(i)
                delay(250)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.deviceScanningFragment)
        binding.progressCircular.startAnimation(helper.circularAnimation(3000))
    }

    override fun onPause() {
        super.onPause()
        binding.progressCircular.clearAnimation()
    }
}