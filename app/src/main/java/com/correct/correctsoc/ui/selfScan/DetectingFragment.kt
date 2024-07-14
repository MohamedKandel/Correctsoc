package com.correct.correctsoc.ui.selfScan

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentDetectingBinding
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass

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

        binding.progressCircular.startAnimation(helper.circularAnimation(3000))

        getAPIAddress()

        val thread = Thread {

            startProgress()
        }
        thread.start()

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
        viewModel.getUserIP(helper.getToken(requireContext()))
        viewModel.userIPResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                if (it.result != null) {
                    ipAddress = it.result.ipAddress
                    helper.setIPAddress(requireContext(), ipAddress)
                }
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
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

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.detectingFragment)
    }
}