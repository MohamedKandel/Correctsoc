package com.correct.correctsoc.ui.pay

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.correct.correctsoc.R
import com.correct.correctsoc.data.pay.PromoCodePercentResponse
import com.correct.correctsoc.databinding.FragmentReceiptBinding
import com.correct.correctsoc.helper.Constants.API_TAG
import com.correct.correctsoc.helper.Constants.DEVICES
import com.correct.correctsoc.helper.Constants.DISCOUNT
import com.correct.correctsoc.helper.Constants.MONTHS
import com.correct.correctsoc.helper.Constants.PRICE
import com.correct.correctsoc.helper.Constants.PROMO
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import com.correct.correctsoc.helper.Constants.TOTAL_PRICE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.NextStepListener
import com.correct.correctsoc.helper.hide
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.helper.reMappingNumbers
import com.correct.correctsoc.helper.upperCaseOnly
import com.correct.correctsoc.helper.show
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.round

class ReceiptFragment : Fragment() {

    private lateinit var binding: FragmentReceiptBinding
    private lateinit var helper: HelperClass
    private lateinit var listener: NextStepListener
    private lateinit var fragmentListener: FragmentChangedListener
    private lateinit var viewModel: PayViewModel
    private var totalPrice = 0.0
    private var price = 0.0
    private var discount = 0.0
    private var token = ""
    private var promoCode = ""
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

        binding.txtViewDiscount.hide()
        binding.txtDiscount.hide()

        if (arguments != null) {
            devices = requireArguments().getInt(DEVICES, 0)
            months = requireArguments().getInt(MONTHS, 0)
            totalPrice = requireArguments().getDouble(TOTAL_PRICE, 0.0)

            if (months > 0 && devices > 0) {
                val device_unit = if (devices > 1) {
                    resources.getString(R.string.more_devices)
                } else {
                    resources.getString(R.string.one_device)
                }
                if (helper.getLang(requireContext()).equals("ar")) {
                    binding.txtDevicesNumber.text = "• $devices $device_unit".mappingNumbers()
                } else {
                    binding.txtDevicesNumber.text = "• $devices $device_unit"
                }
                when (months) {
                    6 -> {
                        binding.txtDuration.text = resources.getString(R.string.six_months)
                    }

                    12 -> {
                        binding.txtDuration.text = resources.getString(R.string.one_year)
                    }
                }
                if (totalPrice > 0) {
                    if (helper.getLang(requireContext()).equals("ar")) {
                        binding.txtAmount.text = "$totalPrice $".mappingNumbers()
                        binding.txtTotal.text = "$totalPrice $".mappingNumbers()
                    } else {
                        binding.txtAmount.text = "$totalPrice $"
                        binding.txtTotal.text = "$totalPrice $"
                    }
                    //  getCost(devices,months)
                }
                getCost(devices, months)

            }
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.promo_code_layout_dialog)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val promo = dialog.findViewById<EditText>(R.id.txt_promo)
        val btn = dialog.findViewById<Button>(R.id.btn_apply)
        val close = dialog.findViewById<ImageView>(R.id.close_icon)

        promo.upperCaseOnly()

