package com.correct.correctsoc.ui.pay

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentParentPayBinding
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.NextStepListener

class ParentPayFragment : Fragment(), NextStepListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentParentPayBinding
    private lateinit var helper: HelperClass
    private lateinit var currentFragment: Fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentParentPayBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        changeSteps(1)
        replaceFragment(PaymentDetailsFragment())

        helper.onBackPressed(this) {
            back()
        }

        binding.btnBack.setOnClickListener {
            back()
        }

        return binding.root
    }

    private fun back() {
        if (currentFragment is PaymentDetailsFragment) {
            findNavController().navigate(R.id.homeFragment)
        } else if (currentFragment is PaymentMethodFragment) {
            replaceFragment(PaymentDetailsFragment())
            changeSteps(1)
        } else if (currentFragment is PaymentSuccessFragment) {
            replaceFragment(PaymentMethodFragment())
            changeSteps(2)
        }
    }

    fun replaceFragment(
        fragment: Fragment,
        bundle: Bundle? = null,
        isHeaderVisible: Boolean = true
    ) {
        if (isHeaderVisible) {
            binding.layoutStepper.visibility = View.VISIBLE
            binding.txtTitle.visibility = View.GONE
        } else {
            binding.layoutStepper.visibility = View.INVISIBLE
            binding.txtTitle.visibility = View.VISIBLE
        }
        currentFragment = fragment
        if (bundle != null) {
            fragment.arguments = bundle
        }
        if (currentFragment is PaymentMethodFragment) {
            Log.v("current mohamed", "payment method")
        } else if (currentFragment is PaymentDetailsFragment) {
            Log.v("current mohamed", "payment details")
        } else {
            Log.v("current mohamed", "payment success")
        }
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun changeSteps(index: Int) {
        when (index) {
            1 -> {
                binding.apply {
                    icon1.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    icon2.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.alphaWhite
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    icon3.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.alphaWhite
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    view1.setBackgroundColor(resources.getColor(R.color.alphaWhite, context?.theme))
                    view2.setBackgroundColor(resources.getColor(R.color.alphaWhite, context?.theme))
                }
            }

            2 -> {
                binding.apply {
                    icon1.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    icon2.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    icon3.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.alphaWhite
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    view1.setBackgroundColor(resources.getColor(R.color.white, context?.theme))
                    view2.setBackgroundColor(resources.getColor(R.color.alphaWhite, context?.theme))
                }
            }

            3 -> {
                binding.apply {
                    icon1.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    icon2.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    icon3.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    view1.setBackgroundColor(resources.getColor(R.color.white, context?.theme))
                    view2.setBackgroundColor(resources.getColor(R.color.white, context?.theme))
                }
            }
        }
    }

    override fun onNextStepListener(step: Int) {
        Log.v("Step mohamed", "$step")
        when (step) {
            1 -> {
                changeSteps(2)
            }

            2 -> {
                changeSteps(3)
            }
        }
    }
}