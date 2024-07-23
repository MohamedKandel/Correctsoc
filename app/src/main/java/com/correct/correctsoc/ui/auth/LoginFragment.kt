package com.correct.correctsoc.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.LoginBody
import com.correct.correctsoc.databinding.FragmentLoginBinding
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import com.correct.correctsoc.helper.Constants.TOKEN_VALUE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.hide
import com.correct.correctsoc.helper.setSpannable
import com.correct.correctsoc.helper.show
import com.correct.correctsoc.room.User
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentLoginBinding
//    private var isRemember = false
    private lateinit var helper: HelperClass
    private var startIndx = 0
    private var endIndx = 0
    private var source = -1
    private lateinit var usersDB: UsersDB
    private lateinit var viewModel: AuthViewModel
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
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        usersDB = UsersDB.getDBInstance(requireContext())

        fragmentListener.onFragmentChangedListener(R.id.loginFragment)

        binding.progress.hide()
        binding.placeholder.hide()

        if (arguments != null) {
            source = requireArguments().getInt(SOURCE)
        }

        /*binding.rememberLayout.setOnClickListener {
            if (!isRemember) {
                binding.rememberIcon.setImageResource(R.drawable.check_circle_icon)
                helper.setRemember(requireContext(), true)
            } else {
                binding.rememberIcon.setImageResource(R.drawable.dot_icon)
                helper.setRemember(requireContext(), false)
            }
            isRemember = !isRemember
        }*/

        if (helper.getLang(requireContext()).equals("en")) {
            startIndx = 21
            endIndx = 28
        } else {
            startIndx = 16
            endIndx = 28
        }
        /*val span = helper.setSpannable(
            startIndx,
            endIndx,
            resources.getString(R.string.new_user),
            resources.getColor(R.color.white, context?.theme)
        ) {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.loginFragment)
            findNavController().navigate(R.id.signUpFragment, bundle)
        }

        binding.txtSignup.text = span*/
        binding.txtSignup.setSpannable(
            startIndx,
            endIndx,
            resources.getString(R.string.new_user),
            resources.getColor(R.color.white, context?.theme)
        ) {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.loginFragment)
            findNavController().navigate(R.id.signUpFragment, bundle)
        }
        binding.txtSignup.movementMethod = LinkMovementMethod.getInstance()

        // accept + in first of edit text only
        binding.txtPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    binding.txtPhone.keyListener = DigitsKeyListener.getInstance("0123456789+")
                } else {
                    if (s.toString().startsWith("+")) {
                        binding.txtPhone.keyListener = DigitsKeyListener.getInstance("0123456789")
                    } else {
                        binding.txtPhone.keyListener = DigitsKeyListener.getInstance("0123456789")
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.loginBtn.setOnClickListener {
            if (helper.isEmpty(binding.txtPassword, binding.txtPhone)) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.required_data),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // send credentials to server here
                binding.placeholder.show()
                binding.progress.show()
                val phone = binding.txtPhone.text.toString()
                val password = binding.txtPassword.text.toString()
                val body = LoginBody(
                    deviceId = helper.getDeviceID(requireContext()),
                    password = password,
                    phoneNumber = phone
                )

                login(body)
            }
        }

        helper.onBackPressed(this) {
            if (source != -1) {
                findNavController().navigate(source)
            } else {
                findNavController().navigate(R.id.registerFragment)
            }
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        binding.btnBack.setOnClickListener {
            if (source != -1) {
                findNavController().navigate(source)
            } else {
                findNavController().navigate(R.id.registerFragment)
            }
        }

        binding.txtForget.setOnClickListener {
            binding.progress.show()
            binding.placeholder.show()
            val userPhone = binding.txtPhone.text.toString()
            lifecycleScope.launch {
                val id = usersDB.dao().getUserID() ?: ""
                val phone = usersDB.dao().getUserPhone(id) ?: userPhone
                if (phone.isNotEmpty()) {
                    forgotPassword(phone)
                } else {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.phone_empty),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

    private fun login(body: LoginBody) {
        viewModel.login(body)
        viewModel.loginResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess && it.result != null) {
                binding.progress.hide()
                binding.placeholder.hide()
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    if (id.isEmpty()) {
                        val user = User(
                            it.result.userid,
                            it.result.name,
                            body.password,
                            body.phoneNumber,
                            "$TOKEN_VALUE ${it.result.token}"
                        )
                        usersDB.dao().insert(user)
                    } else {
                        usersDB.dao().updateToken("$TOKEN_VALUE ${it.result.token}", it.result.userid)
                        helper.setToken(it.result.token, requireContext())
                        usersDB.dao().updateUsername(it.result.name, it.result.userid)
                    }
                    helper.setRemember(requireContext(),true)
                    findNavController().navigate(R.id.homeFragment)
                }
            } else {
                binding.progress.hide()
                binding.placeholder.hide()
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun forgotPassword(phone: String) {
        viewModel.forgetPassword(phone)
        viewModel.forgetResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                binding.progress.hide()
                binding.placeholder.hide()
                val bundle = Bundle()
                if (it.result != null) {
                    //bundle.putString(CODE, it.result.otp)
                    bundle.putString(TOKEN_KEY, it.result)
                    bundle.putInt(SOURCE, R.id.loginFragment)
                    findNavController().navigate(R.id.OTPFragment, bundle)
                }
            } else {
                binding.progress.hide()
                binding.placeholder.hide()
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.loginFragment)
    }
}