        close.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }

        btn.setOnClickListener {
            val code = promo.text?.toString()
            if (code != null) {
                if (code.isNotEmpty()) {
                    dialog.dismiss()
                    dialog.cancel()
                    binding.txtPromo.setText(code)
                    getPromoCodePercent(code)
                }
            }
        }

        dialog.show()

        binding.txtPromo.inputType = android.text.InputType.TYPE_NULL
        binding.txtPromo.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val imm =
                    view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
                dialog.show()
            }
        }

        binding.txtPromo.setOnClickListener {
            val imm =
                view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
            dialog.show()
        }

        binding.nextBtn.setOnClickListener {
            if (devices > 0 && months > 0) {
                if (parentFragment is ParentPayFragment) {
                    price =
                        binding.txtTotal.text.toString().reMappingNumbers().replace("$", "").trim()
                            .toDouble()
                    totalPrice =
                        binding.txtAmount.text.toString().reMappingNumbers().replace("$", "").trim()
                            .toDouble()
                    if (!binding.txtDiscount.text.toString()
                            .equals(resources.getString(R.string.discount))
                    ) {
                        discount =
                            binding.txtDiscount.text.toString().reMappingNumbers().replace("$", "")
                                .trim()
                                .toDouble()
                    } else {
                        discount = 0.0
                    }

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

        binding.btnApply.setOnClickListener {
            //apply promo-code
            if (binding.txtPromo.text.toString().isNotEmpty()) {
                getPromoCodePercent(binding.txtPromo.text.toString())
            }
        }

        binding.imgClear.setOnClickListener {
            clearPromoCode(binding.txtViewPromoName.text.toString())
        }

        binding.txtPromo.upperCaseOnly()

        return binding.root
    }

    private fun getPromoCodePercent(code: String) {
        viewModel.getPromoCodePercent(code)
        val observer = object : Observer<PromoCodePercentResponse?> {
            override fun onChanged(value: PromoCodePercentResponse?) {
                if (value != null) {
                    if (value.promotionPercentage > 0) {
                        discount = value.promotionPercentage
                        promoCodeSuccess(code, discount)
                        Log.i(API_TAG, "$discount")
                        Log.i(API_TAG, "${value.promotionPercentage}")
                        Log.i(API_TAG, "$totalPrice")
                    } else {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.no_promo),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    viewModel.promoCodePercentResponse.removeObserver(this)
                } else {
                    discount = 0.0
                }
            }
        }
        viewModel.promoCodePercentResponse.observe(viewLifecycleOwner, observer)
    }

    @SuppressLint("SetTextI18n")
    private fun promoCodeSuccess(promo: String, discount: Double) {
        // display green layout and gif at first
        binding.promoCodeLayout.show()

        Log.i("Payment data mohamed", promo)
        Log.i("Payment data mohamed", "$discount")

        binding.btnApply.visibility = View.INVISIBLE
        binding.promoCodeLayout.radius = resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
        binding.promoCodeLayout.strokeWidth =
            resources.getDimension(com.intuit.sdp.R.dimen._2sdp).toInt()
        binding.promoCodeLayout.strokeColor =
            resources.getColor(R.color.discount_color, context?.theme)

        binding.layoutGreen.show()
        binding.paymentSuccessGif.show()

        promoCode = promo
        /*
    hide all green layout views expect gif after 1 second
    display another views
    */
        Handler(Looper.getMainLooper()).postDelayed({
            binding.promoCodeLayout.radius =
                resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
            binding.promoCodeLayout.strokeWidth =
                resources.getDimension(com.intuit.sdp.R.dimen._2sdp).toInt()
            binding.promoCodeLayout.strokeColor =
                resources.getColor(R.color.discount_color, context?.theme)

            binding.txtViewDiscount.show()
            binding.txtDiscount.show()

            if (helper.getLang(requireContext()).equals("ar")) {
                binding.txtDiscount.text =
                    "${(calculateDiscount(discount, totalPrice))} $".mappingNumbers()
            } else {
                binding.txtDiscount.text =
                    "${(calculateDiscount(discount, totalPrice))} $"
            }

            if (helper.getLang(requireContext()).equals("ar")) {
                binding.txtTotal.text =
                    "${
                        (totalPrice.minus(
                            calculateDiscount(
                                discount,
                                totalPrice
                            )
                        )).round()
                    } $".mappingNumbers()
            } else {
                binding.txtTotal.text =
                    "${(totalPrice.minus(calculateDiscount(discount, totalPrice))).round()} $"
            }
            //binding.txtTotal.text.toString().replace("$", "").trim().toDouble().minus(discount)

            totalPrice =
                binding.txtAmount.text.toString().reMappingNumbers().replace("$", "").trim()
                    .toDouble()
            this.discount =
                binding.txtDiscount.text.toString().reMappingNumbers().replace("$", "").trim()
                    .toDouble()
            price = binding.txtTotal.text.toString().reMappingNumbers().replace("$", "").trim()
                .toDouble()

            binding.txtViewPromoName.text = promo

            binding.promoCode.hide()
            binding.promoCodeSuccessLayout.show()
            if (helper.getLang(requireContext()).equals("ar")) {
                binding.txtViewDiscountPercent.text =
                    "${discount * 100}% ${resources.getString(R.string.discount_off)}".mappingNumbers()
            } else {
                binding.txtViewDiscountPercent.text =
                    "${discount * 100}% ${resources.getString(R.string.discount_off)}"
            }
        }, 1000)

        // hide gif after 1.5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            binding.paymentSuccessGif.hide()
        }, 1500)
    }

    @SuppressLint("SetTextI18n")
    private fun clearPromoCode(promo: String) {
        viewModel.removePromoCode()
        binding.promoCodeLayout.radius = resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
        binding.promoCodeLayout.strokeWidth =
            resources.getDimension(com.intuit.sdp.R.dimen._2sdp).toInt()
        binding.promoCodeLayout.strokeColor =
            resources.getColor(R.color.white, context?.theme)

        binding.txtViewDiscount.hide()
        binding.txtDiscount.hide()
        binding.btnApply.show()

        //val returnDiscount = totalPrice - (discount * 100)

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.txtTotal.text =
                "${binding.txtAmount.text}".mappingNumbers()
        } else {
            binding.txtTotal.text =
                "${binding.txtAmount.text}"
        }
        promoCode = ""
        discount = 0.0
        totalPrice =
            binding.txtAmount.text.toString().reMappingNumbers().replace("$", "").trim().toDouble()
        price =
            binding.txtTotal.text.toString().reMappingNumbers().replace("$", "").trim().toDouble()

        binding.txtPromo.setText(promo)
        binding.txtPromo.setSelection(binding.txtPromo.text.length)

        binding.promoCode.show()
        binding.layoutGreen.hide()
        binding.promoCodeSuccessLayout.hide()
    }

    private fun Double.round(): Double = round(this * 10) / 10
    private fun calculateDiscount(discount: Double, totalPrice: Double) =
        (totalPrice * discount).round()

    @SuppressLint("SetTextI18n")
    private fun getCost(device: Int, months: Int, year: Int = 0) {
        Log.v("data pay", "$months\t $device")
        viewModel.getCost(device, months, year)
        viewModel.getCostResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                if (it.result != null) {
                    totalPrice = it.result.toDouble()
                    price = it.result.toDouble()

                    if (totalPrice != binding.txtAmount.text.toString().reMappingNumbers()
                            .replace("$", "").trim()
                            .toDouble()
                    ) {
                        if (helper.getLang(requireContext()).equals("ar")) {
                            binding.txtAmount.text = "${it.result} $".mappingNumbers()
                            binding.txtTotal.text = "${it.result} $".mappingNumbers()
                        } else {
                            binding.txtAmount.text = "${it.result} $"
                            binding.txtTotal.text = "${it.result} $"
                        }
                    }
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
                    bundle.putDouble(TOTAL_PRICE, totalPrice)
                    //val priceDiscount = totalPrice - discount
                    bundle.putDouble(DISCOUNT, discount)
                    bundle.putDouble(PRICE, price)
                    bundle.putString(PROMO, promoCode)
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
        if (binding.txtPromo.text.toString().isNotEmpty()) {
            getPromoCodePercent(binding.txtPromo.text.toString())
        }
        fragmentListener.onFragmentChangedListener(R.id.receiptFragment)
    }
}