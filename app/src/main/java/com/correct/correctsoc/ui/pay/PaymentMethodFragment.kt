package com.correct.correctsoc.ui.pay

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.braintreepayments.api.BraintreeClient
import com.braintreepayments.api.DataCollector
import com.braintreepayments.api.GooglePayClient
import com.braintreepayments.api.GooglePayListener
import com.braintreepayments.api.PaymentMethodNonce
import com.braintreepayments.api.UserCanceledException
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.pay.SubscibeGooglePayBody
import com.correct.correctsoc.databinding.FragmentPaymentMethodBinding
import com.correct.correctsoc.helper.Constants.CURRENCY
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
import com.correct.correctsoc.helper.buildDialog
import com.correct.correctsoc.helper.hide
import com.correct.correctsoc.helper.openWhatsApp
import com.correct.correctsoc.helper.show
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.launch

class PaymentMethodFragment : Fragment(), GooglePayListener {

    private lateinit var binding: FragmentPaymentMethodBinding
    private lateinit var helper: HelperClass
    private lateinit var listener: NextStepListener
    private var isSelected = false
    private var googlePay = false
    private var activation = false
    private var activation_wa = false
    private lateinit var viewModel: PayViewModel
    private lateinit var braintreeClient: BraintreeClient
    private lateinit var googlePayClient: GooglePayClient
    private lateinit var dataCollector: DataCollector
    private var amount = 0
    private var price = 0.0
    private var totalPrice = 0.0
    private var discount = 0.0
    private var months = 0
    private lateinit var usersDB: UsersDB
    private var token = ""
    private lateinit var fragmentListener: FragmentChangedListener
    private var promoCode = ""

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
                    childFragmentManager.popBackStack()
                    return
                }
                // Delete parent fragment
                val parentPayFragment =
                    this@PaymentMethodFragment.parentFragment as ParentPayFragment
                parentPayFragment.changeSteps(1)
                parentFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPaymentMethodBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[PayViewModel::class.java]
        usersDB = UsersDB.getDBInstance(requireContext())
        fragmentListener.onFragmentChangedListener(R.id.paymentMethodFragment)
        binding.placeholder.hide()
        binding.progress.hide()

        if (arguments != null) {
            amount = requireArguments().getInt(DEVICES, 0)
            months = requireArguments().getInt(MONTHS, 0)
            price = requireArguments().getDouble(PRICE, 0.0)
            totalPrice = requireArguments().getDouble(TOTAL_PRICE, 0.0)
            discount = requireArguments().getDouble(DISCOUNT, 0.0)
            token = requireArguments().getString(TOKEN_KEY, "")
            promoCode = requireArguments().getString(PROMO, "")
            setupGooglePay(token)
//            Log.v("pay data", "$amount")
//            Log.v("pay data", "$months")
            Log.v("pay data price", "$price")
            Log.v("pay data total", "$totalPrice")
            Log.v("pay data discount", "$discount")
        }

        binding.nextBtn.setOnClickListener {
            if (!isSelected) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.choose_pay),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.placeholder.show()
                binding.progress.show()
                if (googlePay) {
                    binding.placeholder.hide()
                    binding.progress.hide()

                    AlertDialog.Builder(requireContext())
                        .buildDialog(title = resources.getString(R.string.not_supported_title),
                            msg = resources.getString(R.string.not_supported_msg),
                            positiveButton = resources.getString(R.string.ok),
                            negativeButton = resources.getString(R.string.cancel),
                            positiveButtonFunction = {

                            },
                            negativeButtonFunction = {

                            })

                    /*val googlePayRequest = GooglePayRequest()
                    googlePayRequest.transactionInfo = TransactionInfo.newBuilder()
                        .setTotalPrice("$price")
                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                        .setCurrencyCode(CURRENCY)
                        .build()
                    googlePayRequest.isBillingAddressRequired = true
                    binding.placeholder.hide()
                    binding.progress.hide()
                    googlePayClient.requestPayment(requireActivity(), googlePayRequest)*/
                } else if (activation) {
                    val bundle = Bundle()
                    bundle.putString(PROMO, promoCode)
                    (parentFragment as? ParentPayFragment)?.replaceFragment(
                        ActivationCodeFragment(), isHeaderVisible = false,
                        bundle = bundle
                    )
                } else if (activation_wa) {
                    // open whatsapp with the number
                    this.openWhatsApp("19714477890")
                    binding.placeholder.hide()
                    binding.progress.hide()
                }
            }

        }

        binding.googlePayLayout.setOnClickListener {
            if (!isSelected) {
                binding.googlePayChoice.setImageResource(R.drawable.fill_circle_icon)
                isSelected = true
            } else {
                binding.activationChoice.setImageResource(R.drawable.empty_circle_icon)
                binding.activationChoiceWa.setImageResource(R.drawable.empty_circle_icon)
                binding.googlePayChoice.setImageResource(R.drawable.fill_circle_icon)
                isSelected = true
            }
            orderGooglePay()
            googlePay = true
            activation = false
            activation_wa = false
        }

        binding.activitionLayout.setOnClickListener {
            googlePay = false
            activation = true
            activation_wa = false
            if (!isSelected) {
                binding.activationChoice.setImageResource(R.drawable.fill_circle_icon)
                isSelected = true
            } else {
                binding.googlePayChoice.setImageResource(R.drawable.empty_circle_icon)
                binding.activationChoiceWa.setImageResource(R.drawable.empty_circle_icon)
                binding.activationChoice.setImageResource(R.drawable.fill_circle_icon)
                isSelected = true
            }
        }

        binding.activationLayoutWa.setOnClickListener {
            if (!isSelected) {
                binding.activationChoiceWa.setImageResource(R.drawable.fill_circle_icon)
                isSelected = true
            } else {
                binding.googlePayChoice.setImageResource(R.drawable.empty_circle_icon)
                binding.activationChoiceWa.setImageResource(R.drawable.fill_circle_icon)
                binding.activationChoice.setImageResource(R.drawable.empty_circle_icon)
                isSelected = true
            }
            googlePay = false
            activation = false
            activation_wa = true
        }

        return binding.root
    }

    private fun orderGooglePay() {
        viewModel.orderPayWithGooglePay()
        viewModel.orderPayWithGooglePayResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                if (it.result != null) {
                    token = it.result
                }
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun subscribePayWithGooglePay(body: SubscibeGooglePayBody) {
        viewModel.subscribeWithGooglePay(body)
        val observer = object : Observer<ForgotResponse> {
            override fun onChanged(value: ForgotResponse) {
                if (value.isSuccess) {
                    if (parentFragment is ParentPayFragment) {
                        binding.placeholder.hide()
                        binding.progress.hide()
                        (parentFragment as? ParentPayFragment)?.replaceFragment(
                            PaymentSuccessFragment()
                        )
                        listener.onNextStepListener(2)
                    }
                } else {
                    Toast.makeText(requireContext(), value.errorMessages, Toast.LENGTH_SHORT).show()
                }
                viewModel.subscribeWithGooglePay.removeObserver(this)
            }
        }
        viewModel.subscribeWithGooglePay.observe(viewLifecycleOwner, observer)
    }

    private fun setupGooglePay(token: String) {
        braintreeClient = BraintreeClient(requireContext(), token)
        // collect device data
        dataCollector = DataCollector(braintreeClient)
        googlePayClient = GooglePayClient(this, braintreeClient)
        googlePayClient.setListener(this)
        googlePayClient.isReadyToPay(requireActivity()) { isReadyToPay, error ->
            if (isReadyToPay) {
                binding.googlePayLayout.show()
            } else {
                binding.googlePayLayout.hide()
            }
        }
    }

    override fun onGooglePaySuccess(paymentMethodNonce: PaymentMethodNonce) {
        binding.placeholder.show()
        binding.progress.show()
        dataCollector.collectDeviceData(requireContext()) { deviceData, error ->
            if (deviceData != null) {
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    val phone = usersDB.dao().getUserPhone(id) ?: ""
                    val body = SubscibeGooglePayBody(
                        amount = totalPrice.toInt(),
                        discount = discount.toInt(),
                        devicesNumber = amount,
                        currencyIsoCode = CURRENCY,
                        deviceData = deviceData,
                        months = months,
                        nonce = paymentMethodNonce.string,
                        phoneNumber = phone,
                        promoCode = promoCode
                    )
                    // amount is price
                    Log.d("payment mohamed", "$totalPrice")
                    Log.d("payment mohamed", CURRENCY)
                    Log.d("payment mohamed", deviceData)
                    Log.d("payment mohamed", "$months")
                    Log.d("payment mohamed", paymentMethodNonce.string)
                    Log.d("payment mohamed", phone)
                    Log.d("payment mohamed", "$discount")
                    Log.d("payment mohamed", promoCode)
                    subscribePayWithGooglePay(body)
                }
            } else {
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    val phone = usersDB.dao().getUserPhone(id) ?: ""
                    val body = SubscibeGooglePayBody(
                        amount = price.toInt(),
                        devicesNumber = amount,
                        currencyIsoCode = CURRENCY,
                        deviceData = helper.getDeviceID(requireContext()),
                        months = months,
                        nonce = paymentMethodNonce.string,
                        phoneNumber = phone
                    )
                    subscribePayWithGooglePay(body)
                }
            }
        }
    }

    override fun onGooglePayFailure(error: Exception) {
        if (error is UserCanceledException) {
            Log.v("error mohamed", "user canceled")
        } else {
            Log.e("error mohamed", "onGooglePayFailure: ", error)
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.paymentMethodFragment)
    }

}