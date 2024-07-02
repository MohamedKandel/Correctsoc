package com.correct.correctsoc.ui.selfScan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentPenResultBinding
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.Constants.ITEM
import com.correct.correctsoc.helper.Constants.ROUTER
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TYPE
import com.correct.correctsoc.helper.HelperClass

class PenResultFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentPenResultBinding
    private lateinit var helper: HelperClass
    private lateinit var viewModel: ScanViewModel
    private var ipAddress = ""
    private var ip = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPenResultBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[ScanViewModel::class.java]

        binding.progressCircular.startAnimation(helper.circularAnimation(3000))

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.selfPenFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.selfPenFragment)
        }

        if (arguments != null) {
            ip = requireArguments().getString(IP_ADDRESS).toString()
            if (ip != null) {
                if (ip.isEmpty()) {
                    println("ip empty")
                    getIPAddress()
                } else {
                    println("ip not empty")
                    println(ip)
                    if (ip.equals("null")) {
                        getIPAddress()
                    } else {
                        ipAddress = ip
                        binding.txtBody.append(ipAddress)
                    }
                }
            } else {
                println("ip is null")
                getIPAddress()
                //helper.getIPAddress(requireContext())
            }
            println("argument found")
        } else {
            getIPAddress()
            println("argument not found")
        }

        binding.progressLayout.setOnClickListener {
            val bundle = Bundle()
            //bundle.putString(ITEM, ipAddress)
            bundle.putInt(SOURCE, R.id.penResultFragment)
            bundle.putString(TYPE, ROUTER)
            bundle.putString(IP_ADDRESS, ipAddress)
            findNavController().navigate(R.id.webScanFragment, bundle)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    fun getIPAddress() {
        viewModel.getUserIP()
        viewModel.userIPResponse.observe(viewLifecycleOwner) {
            if (it.ipAddress != null) {
                binding.txtBody.append(it.ipAddress)
                ipAddress = it.ipAddress
            } else {
                if (!ipAddress.equals(helper.getIPAddress(requireContext()))) {
                    helper.setIPAddress(requireContext(), ipAddress)
                }
            }
        }
    }

    /*private fun onBackPressed() {
        (activity as AppCompatActivity).supportFragmentManager
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Back is pressed... Finishing the activity
                    findNavController().navigate(R.id.selfPenFragment)
                }
            })
    }*/
}