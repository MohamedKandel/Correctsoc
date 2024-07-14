package com.correct.correctsoc.ui.pay

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.braintreepayments.api.BraintreeClient
import com.braintreepayments.api.DataCollector
import com.braintreepayments.api.GooglePayClient
import com.braintreepayments.api.GooglePayListener
import com.braintreepayments.api.GooglePayRequest
import com.braintreepayments.api.PaymentMethodNonce
import com.braintreepayments.api.UserCanceledException
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.pay.SubscibeGooglePayBody
import com.correct.correctsoc.databinding.FragmentPaymentMethodBinding
import com.correct.correctsoc.helper.Constants
import com.correct.correctsoc.helper.Constants.CURRENCY
import com.correct.correctsoc.helper.Constants.DEVICES
import com.correct.correctsoc.helper.Constants.MONTHS
import com.correct.correctsoc.helper.Constants.PRICE
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.NextStepListener
import com.correct.correctsoc.room.UsersDB
import com.google.android.gms.wallet.TransactionInfo
import com.google.android.gms.wallet.WalletConstants
import kotlinx.coroutines.launch
import java.lang.Exception

class PaymentMethodFragment : Fragment(), GooglePayListener {

    private lateinit var binding: FragmentPaymentMethodBinding
    private lateinit var helper: HelperClass
    private lateinit var listener: NextStepListener
    private var isSelected = false
    private var googlePay = false
    private var activation = false
    private lateinit var viewModel: PayViewModel
    private lateinit var braintreeClient: BraintreeClient
    private lateinit var googlePayClient: GooglePayClient
    private lateinit var dataCollector: DataCollector
    private var amount = 0
    private var price = 0.0
    private var months = 0
    private lateinit var usersDB: UsersDB
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
        binding.placeholder.visibility = View.GONE
        binding.progress.visibility = View.GONE

        if (arguments != null) {
            amount = requireArguments().getInt(DEVICES, 0)
            months = requireArguments().getInt(MONTHS, 0)
            price = requireArguments().getDouble(PRICE, 0.0)
            token = requireArguments().getString(TOKEN_KEY, "")
            setupGooglePay(token)
            Log.v("pay data", "$amount")
            Log.v("pay data", "$months")
            Log.v("pay data", "$price")
        }

        binding.nextBtn.setOnClickListener {
            if (!isSelected) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.choose_pay),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.placeholder.visibility = View.VISIBLE
                binding.progress.visibility = View.VISIBLE
                if (googlePay) {
                    val googlePayRequest = GooglePayRequest()
                    googlePayRequest.transactionInfo = TransactionInfo.newBuilder()
                        .setTotalPrice("$price")
                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                        .setCurrencyCode(CURRENCY)
                        .build()
                    googlePayRequest.isBillingAddressRequired = true
                    binding.placeholder.visibility = View.GONE
                    binding.progress.visibility = View.GONE
                    googlePayClient.requestPayment(requireActivity(), googlePayRequest)
                }
            }

        }

        binding.googlePayLayout.setOnClickListener {
            if (!isSelected) {
                binding.googlePayChoice.setImageResource(R.drawable.fill_circle_icon)
                isSelected = true
            } else {
                binding.activationChoice.setImageResource(R.drawable.empty_circle_icon)
                binding.googlePayChoice.setImageResource(R.drawable.fill_circle_icon)
                isSelected = true
            }
            orderGooglePay()
            googlePay = true
            activation = false
        }

        binding.activitionLayout.setOnClickListener {
            googlePay = false
            activation = true
            if (!isSelected) {
                binding.activationChoice.setImageResource(R.drawable.fill_circle_icon)
                isSelected = true
            } else {
                binding.googlePayChoice.setImageResource(R.drawable.empty_circle_icon)
                binding.activationChoice.setImageResource(R.drawable.fill_circle_icon)
                isSelected = true
            }
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
                        binding.placeholder.visibility = View.GONE
                        binding.progress.visibility = View.GONE
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
                binding.googlePayLayout.visibility = View.VISIBLE
            } else {
                binding.googlePayLayout.visibility = View.GONE
            }
        }
    }

    override fun onGooglePaySuccess(paymentMethodNonce: PaymentMethodNonce) {
        binding.placeholder.visibility = View.VISIBLE
        binding.progress.visibility = View.VISIBLE
        dataCollector.collectDeviceData(requireContext()) { deviceData, error ->
            if (deviceData != null) {
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    val phone = usersDB.dao().getUserPhone(id) ?: ""
                    val body = SubscibeGooglePayBody(
                        amount = price.toInt(),
                        devicesNumber = amount,
                        currencyIsoCode = CURRENCY,
                        deviceData = deviceData,
                        months = months,
                        nonce = paymentMethodNonce.string,
                        phoneNumber = phone
                    )
                    // amount is price
                    Log.d("payment mohamed", "$price")
                    Log.d("payment mohamed", CURRENCY)
                    Log.d("payment mohamed", deviceData)
                    Log.d("payment mohamed", "$months")
                    Log.d("payment mohamed", paymentMethodNonce.string)
                    Log.d("payment mohamed", phone)
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