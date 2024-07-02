package com.correct.correctsoc.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentResetPasswordBinding
import com.correct.correctsoc.helper.HelperClass

class ResetPasswordFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentResetPasswordBinding
    private lateinit var helper: HelperClass
    private var startIndx = 0
    private var endIndx = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        binding.txtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length >= 6) {
                    binding.iconCheck.setImageResource(R.drawable.check_circle_icon)
                } else {
                    binding.iconCheck.setImageResource(R.drawable.dot_icon)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.OTPFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.OTPFragment)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            startIndx = 23
            binding.btnBack.rotation = 180f
        } else {
            startIndx = 28
        }
        endIndx = resources.getString(R.string.password_requirement).length

        binding.txtPasswordRequirements.text = helper.colorfulTextView(
            startIndx, endIndx, resources.getColor(R.color.white, context?.theme),
            resources.getString(R.string.password_requirement)
        )


        return binding.root
    }

}