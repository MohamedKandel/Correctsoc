package com.correct.correctsoc.ui.deviceScan

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.DevicesData
import com.correct.correctsoc.databinding.FragmentDeviceDataBinding
import com.correct.correctsoc.helper.Constants.ITEM
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.ui.selfScan.ScanViewModel


class DeviceDataFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentDeviceDataBinding
    private lateinit var viewModel: ScanViewModel
    private lateinit var helper: HelperClass
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
        binding = FragmentDeviceDataBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ScanViewModel::class.java]
        helper = HelperClass.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.deviceDataFragment)

        if (arguments != null) {
            val model: DevicesData
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                model = requireArguments().getParcelable(ITEM, DevicesData::class.java)!!
            } else {
                model = requireArguments().getParcelable(ITEM)!!
            }
            if (helper.getLang(requireContext()).equals("ar")) {
                binding.txtTitle.text = model.ipAddress.mappingNumbers()
                binding.btnBack.rotation = 180f
            } else {
                binding.txtTitle.text = model.ipAddress
            }
            binding.txtDeviceName.text = model.deviceName
            binding.txtDeviceMac.text = model.macAddress
            // call API here
            if (model.macAddress.isNotEmpty()) {
                getVendorName(model.macAddress)
            } else {
                binding.txtDeviceVendor.text = "Cannot detect"
            }

            binding.btnBack.setOnClickListener {
                findNavController().navigate(R.id.devicesFragment, requireArguments())
            }

            helper.onBackPressed(this) {
                findNavController().navigate(R.id.devicesFragment, requireArguments())
            }
        }

        

        return binding.root
    }

    /*private fun onBancPressed() {
        (activity as AppCompatActivity).supportFragmentManager
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Back is pressed... Finishing the activity
                    findNavController().navigate(R.id.devicesFragment, requireArguments())
                }
            })
    }*/

    private fun getVendorName(macAddress: String) {
        viewModel.fetchResponse(macAddress)
        var vendor: String
        viewModel.vendorResponse.observe(viewLifecycleOwner) {
            if (it.equals("Unknown error") || it.equals("Not Found")) {
                vendor = "Cannot detect"
            } else {
                vendor = it
            }
            binding.txtDeviceVendor.text = vendor
            Log.i("TextVendor name", "getVendorName: $vendor")
        }

    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.deviceDataFragment)
    }
}