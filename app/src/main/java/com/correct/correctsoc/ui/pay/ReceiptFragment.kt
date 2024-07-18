package com.correct.correctsoc.ui.pay

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentReceiptBinding
import com.correct.correctsoc.helper.Constants
import com.correct.correctsoc.helper.Constants.DEVICES
import com.correct.correctsoc.helper.Constants.MONTHS
import com.correct.correctsoc.helper.Constants.PRICE
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.NextStepListener
import com.correct.correctsoc.helper.upperCaseOnly

class ReceiptFragment : Fragment() {

    private lateinit var binding: FragmentReceiptBinding
    private lateinit var helper: HelperClass
    private lateinit var listener: NextStepListener
    private lateinit var fragmentListener: FragmentChangedListener
    private lateinit var viewModel: PayViewModel
    private var price = 0.0
    private var token = ""
    private var devices = 0
    private var months = 0

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
                    childFragmentManager.popBackStack()
                    return
                }
                // Delete parent fragment
                val parentPayFragment =
                    this@ReceiptFragment.parentFragment as ParentPayFragment
                parentPayFragment.changeSteps(1)
                parentFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentReceiptBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[PayViewModel::class.java]

        fragmentListener.onFragmentChangedListener(R.id.receiptFragment)

        binding.txtViewDiscount.visibility = View.GONE
        binding.txtDiscount.visibility = View.GONE

        if (arguments != null) {
            devices = requireArguments().getInt(DEVICES, 0)
            months = requireArguments().getInt(MONTHS, 0)
            price = requireArguments().getDouble(PRICE, 0.0)

            if (months > 0 && devices > 0) {
                val device_unit = if (devices > 1) {
                    resources.getString(R.string.more_devices)
                } else {
                    resources.getString(R.string.one_device)
                }
                binding.txtDevicesNumber.text = "• $devices $device_unit"
                when (months) {
                    6 -> {
                        binding.txtDuration.text = resources.getString(R.string.six_months)
                    }

                    12 -> {
                        binding.txtDuration.text = resources.getString(R.string.one_year)
                    }
                }
                if (price > 0) {
                    binding.txtAmount.text = "$price $"
                    binding.txtTotal.text = "$price $"
                } else {
                    getCost(devices, months)
                }
            }
        }

        binding.nextBtn.setOnClickListener {
            if (devices > 0 && months > 0) {
                if (parentFragment is ParentPayFragment) {
                    price = binding.txtTotal.text.toString().replace("$","").trim().toDouble()
                    orderGooglePay()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.empty_device_duration),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.txtApply.setOnClickListener {
            //apply promo-code
            if (binding.txtPromo.text.toString().isNotEmpty()) {
                promoCodeSuccess(binding.txtPromo.text.toString())
            }
        }

        binding.imgClear.setOnClickListener {
            clearPromoCode(binding.txtViewPromoName.text.toString())
        }

        binding.txtPromo.upperCaseOnly()

        return binding.root
    }

    private fun promoCodeSuccess(promo: String) {
        // display green layout and gif at first
        binding.promoCodeLayout.visibility = View.VISIBLE

        binding.promoCodeLayout.radius = resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
        binding.promoCodeLayout.strokeWidth =
            resources.getDimension(com.intuit.sdp.R.dimen._2sdp).toInt()
        binding.promoCodeLayout.strokeColor =
            resources.getColor(R.color.discount_color, context?.theme)

        binding.layoutGreen.visibility = View.VISIBLE
        binding.paymentSuccessGif.visibility = View.VISIBLE

        /*
        hide all green layout views expect gif after 1 second
        display another views
        */
        Handler(Looper.getMainLooper()).postDelayed({
            binding.promoCodeLayout.radius = resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
            binding.promoCodeLayout.strokeWidth =
                resources.getDimension(com.intuit.sdp.R.dimen._2sdp).toInt()
            binding.promoCodeLayout.strokeColor =
                resources.getColor(R.color.discount_color, context?.theme)

            binding.txtViewDiscount.visibility = View.VISIBLE
            binding.txtDiscount.visibility = View.VISIBLE
            binding.txtDiscount.text = "-550 $"

            binding.txtTotal.text = "${
                binding.txtTotal.text.toString().replace("$", "").trim().toDouble().minus(550)
            } $"

            binding.txtViewPromoName.text = promo

            binding.promoCode.visibility = View.GONE
            binding.promoCodeSuccessLayout.visibility = View.VISIBLE
        }, 1000)

        // hide gif after 1.5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            binding.paymentSuccessGif.visibility = View.GONE
        }, 1500)
    }

    private fun clearPromoCode(promo: String) {
        binding.promoCodeLayout.radius = resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
        binding.promoCodeLayout.strokeWidth =
            resources.getDimension(com.intuit.sdp.R.dimen._2sdp).toInt()
        binding.promoCodeLayout.strokeColor =
            resources.getColor(R.color.white, context?.theme)

        binding.txtViewDiscount.visibility = View.GONE
        binding.txtDiscount.visibility = View.GONE

        binding.txtTotal.text =
            "${binding.txtTotal.text.toString().replace("$", "").trim().toDouble().plus(550)} $"

        binding.txtPromo.setText(promo)
        binding.txtPromo.setSelection(binding.txtPromo.text.length)

        binding.promoCode.visibility = View.VISIBLE
        binding.layoutGreen.visibility = View.GONE
        binding.promoCodeSuccessLayout.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun getCost(device: Int, months: Int, year: Int = 0) {
        Log.v("data pay", "$months\t $device")
        viewModel.getCost(device, months, year)
        viewModel.getCostResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                if (it.result != null) {
                    binding.txtAmount.text = "${it.result} $"
                    binding.txtTotal.text = "${it.result} $"
                }
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun orderGooglePay() {
        viewModel.orderPayWithGooglePay()
        viewModel.orderPayWithGooglePayResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                if (it.result != null) {
                    token = it.result

                    val bundle = Bundle()
                    bundle.putInt(DEVICES, devices)
                    bundle.putInt(MONTHS, months)
                    bundle.putDouble(PRICE, price)
                    bundle.putString(TOKEN_KEY, token)
                    (parentFragment as? ParentPayFragment)?.replaceFragment(
                        PaymentMethodFragment(),
                        bundle
                    )
                    listener.onNextStepListener(1)
                }
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.receiptFragment)
    }
}