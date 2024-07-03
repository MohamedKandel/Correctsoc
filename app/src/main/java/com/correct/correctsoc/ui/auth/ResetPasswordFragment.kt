package com.correct.correctsoc.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentResetPasswordBinding
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.isContainsSpecialCharacter
import com.correct.correctsoc.helper.isContainsNumbers

class ResetPasswordFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentResetPasswordBinding
    private lateinit var helper: HelperClass
    private var isLengthChecked = false
    private var isUpperChecked = false
    private var isNumberChecked = false
    private var achieved = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        //binding.strengthLayout.visibility = View.GONE

        binding.txtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                //binding.strengthLayout.visibility = View.VISIBLE
                if (s.toString().isContainsNumbers() && !isNumberChecked) {
                    Log.v("Password instructions", "number")
                    achieved += 1
                    isNumberChecked = true
                    binding.icon1Number.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.safe_color
                        ), android.graphics.PorterDuff.Mode.MULTIPLY
                    )
                } else if (!s.toString().isContainsNumbers() && isNumberChecked) {
                    isNumberChecked = false
                    if (achieved > 0) {
                        achieved -= 1
                    }
                    binding.icon1Number.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.alphaGray
                        ), android.graphics.PorterDuff.Mode.MULTIPLY
                    )
                }
                if (s.toString().isContainsSpecialCharacter() && !isUpperChecked) {
                    achieved += 1
                    isUpperChecked = true
                    binding.icon1Upper.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.safe_color
                        ), android.graphics.PorterDuff.Mode.MULTIPLY
                    )
                } else if (!s.toString().isContainsSpecialCharacter() && isUpperChecked) {
                    isUpperChecked = false
                    if (achieved > 0) {
                        achieved -= 1
                    }
                    binding.icon1Upper.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.alphaGray
                        ), android.graphics.PorterDuff.Mode.MULTIPLY
                    )
                }
                if (s.toString().length >= 6 && !isLengthChecked) {
                    achieved += 1
                    isLengthChecked = true
                    binding.icon6Check.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.safe_color
                        ), android.graphics.PorterDuff.Mode.MULTIPLY
                    )
                } else if (s.toString().length < 6 && isLengthChecked) {
                    isLengthChecked = false
                    if (achieved > 0) {
                        achieved -= 1
                    }
                    binding.icon6Check.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.alphaGray
                        ), android.graphics.PorterDuff.Mode.MULTIPLY
                    )
                }

                Log.v("Password instructions", "$achieved")

                checkSteps(achieved)
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
            binding.btnBack.rotation = 180f
        }



        return binding.root
    }

    private fun checkSteps(achieved: Int) {
        when (achieved) {
            0 -> {
                binding.view1.setCardBackgroundColor(
                    resources.getColor(
                        R.color.alphaWhite,
                        context?.theme
                    )
                )
                binding.view2.setCardBackgroundColor(
                    resources.getColor(
                        R.color.alphaWhite,
                        context?.theme
                    )
                )
                binding.view3.setCardBackgroundColor(
                    resources.getColor(
                        R.color.alphaWhite,
                        context?.theme
                    )
                )
            }

            1 -> {
                binding.view1.setCardBackgroundColor(
                    resources.getColor(
                        R.color.danger_color,
                        context?.theme
                    )
                )
                binding.view2.setCardBackgroundColor(
                    resources.getColor(
                        R.color.alphaWhite,
                        context?.theme
                    )
                )
                binding.view3.setCardBackgroundColor(
                    resources.getColor(
                        R.color.alphaWhite,
                        context?.theme
                    )
                )
            }

            2 -> {
                binding.view1.setCardBackgroundColor(
                    resources.getColor(
                        R.color.warning_color,
                        context?.theme
                    )
                )
                binding.view2.setCardBackgroundColor(
                    resources.getColor(
                        R.color.warning_color,
                        context?.theme
                    )
                )
                binding.view3.setCardBackgroundColor(
                    resources.getColor(
                        R.color.alphaWhite,
                        context?.theme
                    )
                )
            }

            3 -> {
                binding.view1.setCardBackgroundColor(
                    resources.getColor(
                        R.color.safe_color,
                        context?.theme
                    )
                )
                binding.view2.setCardBackgroundColor(
                    resources.getColor(
                        R.color.safe_color,
                        context?.theme
                    )
                )
                binding.view3.setCardBackgroundColor(
                    resources.getColor(
                        R.color.safe_color,
                        context?.theme
                    )
                )
            }
        }
    }

}