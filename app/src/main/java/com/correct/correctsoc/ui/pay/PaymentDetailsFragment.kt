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
import com.correct.correctsoc.helper.hideKeyboard
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.helper.reMappingNumbers
import kotlin.math.max

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
    private val minimum = 1
    private val maximum = 10


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
            devices = if (binding.value.text.toString().toInt() > 0) {
                binding.value.text.toString().reMappingNumbers().toInt()
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
                Log.v("devices mohamed", "$devices")
                Log.v("months mohamed", "$months")
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.empty_device_duration),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.value.text = binding.value.text.toString().mappingNumbers()
        } else {
            binding.value.text = binding.value.text.toString()
        }

        binding.spnDuration.setOnClickListener {
            Log.v("Item count", "${arrayAdapter.count}")
            it.hideKeyboard()
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
                    years = 0
                    devices = binding.value.text.toString().reMappingNumbers().toInt()
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

                } else {
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
                if (months > 0) {
                    getCost(devices, months)
                }
            }

        binding.btnAdd.setOnClickListener {
            var number = binding.value.text.toString().toInt()
            if (number < maximum) {
                number++
                if (helper.getLang(requireContext()).equals("ar")) {
                    binding.value.text = "$number".mappingNumbers()
                } else {
                    binding.value.text = "$number"
                }
            }
        }

        binding.btnMinus.setOnClickListener {
            var number = binding.value.text.toString().toInt()
            if (number > minimum) {
                number--
                if (helper.getLang(requireContext()).equals("ar")) {
                    binding.value.text = "$number".mappingNumbers()
                } else {
                    binding.value.text = "$number"
                }
            }
        }

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