package com.correct.correctsoc.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentLoginBinding
import com.correct.correctsoc.helper.Constants
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.HelperClass

class LoginFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentLoginBinding
    private var isRemember = false
    private lateinit var helper: HelperClass
    private var startIndx = 0
    private var endIndx = 0
    private var source = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        if (arguments != null) {
            source = requireArguments().getInt(SOURCE)
        }

        binding.rememberLayout.setOnClickListener {
            if (!isRemember) {
                binding.rememberIcon.setImageResource(R.drawable.check_circle_icon)
            } else {
                binding.rememberIcon.setImageResource(R.drawable.dot_icon)
            }
            isRemember = !isRemember
        }

        if (helper.getLang(requireContext()).equals("en")) {
            startIndx = 21
            endIndx = 28
        } else {
            startIndx = 16
            endIndx = 28
        }
        val span = helper.setSpannable(
            startIndx,
            endIndx,
            resources.getString(R.string.new_user),
            resources.getColor(R.color.white, context?.theme)
        ) {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.loginFragment)
            findNavController().navigate(R.id.signUpFragment, bundle)
        }

        binding.txtSignup.text = span
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
            // send credentials to server here
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
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.loginFragment)
            findNavController().navigate(R.id.OTPFragment, bundle)
        }

        return binding.root
    }

    /*private fun onBackPressed() {
        (activity as AppCompatActivity).supportFragmentManager
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (source != -1) {
                        findNavController().navigate(source)
                    } else {
                        findNavController().navigate(R.id.askForRegisterFragment)
                    }
                }
            })
    }*/
}