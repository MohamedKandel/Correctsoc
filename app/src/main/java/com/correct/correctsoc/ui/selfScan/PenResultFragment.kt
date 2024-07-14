package com.correct.correctsoc.ui.selfScan

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentPenResultBinding
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.Constants.ROUTER
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TYPE
import com.correct.correctsoc.helper.FragmentChangedListener
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
        binding = FragmentPenResultBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[ScanViewModel::class.java]
        fragmentListener.onFragmentChangedListener(R.id.penResultFragment)
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
        viewModel.getUserIP(helper.getToken(requireContext()))
        viewModel.userIPResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                if (it.result != null) {
                    if (it.result.ipAddress != null) {
                        binding.txtBody.append(it.result.ipAddress)
                        ipAddress = it.result.ipAddress
                    } else {
                        if (!ipAddress.equals(helper.getIPAddress(requireContext()))) {
                            helper.setIPAddress(requireContext(), ipAddress)
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(),it.errorMessages,Toast.LENGTH_SHORT)
                    .show()
            }
            /*if (it.ipAddress != null) {
                binding.txtBody.append(it.ipAddress)
                ipAddress = it.ipAddress
            } else {
                if (!ipAddress.equals(helper.getIPAddress(requireContext()))) {
                    helper.setIPAddress(requireContext(), ipAddress)
                }
            }*/
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.penResultFragment)
    }
}