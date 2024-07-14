package com.correct.correctsoc.ui.selfScan

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentSelfPenBinding
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass

class SelfPenFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentSelfPenBinding
    private lateinit var helper: HelperClass
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
        binding = FragmentSelfPenBinding.inflate(inflater,container,false)
        helper = HelperClass.getInstance()
        fragmentListener.onFragmentChangedListener(R.id.selfPenFragment)

        //Start animation
        binding.progressCircular.startAnimation(helper.circularAnimation(3000))

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.progressLayout.setOnClickListener {
            // start scan
            findNavController().navigate(R.id.detectingFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.selfPenFragment)
    }
}