package com.correct.correctsoc.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.room.UsersDB
import com.correct.correctsoc.databinding.FragmentSplashBinding
import com.correct.correctsoc.helper.HelperClass
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentSplashBinding
    private lateinit var helper: HelperClass
    private var milliFinished: Long = 0
    private var countDownTimer: CountDownTimer? = null
    private val TAG = "SplashFragment mohamed"
    private lateinit var usersDB: UsersDB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())

        binding.txtVersion.append(" ${helper.getAppVersion(requireContext())}")

        helper.onBackPressed(this) {
            requireActivity().finish()
        }

        startSplash()

        return binding.root
    }

    private fun startSplash() {
        countDownTimer = object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                milliFinished = millisUntilFinished
                Log.i(TAG, "onTick: millisUntilFinished=$millisUntilFinished")
            }

            override fun onFinish() {
                // navigate to onBoarding
                Log.i(TAG, "onFinish: ${milliFinished}")
                if (helper.isFirstStartApp(requireContext())) {
                    findNavController().navigate(R.id.onBoardingFragment)
                    helper.setFirstStartApp(false, requireContext())
                } else {
                    checkForUsers()
                }
            }

        }.start()
    }

    private fun checkForUsers() {
        lifecycleScope.launch {
            val id = usersDB.dao().getUserID() ?: ""
            // users found
            if (id.isNotEmpty()) {
                findNavController().navigate(R.id.homeFragment)
            }
            // no user found
            else {
                findNavController().navigate(R.id.registerFragment)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        countDownTimer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }

    /*override fun onResume() {
        super.onResume()
        val millis = helper.getSplashTime(requireContext())
        if (millis in 1..<duration) {
            val remain = duration - millis
            startSplash(remain)
        }
    }

    override fun onPause() {
        super.onPause()
        helper.setSplashTime(milliFinished,requireContext())
    }*/
}