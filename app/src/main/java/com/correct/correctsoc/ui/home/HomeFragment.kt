package com.correct.correctsoc.ui.home

import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.MenuAdapter
import com.correct.correctsoc.data.MenuData
import com.correct.correctsoc.databinding.FragmentHomeBinding
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TYPE
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.OnSwipeGestureListener
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode


class HomeFragment : Fragment(), ClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var helper: HelperClass
    private lateinit var adapter: MenuAdapter
    private lateinit var list: MutableList<MenuData>
    private val TAG = "HomeFragment mohamed"
    private var isVisible = false
    private var isDialogVisible = false
    private lateinit var slideIn: Animation
    private lateinit var slideOut: Animation
    private lateinit var fadeIn: Animation
    private lateinit var fadeOut: Animation
    private lateinit var gestureDetector: GestureDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        list = mutableListOf()
        adapter = MenuAdapter(requireContext(), list, this)

        binding.drawerMenu.recyclerView.adapter = adapter

        fillList()


        fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        binding.btnScan.setOnClickListener {
            binding.placeholder.visibility = View.VISIBLE
            binding.dialog.root.visibility = View.VISIBLE
            isDialogVisible = true
            binding.dialog.root.startAnimation(fadeIn)

            val btn_pen_scan = binding.dialog.root.findViewById<RelativeLayout>(R.id.btn_self_scan)
            val btn_ip_scan = binding.dialog.root.findViewById<RelativeLayout>(R.id.btn_ip_scan)

            btn_pen_scan.setOnClickListener {
                findNavController().navigate(R.id.selfPenFragment)
                isDialogVisible = false
                binding.placeholder.visibility = View.GONE
                binding.dialog.root.startAnimation(fadeOut)
                binding.dialog.root.visibility = View.GONE
            }

            btn_ip_scan.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(TYPE, IP_ADDRESS)
                findNavController().navigate(R.id.insertLinkFragment, bundle)
                isDialogVisible = false
                binding.placeholder.visibility = View.GONE
                binding.dialog.root.startAnimation(fadeOut)
                binding.dialog.root.visibility = View.GONE
            }


            //dialog.show()
        }

        binding.btnAppScan.setOnClickListener {
            findNavController().navigate(R.id.applicationScanFragment)
        }

        binding.btnIpScan.setOnClickListener {
            findNavController().navigate(R.id.deviceScanningFragment)
        }

        binding.btnWebScan.setOnClickListener {
            findNavController().navigate(R.id.insertLinkFragment)
        }

        binding.txtVersion.append(" ${helper.getAppVersion(requireContext())}")

        helper.onBackPressed(this) {
            requireActivity().finish()
        }

        if (helper.getLang(requireContext()).equals("en")) {
            slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_en)
            slideOut = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_en)
        } else {
            slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_ar)
            slideOut = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_ar)
        }
        gestureDetector =
            GestureDetector(requireContext(), object : OnSwipeGestureListener() {
                override fun onSwipeRight() {
                    // Handle swipe right
                    Log.d(TAG, "onSwipeRight: right")
                    if (helper.getLang(requireContext()).equals("ar") && isVisible) {
                        closeMenu()
                    } else if (helper.getLang(requireContext()).equals("en") && !isVisible) {
                        openMenu()
                    }
//                    isVisible = !isVisible
                }

                override fun onSwipeLeft() {
                    // Handle swipe left
                    Log.d(TAG, "onSwipeLeft: left")
                    if (helper.getLang(requireContext()).equals("ar") && !isVisible) {
                        openMenu()
                    } else if (helper.getLang(requireContext()).equals("en") && isVisible) {
                        closeMenu()
                    }
                }
            })

        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        binding.menuIcon.setOnClickListener {
            if (isVisible) {
                closeMenu()
            } else {
                openMenu()
            }
        }

        binding.placeholder.setOnClickListener {
            if (isVisible) {
                closeMenu()
            } else if (isDialogVisible) {
                binding.placeholder.visibility = View.GONE
                binding.dialog.root.startAnimation(fadeOut)
                binding.dialog.root.visibility = View.GONE
                isDialogVisible = false
            }
        }

        return binding.root
    }

    /*private fun onBackPressed() {
        (activity as AppCompatActivity).supportFragmentManager
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Back is pressed... Finishing the activity
                    requireActivity().finish()
                }
            })
    }*/

    private fun openMenu() {
        binding.placeholder.visibility = View.VISIBLE
        binding.drawerMenu.root.visibility = View.VISIBLE
        binding.drawerMenu.root.startAnimation(slideIn)
        binding.drawerMenu.recyclerView.scrollToPosition(0)
        isVisible = true
    }

    private fun closeMenu() {
        binding.placeholder.visibility = View.GONE
        binding.drawerMenu.root.startAnimation(slideOut)
        binding.drawerMenu.root.visibility = View.GONE
        isVisible = false
    }

    private fun fillList() {
        list.add(MenuData(R.drawable.premium_icon, resources.getString(R.string.premium)))
        list.add(MenuData(R.drawable.support_icon, resources.getString(R.string.support)))
        list.add(MenuData(R.drawable.setting_icon, resources.getString(R.string.setting)))
        list.add(MenuData(R.drawable.reward_icon, resources.getString(R.string.reward)))
        list.add(MenuData(R.drawable.community_icon, resources.getString(R.string.community)))
        list.add(MenuData(R.drawable.rate_icon, resources.getString(R.string.rate)))
        list.add(MenuData(R.drawable.about_icon, resources.getString(R.string.about)))
        list.add(MenuData(R.drawable.logout_icon, resources.getString(R.string.logout)))
        list.add(
            MenuData(
                0,
                "${resources.getString(R.string.version)} : ${helper.getAppVersion(requireContext())}"
            )
        )

        adapter.updateAdapter(list)
    }


    override fun onItemClickListener(position: Int, extras: Bundle?) {
        when (position) {
            //premium
            0 -> {
                Log.i(TAG, "onItemClickListener: premium")
            }
            //support
            1 -> {
                Log.i(TAG, "onItemClickListener: support")
            }
            //setting
            2 -> {
                Log.i(TAG, "onItemClickListener: settings")
                findNavController().navigate(R.id.settingFragment)
            }
            //reward
            3 -> {
                Log.i(TAG, "onItemClickListener: reward")
            }
            //community
            4 -> {
                Log.i(TAG, "onItemClickListener: community")
            }
            //rate
            5 -> {
                Log.i(TAG, "onItemClickListener: rate")
                //Uncomment line below before publishing app on google play
                //rateApp()
            }
            //about
            6 -> {
                val bundle = Bundle()
                bundle.putInt(SOURCE, R.id.homeFragment)
                findNavController().navigate(R.id.aboutFragment, bundle)
                Log.i(TAG, "onItemClickListener: about")
            }
            //logout
            7 -> {
                Log.i(TAG, "onItemClickListener: logout")
            }
        }
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {

    }

    private fun rateApp() {
        val manager = ReviewManagerFactory.create(requireContext())
        manager.requestReviewFlow().addOnCompleteListener {
            if (it.isSuccessful) {
                manager.launchReviewFlow(requireActivity(), it.result)
            } else {
                @ReviewErrorCode val reviewErrorCode = (it.exception as ReviewException).errorCode
                Log.e(TAG, "rateApp: Error code $reviewErrorCode", it.exception)
            }
        }
    }
}