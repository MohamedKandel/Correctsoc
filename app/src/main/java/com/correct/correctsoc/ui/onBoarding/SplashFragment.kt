package com.correct.correctsoc.ui.onBoarding

import android.content.Context
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
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.mappingNumbers
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
    private lateinit var fragmentListener: FragmentChangedListener
    //private lateinit var viewModel: AuthViewModel

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
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())

        //viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.root.keepScreenOn = true

        val version = if (helper.getLang(requireContext()).equals("ar")) {
            helper.getAppVersion(requireContext()).mappingNumbers()
        } else {
            helper.getAppVersion(requireContext())
        }
        fragmentListener.onFragmentChangedListener(R.id.splashFragment)
        binding.txtVersion.append(" $version")

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
                } else {
                    if (helper.getRemember(requireContext())) {
                        findNavController().navigate(R.id.homeFragment)
                    } else {
                        findNavController().navigate(R.id.registerFragment)
                    }
                    //checkForUsers()
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
}