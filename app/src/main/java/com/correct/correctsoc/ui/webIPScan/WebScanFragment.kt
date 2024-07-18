package com.correct.correctsoc.ui.webIPScan

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.PortsAdapter
import com.correct.correctsoc.data.openPorts.CvEs
import com.correct.correctsoc.data.openPorts.OpenPorts
import com.correct.correctsoc.data.openPorts.Port
import com.correct.correctsoc.databinding.FragmentWebScanBinding
import com.correct.correctsoc.helper.AudioUtils
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.DEVICE_NAME
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.Constants.ITEM
import com.correct.correctsoc.helper.Constants.LIST
import com.correct.correctsoc.helper.Constants.PORTS_LIST
import com.correct.correctsoc.helper.Constants.ROUTER
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TYPE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.OnDataFetchedListener
import com.correct.correctsoc.helper.OnProgressUpdatedListener
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.ui.selfScan.ScanViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class WebScanFragment : Fragment(), ClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var value: OpenPorts

    private lateinit var binding: FragmentWebScanBinding
    private lateinit var helper: HelperClass
    private lateinit var viewModel: ScanViewModel
    private lateinit var list: MutableList<Port>
    private lateinit var adapter: PortsAdapter
    private var sourceFragment = 0
    private var type = ""
    private var deviceName = ""
    private var isSafe = false
    private lateinit var audioUtils: AudioUtils
    private lateinit var fragmentListener: FragmentChangedListener
    private var progressJob: Job? = null
    private var fetchDevicesJob: Job? = null

    /*
    * 1 -> web scan
    * 2 -> ip scan
    * 3 -> router scan*/
    private var scan_type = 1

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
        binding = FragmentWebScanBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[ScanViewModel::class.java]
        audioUtils = AudioUtils.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.webScanFragment)
        binding.loadingLayout.visibility = View.VISIBLE
        binding.txtDeviceName.visibility = View.GONE
        binding.txtNameTitle.visibility = View.GONE
        binding.imgSecurity.visibility = View.GONE

        list = mutableListOf()
        adapter = PortsAdapter(list, this)
        binding.recyclerView.adapter = adapter

        if (arguments != null) {
            binding.placeholder.keepScreenOn = true
            val input = requireArguments().getString(IP_ADDRESS, "")
            sourceFragment = requireArguments().getInt(SOURCE, 0)
            type = requireArguments().getString(TYPE, "")
            deviceName = requireArguments().getString(DEVICE_NAME).toString()
            when (sourceFragment) {
                R.id.insertLinkFragment -> {
                    if (type.isNotEmpty()) {
                        binding.txtTitle.text = resources.getString(R.string.ip_scan)
                        scan_type = 2
                    } else {
                        binding.txtTitle.text = resources.getString(R.string.web_scan)
                        scan_type = 1
                    }
                }

                R.id.penResultFragment -> {
                    binding.txtTitle.text = resources.getString(R.string.self_test)
                    scan_type = 3
                }
            }
            if (type.equals(IP_ADDRESS)) {
                binding.txtTitle.text = resources.getString(R.string.ip_scan)
                scan_type = 2
            } else if (type.equals(ROUTER)) {
                binding.txtTitle.text = resources.getString(R.string.self_test)
                scan_type = 3
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (requireArguments().getParcelableArrayList(
                        PORTS_LIST,
                        Port::class.java
                    ) != null
                ) {
                    list = requireArguments().getParcelableArrayList(PORTS_LIST, Port::class.java)!!
                    val isSecure = helper.isSecureSite(list)
                    if (isSecure) {
                        binding.imgSecurity.setImageResource(R.drawable.safe)
                        binding.txtNameTitle.setTextColor(
                            resources.getColor(
                                R.color.safe_color,
                                context?.theme
                            )
                        )
                        if (helper.getLang(requireContext()).equals("en")) {
                            playSound(scan_type, "en", true)
                        } else {
                            playSound(scan_type, "ar", true)
                        }
                        isSafe = true
                    } else {
                        binding.imgSecurity.setImageResource(R.drawable.danger)
                        binding.txtNameTitle.setTextColor(
                            resources.getColor(
                                R.color.danger_color,
                                context?.theme
                            )
                        )
                        if (helper.getLang(requireContext()).equals("en")) {
                            playSound(scan_type, "en", false)
                        } else {
                            playSound(scan_type, "ar", false)
                        }
                        isSafe = false
                    }
                    binding.txtDeviceName.visibility = View.VISIBLE
                    binding.txtNameTitle.visibility = View.VISIBLE
                    binding.imgSecurity.visibility = View.VISIBLE
                    binding.loadingLayout.visibility = View.GONE
                    adapter.updateAdapter(list)
                }
            } else {
                if (requireArguments().getParcelableArrayList<Port>(PORTS_LIST) != null) {
                    list = requireArguments().getParcelableArrayList(PORTS_LIST)!!

                    val isSecure = helper.isSecureSite(list)
                    if (isSecure) {
                        binding.imgSecurity.setImageResource(R.drawable.safe)
                        binding.txtNameTitle.setTextColor(
                            resources.getColor(
                                R.color.safe_color,
                                context?.theme
                            )
                        )
                        if (helper.getLang(requireContext()).equals("en")) {
                            playSound(scan_type, "en", true)
                        } else {
                            playSound(scan_type, "ar", true)
                        }
                    } else {
                        binding.imgSecurity.setImageResource(R.drawable.danger)
                        binding.txtNameTitle.setTextColor(
                            resources.getColor(
                                R.color.danger_color,
                                context?.theme
                            )
                        )
                        if (helper.getLang(requireContext()).equals("en")) {
                            playSound(scan_type, "en", false)
                        } else {
                            playSound(scan_type, "ar", false)
                        }
                    }
                    isSafe = isSecure

                    binding.txtDeviceName.visibility = View.VISIBLE
                    binding.txtNameTitle.visibility = View.VISIBLE
                    binding.imgSecurity.visibility = View.VISIBLE

                    binding.loadingLayout.visibility = View.GONE
                    adapter.updateAdapter(list)
                }
            }
            binding.txtDeviceName.text = requireArguments().getString(DEVICE_NAME)



            if (input.isNotEmpty()) {
                //scanning(input)
                CoroutineScope(Dispatchers.Main).launch {
                    //var mlist = mutableListOf<Port>()
                    val progressJob = launch(Dispatchers.Main) {
                        progress(object : OnProgressUpdatedListener {
                            @SuppressLint("SetTextI18n")
                            override fun onUpdateProgressLoad(progress: Int) {
                                val mprogress = if (helper.getLang(requireContext()).equals("ar")) {
                                    "$progress %".mappingNumbers()
                                } else {
                                    "$progress %"
                                }
                                binding.txtProgress.text = mprogress
                                if (progress == 100) {
                                    binding.loadingLayout.visibility = View.GONE

                                    if (this@WebScanFragment::value.isInitialized) {
                                        if (value.scanHostDeviceName.isEmpty()) {
                                            binding.txtDeviceName.text = value.scanIp
                                            deviceName = value.scanIp
                                        } else {
                                            binding.txtDeviceName.text = value.scanHostDeviceName
                                            deviceName = value.scanHostDeviceName
                                        }

                                        val isSecure = helper.isSecureSite(list)
                                        if (isSecure) {
                                            binding.imgSecurity.setImageResource(R.drawable.safe)
                                            binding.txtNameTitle.setTextColor(
                                                resources.getColor(
                                                    R.color.safe_color,
                                                    context?.theme
                                                )
                                            )
                                            if (helper.getLang(requireContext()).equals("en")) {
                                                playSound(scan_type, "en", true)
                                            } else {
                                                playSound(scan_type, "ar", true)
                                            }
                                        } else {
                                            binding.imgSecurity.setImageResource(R.drawable.danger)
                                            binding.txtNameTitle.setTextColor(
                                                resources.getColor(
                                                    R.color.danger_color,
                                                    context?.theme
                                                )
                                            )
                                            if (helper.getLang(requireContext()).equals("en")) {
                                                playSound(scan_type, "en", false)
                                            } else {
                                                playSound(scan_type, "ar", false)
                                            }
                                        }
                                        isSafe = isSecure

                                        binding.txtDeviceName.visibility = View.VISIBLE
                                        binding.txtNameTitle.visibility = View.VISIBLE
                                        binding.imgSecurity.visibility = View.VISIBLE
                                    }

                                    adapter.updateAdapter(list)
                                }
                            }
                        }) {
                            fetchDevicesJob?.isActive == false
                        }
                    }
                    fetchDevicesJob = launch(Dispatchers.IO) {
                        list = fetchData(input)
                        progressJob.join()
                    }
                }
            }

        }

        binding.btnBack.setOnClickListener {
            //onBackButtonPressed()
            stopOperations()
        }

        helper.onBackPressed(this) {
            //onBackButtonPressed()
            stopOperations()
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    private fun scanning(input: String) {
        viewModel.scan(requireContext(), input, helper.getToken(requireContext()))
        val mediatorLiveData = MediatorLiveData<Pair<OpenPorts, Boolean>>()
        mediatorLiveData.addSource(viewModel.scanResponse) {
            val isSuccessful = viewModel.isRequestSuccessfull.value ?: false
            mediatorLiveData.value = Pair(it, isSuccessful)
        }

        mediatorLiveData.addSource(viewModel.isRequestSuccessfull) {
            val openPorts = viewModel.scanResponse.value ?: OpenPorts(
                "", "",
                listOf(Port("", 0, "", "", "", listOf(CvEs(0.0, "", ""))))
            )
            mediatorLiveData.value = Pair(openPorts, it)
        }
        mediatorLiveData.observe(viewLifecycleOwner) {
            if (it.second) {
                if (it.first.scanHostDeviceName != null) {
                    if (it.first.scanHostDeviceName.isEmpty()) {
                        binding.txtDeviceName.text = it.first.scanIp
                        deviceName = it.first.scanIp
                    } else {
                        binding.txtDeviceName.text = it.first.scanHostDeviceName
                        deviceName = it.first.scanHostDeviceName
                    }
                    list = it.first.openPortsInfo.toMutableList()
                    adapter.updateAdapter(list)

                    val isSecure = helper.isSecureSite(list)
                    if (isSecure) {
                        binding.imgSecurity.setImageResource(R.drawable.safe)
                        binding.txtNameTitle.setTextColor(
                            resources.getColor(
                                R.color.safe_color,
                                context?.theme
                            )
                        )
                        if (helper.getLang(requireContext()).equals("en")) {
                            playSound(scan_type, "en", true)
                            //audioUtils.playAudio(requireContext(), R.raw.secure_en)
                        } else {
                            playSound(scan_type, "ar", true)
                            //audioUtils.playAudio(requireContext(), R.raw.secure_ar)
                        }
                    } else {
                        binding.imgSecurity.setImageResource(R.drawable.danger)
                        binding.txtNameTitle.setTextColor(
                            resources.getColor(
                                R.color.danger_color,
                                context?.theme
                            )
                        )
                        if (helper.getLang(requireContext()).equals("en")) {
                            playSound(scan_type, "en", false)
                            //audioUtils.playAudio(requireContext(), R.raw.danger_en)
                        } else {
                            playSound(scan_type, "ar", false)
                            //audioUtils.playAudio(requireContext(), R.raw.danger_ar)
                        }
                    }
                    isSafe = isSecure

                    binding.txtDeviceName.visibility = View.VISIBLE
                    binding.txtNameTitle.visibility = View.VISIBLE
                    binding.imgSecurity.visibility = View.VISIBLE
                } else {
                    Log.v("Error mohamed", "null")
                    Toast.makeText(
                        requireContext(), resources.getString(R.string.invalid_link),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            binding.loadingLayout.visibility = View.GONE
        }
    }

    override fun onItemClickListener(position: Int, extras: Bundle?) {
//        TODO("Not yet implemented")
        val list: ArrayList<CvEs>
        if (extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list = extras.getParcelableArrayList(LIST, CvEs::class.java)!!
            } else {
                list = extras.getParcelableArrayList(LIST)!!
            }
            if (list.isNotEmpty()) {
                extras.putParcelableArrayList(PORTS_LIST, ArrayList(this.list))
                extras.putString(TYPE, type)
                extras.putString(DEVICE_NAME, deviceName)
                findNavController().navigate(R.id.CVEsFragment, extras)
                for (cv in list) {
                    println("${cv.id}\t${cv.cvss}\t${cv.url}")
                }
            }
        }
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {
//        TODO("Not yet implemented")
    }

    private fun onBackButtonPressed() {
        val source = requireArguments().getString(TYPE, "")
        if (source.isNotEmpty()) {
            requireArguments().putString(TYPE, source)
            if (sourceFragment != 0) {
                findNavController().navigate(sourceFragment, requireArguments())
            } else {
                if (source.equals(ROUTER)) {
                    findNavController().navigate(R.id.penResultFragment, requireArguments())
                } else {
                    findNavController().navigate(R.id.insertLinkFragment, requireArguments())
                }
            }
        } else {
            if (sourceFragment != 0) {
                findNavController().navigate(sourceFragment)
            } else {
                findNavController().navigate(R.id.insertLinkFragment)
            }
            if (source.equals(ROUTER)) {
                findNavController().navigate(R.id.penResultFragment, requireArguments())
            }

        }
        println(source)
    }

    private fun stopOperations() {
        // Cancel progress job
        progressJob?.cancel()
        // Cancel fetch devices job
        fetchDevicesJob?.cancel()
        //findNavController().navigate(SOURCE)
        onBackButtonPressed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun fetchData(input: String): MutableList<Port> {
        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                getPorts(input, object : OnDataFetchedListener<Port> {
                    override fun onAllDataFetched(data: MutableList<Port>) {
                        continuation.resume(data, null)
                    }
                })
            }
        }
    }

    private fun getPorts(input: String, callback: OnDataFetchedListener<Port>) {
        list = mutableListOf<Port>()
        viewModel.scan(requireContext(), input, helper.getToken(requireContext()))
        val observer = object : Observer<OpenPorts?> {
            override fun onChanged(value: OpenPorts?) {
                if (value != null) {
                    this@WebScanFragment.value = value
                    list.addAll(value.openPortsInfo)
                    callback.onAllDataFetched(list)
                    viewModel.scanResponse.removeObserver(this)
                }
            }
        }
        viewModel.scanResponse.observe(viewLifecycleOwner, observer)
    }

    private suspend fun progress(progress: OnProgressUpdatedListener, isCanceled: () -> Boolean) {
        withContext(Dispatchers.Main) {
            for (i in 0..100) {
                if (isCanceled()) {
                    break
                }
                progress.onUpdateProgressLoad(i)
                delay(700)
            }
        }
    }


    private fun playSound(scanType: Int, lang: String, isSecure: Boolean) {
        if (isSecure) {
            when (scanType) {
                1 -> {
                    if (lang.equals("ar")) {
                        audioUtils.playAudio(requireContext(), R.raw.url_secure_ar)
                    } else {
                        audioUtils.playAudio(requireContext(), R.raw.url_secure_en)
                    }
                }

                2 -> {
                    if (lang.equals("ar")) {
                        audioUtils.playAudio(requireContext(), R.raw.ip_secure_ar)
                    } else {
                        audioUtils.playAudio(requireContext(), R.raw.ip_secure_en)
                    }
                }

                3 -> {
                    if (lang.equals("ar")) {
                        audioUtils.playAudio(requireContext(), R.raw.router_secure_ar)
                    } else {
                        audioUtils.playAudio(requireContext(), R.raw.router_secure_en)
                    }
                }
            }
        } else {
            when (scanType) {
                1 -> {
                    if (lang.equals("ar")) {
                        audioUtils.playAudio(requireContext(), R.raw.url_insecure_ar)
                    } else {
                        audioUtils.playAudio(requireContext(), R.raw.url_insecure_en)
                    }
                }

                2 -> {
                    if (lang.equals("ar")) {
                        audioUtils.playAudio(requireContext(), R.raw.ip_insecure_ar)
                    } else {
                        audioUtils.playAudio(requireContext(), R.raw.ip_insecure_en)
                    }
                }

                3 -> {
                    if (lang.equals("ar")) {
                        audioUtils.playAudio(requireContext(), R.raw.router_insecure_ar)
                    } else {
                        audioUtils.playAudio(requireContext(), R.raw.router_insecure_en)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        audioUtils.releaseMedia()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.webScanFragment)
    }

}