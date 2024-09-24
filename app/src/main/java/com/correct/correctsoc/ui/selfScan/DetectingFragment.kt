package com.correct.correctsoc.ui.selfScan

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentDetectingBinding
import com.correct.correctsoc.helper.ConnectionManager
import com.correct.correctsoc.helper.ConnectivityListener
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.buildDialog
import com.correct.correctsoc.helper.circularAnimation

class DetectingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentDetectingBinding
    private lateinit var helper: HelperClass
    private val handler = Handler(Looper.getMainLooper())
    private var isRun = true
    private lateinit var viewModel: ScanViewModel
    private var ipAddress = ""
    private lateinit var fragmentListener: FragmentChangedListener
    private lateinit var connectionManager: ConnectionManager
    private var isConnected = MutableLiveData<Boolean>()

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
        binding = FragmentDetectingBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[ScanViewModel::class.java]
        fragmentListener.onFragmentChangedListener(R.id.detectingFragment)
        helper.onBackPressed(this) {
            findNavController().navigate(R.id.selfPenFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.selfPenFragment)
        }



        connectionManager = ConnectionManager(requireContext())

        val thread = Thread {
            startProgress()
        }
        thread.start()

        connectionManager.observe()
        connectionManager.statusLiveData.observe(viewLifecycleOwner) {
            when (it) {
                ConnectivityListener.Status.AVAILABLE -> {
                    isConnected.postValue(true)
                    getAPIAddress()
                }

                ConnectivityListener.Status.UNAVAILABLE -> {
                    isConnected.postValue(false)
                    isRun = false
                    noInternet()
                    thread.interrupt()
                    handler.post {
                    }
                    findNavController().navigate(R.id.selfPenFragment)
                }

                ConnectivityListener.Status.LOST -> {
                    isConnected.postValue(false)
                    isRun = false
                    noInternet()
                    thread.interrupt()
                    handler.post {
                    }
                    findNavController().navigate(R.id.selfPenFragment)
                }

                ConnectivityListener.Status.LOSING -> {
                    isConnected.postValue(false)
                    isRun = false
                    noInternet()
                    thread.interrupt()
                    handler.post {
                    }
                    findNavController().navigate(R.id.selfPenFragment)
                }
            }
        }


        isConnected.observe(viewLifecycleOwner) {
            if (!it) {
                isRun = false
                noInternet()
                thread.interrupt()
                handler.post {
                }
                findNavController().navigate(R.id.selfPenFragment)
            }
        }


        binding.btnStop.setOnClickListener {
            isRun = false
            thread.interrupt()
            handler.post {

            }
            findNavController().navigate(R.id.selfPenFragment)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    private fun getAPIAddress() {
        viewModel.getExternalIP()
        viewModel.externalIPResponse.observe(viewLifecycleOwner) {
            ipAddress = it
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startProgress() {
        for (i in 0..100) {
            if (isRun) {
                try {
                    Thread.sleep(100)
                    handler.post {
                        binding.txtPercent.text = "$i%"
                        if (i == 100) {
                            Log.d("percent mohamed", "startProgress: finished")
                            val bundle = Bundle()
                            bundle.putString(IP_ADDRESS, ipAddress)
                            findNavController().navigate(R.id.penResultFragment, bundle)
                            Log.i("ip address mohamed", "onCreateView: $ipAddress")
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

    }

    private fun noInternet() {
        AlertDialog.Builder(requireContext())
            .buildDialog(title = resources.getString(R.string.warning),
                msg = resources.getString(R.string.no_internet_connection),
                icon = R.drawable.no_internet_icon,
                positiveButton = resources.getString(R.string.ok),
                negativeButton = resources.getString(R.string.cancel),
                positiveButtonFunction = {

                },
                negativeButtonFunction = {

                })
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.detectingFragment)
        binding.progressCircular.startAnimation(circularAnimation(3000))
    }

    override fun onPause() {
        super.onPause()
        // stop animation
        binding.progressCircular.clearAnimation()
    }
}