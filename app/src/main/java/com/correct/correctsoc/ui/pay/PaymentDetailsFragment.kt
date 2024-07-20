package com.correct.correctsoc.ui.pay

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentPaymentDetailsBinding
import com.correct.correctsoc.helper.Constants.DEVICES
import com.correct.correctsoc.helper.Constants.MONTHS
import com.correct.correctsoc.helper.Constants.PRICE
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import com.correct.correctsoc.helper.Constants.TOTAL_PRICE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.NextStepListener

class PaymentDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPaymentDetailsBinding
    private lateinit var helper: HelperClass
    private lateinit var listener: NextStepListener
    private var devices = 0
    private var months = 0
    private var years = 0
    private var price = 0.0
    private lateinit var viewModel: PayViewModel
    private var token = ""
    private lateinit var fragmentListener: FragmentChangedListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is NextStepListener) {
            listener = parentFragment as NextStepListener
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
        if (context is FragmentChangedListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (childFragmentManager.backStackEntryCount > 1) {
                    return
                }
                findNavController().navigate(R.id.homeFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPaymentDetailsBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[PayViewModel::class.java]

        fragmentListener.onFragmentChangedListener(R.id.paymentDetailsFragment)
        val durations = resources.getStringArray(R.array.durations)
        var arrayAdapter = ArrayAdapter(
            requireContext(), R.layout.duration_spn_item,
            durations
        )
        binding.spnDuration.setAdapter(arrayAdapter)

        binding.nextBtn.setOnClickListener {
            devices = if (binding.txtDeviceNumber.text.toString().isNotEmpty()) {
                binding.txtDeviceNumber.text.toString().toInt()
            } else {
                0
            }
            if (devices > 0 && months > 0) {
                if (parentFragment is ParentPayFragment) {
                    val bundle = Bundle()
                    bundle.putInt(MONTHS, months)
                    bundle.putInt(DEVICES, devices)
                    bundle.putDouble(TOTAL_PRICE, price)
                    (parentFragment as? ParentPayFragment)?.replaceFragment(
                        ReceiptFragment(),
                        bundle
                    )
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.empty_device_duration),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.spnDuration.setOnClickListener {
            Log.v("Item count","${arrayAdapter.count}")
            if (arrayAdapter.count != durations.size) {
                arrayAdapter = ArrayAdapter(
                    requireContext(), R.layout.duration_spn_item,
                    durations
                )
                binding.spnDuration.setAdapter(arrayAdapter)
                arrayAdapter.notifyDataSetChanged()
            }
        }

        binding.spnDuration.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                //Log.v("Selected mohamed", arrayAdapter.getItem(position).toString())
                // Log.v("Selected mohamed", "$position")
                if (devices > 0) {
                    if (binding.txtDeviceNumber.text.toString().isNotEmpty()) {
                        years = 0
                        devices = binding.txtDeviceNumber.text.toString().toInt()
                        when (position) {
                            0 -> {
                                months = 6
                            }

                            1 -> {
                                months = 12
                            }

                            else -> {
                                months = 0
                            }
                        }
                    }
                }
            }

        binding.txtDeviceNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    devices = s.toString().toInt()
                } else {
                    if(months > 0 && devices > 0) {
                        getCost(devices, months)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty() && months > 0) {
                    devices = s.toString().toInt()
                    if(devices > 0) {
                        getCost(devices, months)
                    }
                }
            }
        })

        return binding.root
    }

    private fun getCost(device: Int, months: Int, year: Int = 0) {
        Log.v("data pay", "$months\t $device")
        viewModel.getCost(device, months, year)
        viewModel.getCostResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                if (it.result != null) {
                    price = it.result.toString().toDouble()
                }
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.paymentDetailsFragment)
    }
}