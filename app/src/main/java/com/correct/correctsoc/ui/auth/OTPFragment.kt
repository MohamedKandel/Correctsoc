package com.correct.correctsoc.ui.auth

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.AuthResponse
import com.correct.correctsoc.data.auth.ConfirmPhoneBody
import com.correct.correctsoc.data.auth.UpdatePhoneBody
import com.correct.correctsoc.data.auth.ValidateOTPBody
import com.correct.correctsoc.databinding.FragmentOTPBinding
import com.correct.correctsoc.helper.Constants.ISRECONFIRM
import com.correct.correctsoc.helper.Constants.PHONE
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TAG
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.VerificationTextFilledListener
import com.correct.correctsoc.helper.hide
import com.correct.correctsoc.helper.upperCaseOnly
import com.correct.correctsoc.helper.show
import com.correct.correctsoc.room.User
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.launch

class OTPFragment : Fragment(), VerificationTextFilledListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentOTPBinding
    private lateinit var verification: String
    private var source = -1
    private var phone = ""
    private lateinit var helper: HelperClass

    //private var smsReceiver: SmsReceiver? = null
    private var countDownTimer: CountDownTimer? = null
    private lateinit var usersDB: UsersDB
    private lateinit var viewModel: AuthViewModel
    private lateinit var fragmentListener: FragmentChangedListener
    private var isReconfirmed = false

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
        binding = FragmentOTPBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        fragmentListener.onFragmentChangedListener(R.id.OTPFragment)

        binding.placeholder.hide()
        binding.progress.hide()

        if (arguments != null) {
            source = requireArguments().getInt(SOURCE)
            phone = requireArguments().getString(PHONE) ?: ""
            if (source == R.id.loginFragment) {
                isReconfirmed = requireArguments().getBoolean(ISRECONFIRM)
            }
        }

        binding.txtFirstDigit.requestFocus()
        val array = arrayOf(
            binding.txtFirstDigit,
            binding.txtSecondDigit,
            binding.txtThirdDigit,
            binding.txtFourthDigit,
            binding.txtFifthDigit,
            binding.txtSixthDigit
        )
        changeFocus(array, this)

        binding.apply {
            txtFirstDigit.upperCaseOnly()
            txtSecondDigit.upperCaseOnly()
            txtThirdDigit.upperCaseOnly()
            txtFourthDigit.upperCaseOnly()
            txtFifthDigit.upperCaseOnly()
            txtSixthDigit.upperCaseOnly()
        }

        getUserPhone()

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        binding.btnBack.setOnClickListener {
            if (source != -1) {
                findNavController().navigate(source)
            } else {
                findNavController().navigate(R.id.signUpFragment)
            }
        }

        helper.onBackPressed(this) {
            if (source != -1) {
                findNavController().navigate(source)
            } else {
                findNavController().navigate(R.id.signUpFragment)
            }
        }

        startCountDownTimer()

        //startCountDownTimer()

        binding.btnResend.setOnClickListener {
            countDownTimer?.cancel()
            if (arguments != null) {
                val phone = requireArguments().getString(PHONE) ?: ""
                if (phone.isNotEmpty()) {
                    resendOTP(phone)
                } else {
                    lifecycleScope.launch {
                        binding.placeholder.show()
                        binding.progress.show()
                        val id = usersDB.dao().getUserID() ?: ""
                        val phone = usersDB.dao().getUserPhone(id) ?: ""
                        resendOTP(phone)
                    }
                }
            } else {
                lifecycleScope.launch {
                    binding.placeholder.show()
                    binding.progress.show()
                    val id = usersDB.dao().getUserID() ?: ""
                    val phone = usersDB.dao().getUserPhone(id) ?: ""
                    resendOTP(phone)
                }
            }

            startCountDownTimer()
        }

        return binding.root
    }

    private fun getUserPhone() {
        if (phone.isNotEmpty() && !phone.equals("null")) {
            binding.txtMsg.append(" $phone")
        } else {
            lifecycleScope.launch {
                val id = usersDB.dao().getUserID() ?: ""
                val phone = if (id.isNotEmpty()) {
                    usersDB.dao().getUserPhone(id) ?: ""
                } else {
                    usersDB.dao().getUserPhone("1")
                }
                binding.txtMsg.append(" $phone")
            }
        }
    }

    private fun confirmOTP(body: ConfirmPhoneBody) {
        viewModel.confirmOTP(body)
        viewModel.otpResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                binding.placeholder.hide()
                binding.progress.hide()
                if (it.result != null) {
                    lifecycleScope.launch {
                        Log.v(TAG, it.result.userid)
                        Log.v(TAG, it.result.name)
                        Log.v(TAG, it.result.token)
                        val password = usersDB.dao().getPassword("1") ?: ""
                        val phone = usersDB.dao().getUserPhone("1") ?: ""
                        val user = User(
                            it.result.userid, it.result.name,
                            password, phone, it.result.token
                        )
                        usersDB.dao().insert(user)
                        usersDB.dao().deleteUser("1")
                        helper.setToken(it.result.token, requireContext())
                        helper.setDeviceConfirmed(requireContext(),true)
                        Log.i(TAG, it.result.token)
                        findNavController().navigate(R.id.homeFragment)
                    }
                }
            } else {
                binding.placeholder.hide()
                binding.progress.hide()
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resendOTP(phone: String) {
        viewModel.resendOTP(phone)
        viewModel.otpResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                binding.placeholder.hide()
                binding.progress.hide()
                if (it.result != null) {
                    lifecycleScope.launch {
                        Log.v(TAG, it.result.userid)
                        Log.v(TAG, it.result.name)
                        Log.v(TAG, it.result.token)
                        val password = usersDB.dao().getPassword("1") ?: ""
                        val phone = usersDB.dao().getUserPhone("1") ?: ""
                        val user = User(
                            it.result.userid, it.result.name,
                            password, phone, it.result.token
                        )
                        helper.setToken(it.result.token, requireContext())
                        usersDB.dao().insert(user)
                        usersDB.dao().deleteUser("1")
                        findNavController().navigate(R.id.homeFragment)
                    }
                }
            } else {
                binding.placeholder.hide()
                binding.progress.hide()
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCountDownTimer() {
        binding.txtTime.show()
        binding.txtExpires.text = resources.getString(R.string.verification_expires)
        // 3 minute valid
        countDownTimer = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.txtTime.text = reformatMillis(millisUntilFinished)
            }

            override fun onFinish() {
                //binding.txtTime.text = " 00:00"
                binding.txtTime.hide()
                binding.txtExpires.text = resources.getString(R.string.code_expired)
                Log.i("Timer finished mohamed", "onFinish:")
            }

        }.start()
    }

    private fun reformatMillis(milliseconds: Long): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        val minute = if ("$minutes".length == 1) {
            "0$minutes"
        } else {
            "$minutes"
        }
        val second = if ("$seconds".length == 1) {
            "0$seconds"
        } else {
            "$seconds"
        }
        return " $minute:$second"
    }

    override fun onDetach() {
        super.onDetach()
        countDownTimer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        //requireActivity().unregisterReceiver(smsReceiver)
    }

    private fun changeFocus(editTexts: Array<EditText>, listener: VerificationTextFilledListener) {
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    if (after < count && i > 0) {
                        editTexts[i - 1].requestFocus()
                    }
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    //editTexts[i].upperCaseOnly()
                    if (s.toString().length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    }
                }
            })
            editTexts[i].setOnPasteListener {
                val pastedText = it.toString()
                if (pastedText.length == editTexts.size) {
                    for (j in editTexts.indices) {
                        editTexts[j].setText(pastedText[j].toString())
                    }
                }
                editTexts[editTexts.size - 1].requestFocus()
            }

            editTexts[i].setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editTexts[i].text.isEmpty()) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    } else {
                        editTexts[i].setText("")
                    }
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
        }

        editTexts[editTexts.size - 1].addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    Log.v("Verification mohamed", s.toString())
                    var isAnyEmpty = false
                    for (i in editTexts.indices) {
                        if (editTexts[i].text.isEmpty()) {
                            isAnyEmpty = true
                            break
                        } else {
                            isAnyEmpty = false
                        }
                    }
                    if (!isAnyEmpty) {
                        listener.onVerificationTextFilled()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun EditText.setOnPasteListener(callback: (CharSequence) -> Unit) {
        this.setOnCreateContextMenuListener { _, _, _ ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val item = clipboard.primaryClip?.getItemAt(0)
                item?.text?.let { callback(it) }
            }
        }
    }

    override fun onVerificationTextFilled() {
        verification = "${binding.txtFirstDigit.text}" +
                "${binding.txtSecondDigit.text}" +
                "${binding.txtThirdDigit.text}" +
                "${binding.txtFourthDigit.text}" +
                "${binding.txtFifthDigit.text}" +
                "${binding.txtSixthDigit.text}"

        // send verification code to server
        Log.i("Verification mohamed", "onCreateView: $verification")
        lifecycleScope.launch {
            val id = usersDB.dao().getUserID() ?: ""
            val phone = if (arguments != null) {
                requireArguments().getString(PHONE) ?: usersDB.dao().getUserPhone(id) ?: ""
            } else {
                usersDB.dao().getUserPhone(id) ?: ""
            }
            binding.placeholder.show()
            binding.progress.show()
            when (source) {
                R.id.loginFragment -> {
                    isReconfirmed = requireArguments().getBoolean(ISRECONFIRM)
                    // Log.v("okhttp.OkHttpClient","http://correctsocandroidapi.somee.com/api/Authentication/ $isReconfirmed")
                    // forgot password clicked or reconfirming phone
                    if (isReconfirmed) {
                        val body = ConfirmPhoneBody(phone,verification)
                        reConform(body)
                    } else {
                        if (phone.isNotEmpty()) {
                            val body = ValidateOTPBody(verification, phone)
                            validateOTP(body)
                        } else {
                            Toast.makeText(
                                requireContext(), resources.getString(R.string.phone_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                R.id.signUpFragment -> {
                    // register account clicked
                    confirmOTP(ConfirmPhoneBody(phone, verification))
                }

                R.id.editInfoFragment -> {
                    // update phone clicked
                    lifecycleScope.launch {
                        val id = usersDB.dao().getUserID() ?: ""
                        val phone = usersDB.dao().getUserPhone(id) ?: ""
                        val body = UpdatePhoneBody(
                            userId = id, newPhone = phone,
                            otp = verification
                        )
                        updatePhone(body, helper.getToken(requireContext()))
                    }
                }
            }
        }
    }

    private fun reConform(body: ConfirmPhoneBody) {
        viewModel.confirmOTP(body)
        val observer = object : Observer<AuthResponse> {
            override fun onChanged(value: AuthResponse) {
                if (value.isSuccess) {
                    findNavController().navigate(R.id.loginFragment)
                }
                viewModel.otpResponse.removeObserver(this)
            }
        }
        viewModel.otpResponse.observe(viewLifecycleOwner,observer)
    }

    private fun validateOTP(body: ValidateOTPBody) {
        viewModel.validateOTP(body)
        val observer = object : Observer<AuthResponse> {
            override fun onChanged(value: AuthResponse) {
                if (value.isSuccess) {
                    binding.placeholder.hide()
                    binding.progress.hide()

                    val token = requireArguments().getString(TOKEN_KEY, "") ?: ""
                    val bundle = Bundle()
                    bundle.putString(TOKEN_KEY, token)
                    findNavController().navigate(R.id.resetPasswordFragment, bundle)

                } else {
                    binding.placeholder.hide()
                    binding.progress.hide()
                    Toast.makeText(requireContext(), value.errorMessages, Toast.LENGTH_SHORT)
                        .show()
                }
                viewModel.validateOTPResponse.removeObserver(this)
            }
        }
        viewModel.validateOTPResponse.observe(viewLifecycleOwner, observer)
    }

    private fun updatePhone(body: UpdatePhoneBody, token: String) {
        viewModel.updatePhone(body, token)
        viewModel.updatePhoneResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.phone_updated),
                    Toast.LENGTH_SHORT
                )
                    .show()
                findNavController().navigate(R.id.homeFragment)
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.OTPFragment)
    }
}