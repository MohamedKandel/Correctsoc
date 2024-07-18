package com.correct.correctsoc.ui.pay

import android.content.Context
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
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.NextStepListener

class ParentPayFragment : Fragment(), NextStepListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentParentPayBinding
    private lateinit var helper: HelperClass
    private lateinit var currentFragment: Fragment
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
        binding = FragmentParentPayBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        fragmentListener.onFragmentChangedListener(R.id.parentPayFragment)
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
        if (childFragmentManager.backStackEntryCount > 1) {
            childFragmentManager.popBackStack()
            return
        }
        val parentPayFragment = currentFragment.parentFragment as ParentPayFragment
        when (currentFragment) {
            is PaymentMethodFragment -> {
                changeSteps(1)
            }

            is ActivationCodeFragment -> {
                changeSteps(2)
            }

            is PaymentSuccessFragment -> {
                changeSteps(2)
            }
        }
        parentPayFragment.changeSteps(2)
        parentFragmentManager.popBackStack()
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
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun displayHeader(isHeaderVisible: Boolean) {
        if (isHeaderVisible) {
            binding.layoutStepper.visibility = View.VISIBLE
            binding.txtTitle.visibility = View.GONE
        } else {
            binding.layoutStepper.visibility = View.INVISIBLE
            binding.txtTitle.visibility = View.VISIBLE
        }
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

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.parentPayFragment)
    }
}