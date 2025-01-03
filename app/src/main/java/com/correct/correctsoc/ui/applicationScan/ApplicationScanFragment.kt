package com.correct.correctsoc.ui.applicationScan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentApplicationScanBinding
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.circularAnimation

class ApplicationScanFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentApplicationScanBinding
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
        binding = FragmentApplicationScanBinding.inflate(inflater,container,false)
        helper = HelperClass.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.applicationScanFragment)
        binding.circularImage.startAnimation(circularAnimation(3000))

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnScan.setOnClickListener {
            findNavController().navigate(R.id.scanningFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        binding.circularImage.clearAnimation()
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.applicationScanFragment)
        binding.circularImage.startAnimation(circularAnimation(3000))
    }

}