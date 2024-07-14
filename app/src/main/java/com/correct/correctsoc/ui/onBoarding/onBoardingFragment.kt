package com.correct.correctsoc.ui.onBoarding

import ViewPagerAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentOnBoardingBinding
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass


class onBoardingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentOnBoardingBinding
    private var pageNumber = 0
    private lateinit var helper:HelperClass
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
        binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        helper.deleteSplashTime(requireContext())

        fragmentListener.onFragmentChangedListener(R.id.onBoardingFragment)

        helper.onBackPressed(this) {
            requireActivity().finish()
        }

        val fragmentList = arrayListOf(
            FirstFragment(),
            SecondFragment(),
            ThirdFragment(),
            FourthFragment()
        )

        val adapter = ViewPagerAdapter(
            requireActivity().supportFragmentManager,
            lifecycle, fragmentList
        )

        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = pageNumber
        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.nextBtn.setOnClickListener {
            if (pageNumber <= 2) {
                pageNumber++
                binding.viewPager.currentItem = pageNumber
            } else {
                helper.setFirstStartApp(false, requireContext())
                findNavController().navigate(R.id.langFragment)
            }
            Log.v("Page number", "$pageNumber")
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.d("page number","$position")
                pageNumber = position
            }
        })

        return binding.root
    }
}