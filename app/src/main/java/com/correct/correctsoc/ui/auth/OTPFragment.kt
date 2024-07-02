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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentOTPBinding
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.VerificationTextFilledListener
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.launch

class OTPFragment : Fragment(), VerificationTextFilledListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentOTPBinding
    private lateinit var verification: String
    private var source = -1
    private lateinit var helper: HelperClass

    //private var smsReceiver: SmsReceiver? = null
    private var countDownTimer: CountDownTimer? = null
    private lateinit var usersDB: UsersDB


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOTPBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())

        if (arguments != null) {
            source = requireArguments().getInt(SOURCE)
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

        getUserPhone()

        binding.btnResend.setOnClickListener {
            countDownTimer?.cancel()
            startCountDownTimer()
        }

        //startSmartUserConsent()

        return binding.root
    }

    private fun getUserPhone() {
        lifecycleScope.launch {
            val phone = usersDB.dao().getUserPhone() ?: "+201066168221"
            binding.txtMsg.append(" $phone")
        }
    }

    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.txtTime.text = reformatMillis(millisUntilFinished)
                //Log.i(TAG, "onTick: millisUntilFinished=$millisUntilFinished")
            }

            override fun onFinish() {
                //binding.txtTime.text = " 00:00"
                binding.txtTime.visibility = View.GONE
                binding.txtExpires.text = resources.getString(R.string.code_expired)
                Log.i("Timer finished mohamed", "onFinish:")
                /*if (helper.isFirstStartApp(requireContext())) {
                    findNavController().navigate(R.id.onBoardingFragment)
                    helper.setFirstStartApp(false, requireContext())
                } else {
                    checkForUsers()
                }*/
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

    }

    /*override fun onStart() {
        super.onStart()
        registerReceiver()
    }
    private val arl: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            val message = it.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            getOTPFromSms(message)
        }
    }

    private fun getOTPFromSms(message: String?) {
        val otpPattern = Pattern.compile("(|^)\\d{6}")
        val matcher = otpPattern.matcher(message)
        if (matcher.find()) {
            // otp fetched from sms
            val otp = matcher.group(0)
        }
    }
    private fun startSmartUserConsent() {
        val client = SmsRetriever.getClient(requireContext())
        client.startSmsUserConsent(null)
    }

    private fun registerReceiver() {
        smsReceiver = SmsReceiver()
        smsReceiver!!.listener = object : SmsReceiver.SmsReceiverListener {
            override fun onSuccess(intent: Intent?) {
                arl.launch(intent)
            }

            override fun onFailure() {

            }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        requireActivity().registerReceiver(smsReceiver, intentFilter)
    }

    private fun changeFocus(
        previousTxt: EditText,
        currentTxt: EditText,
        nextText: EditText,
        functionToExecute: () -> Unit = {}
    ) {
        currentTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // user deleted text
                if (after < count) {
                    previousTxt.requestFocus()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val string = s.toString()
                if (string.length == 1) {
                    nextText.requestFocus()
                }
            }
        })

        // last editText
        if (nextText == currentTxt) {
            nextText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().length == 1) {
                        functionToExecute()
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

        currentTxt.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (currentTxt.text.isEmpty()) {
                    previousTxt.requestFocus()
                } else {
                    currentTxt.setText("")
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    private fun onBackPressed() {
        (activity as AppCompatActivity).supportFragmentManager
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (source != -1) {
                        findNavController().navigate(source)
                    } else {
                        findNavController().navigate(R.id.signUpFragment)
                    }
                }
            })
    }*/
}