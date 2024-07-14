package com.correct.correctsoc.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.MenuAdapter
import com.correct.correctsoc.data.MenuData
import com.correct.correctsoc.data.auth.SignOutBody
import com.correct.correctsoc.databinding.FragmentHomeBinding
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TYPE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.OnSwipeGestureListener
import com.correct.correctsoc.helper.buildDialog
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.room.UsersDB
import com.correct.correctsoc.ui.auth.AuthViewModel
import kotlinx.coroutines.launch


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
    private lateinit var usersDB: UsersDB
    private lateinit var viewModel: AuthViewModel
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
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        fragmentListener.onFragmentChangedListener(R.id.homeFragment)

        list = mutableListOf()
        adapter = MenuAdapter(requireContext(), list, this)

        binding.drawerMenu.recyclerView.adapter = adapter

        fillList()

        lifecycleScope.launch {
            val id = usersDB.dao().getUserID() ?: ""
            val user = usersDB.dao().getUser(id)
            if (user != null) {
                Log.v(TAG, user.id)
                Log.v(TAG, user.phone)
                Log.v(TAG, user.token)
                Log.v(TAG, user.password)
                Log.v(TAG, user.username)
            }
        }

        /*if (!helper.getDeviceStatus(requireContext())) {
            setDeviceOn(helper.getToken(requireContext()))
        }*/

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
        }

        binding.btnAppScan.setOnClickListener {
            validateToken(helper.getToken(requireContext()), R.id.applicationScanFragment)
        }

        binding.btnIpScan.setOnClickListener {
            validateToken(helper.getToken(requireContext()), R.id.deviceScanningFragment)
        }

        binding.btnWebScan.setOnClickListener {
            findNavController().navigate(R.id.insertLinkFragment)
        }

        val version = if (helper.getLang(requireContext()).equals("ar")) {
            helper.getAppVersion(requireContext()).mappingNumbers()
        } else {
            helper.getAppVersion(requireContext())
        }

        binding.txtVersion.append(" $version")

        helper.onBackPressed(this) {
            if (isDialogVisible) {
                isDialogVisible = false
                binding.placeholder.visibility = View.GONE
                binding.dialog.root.startAnimation(fadeOut)
                binding.dialog.root.visibility = View.GONE
            } else if (isVisible) {
                closeMenu()
            } else {
                requireActivity().finish()
            }
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

        if (helper.isUnknownSourcesEnabled(requireContext())) {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Warning")
            dialog.setIcon(R.drawable.warning)
            dialog.setMessage("Unknown sources is enabled, for your security you can disable it")
            dialog.setPositiveButton("ok") { dialog, which ->
                dialog.dismiss()
                dialog.cancel()
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("cancel") { dialog, which ->
                dialog.dismiss()
                dialog.cancel()
            }
            dialog.setCancelable(false)
            dialog.show()
        }

        return binding.root
    }

    private fun setDeviceOn(token: String) {
        viewModel.setDeviceOn(token)
        val observer = object : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                if (value) {
                    helper.setDeviceOnline(true, requireContext())
                    Log.v("device status", "account online")
                } else {
                    Log.v("device status", "account failed to be online")
                }
                viewModel.changeDeviceStatus.removeObserver(this)
            }
        }
        viewModel.changeDeviceStatus.observe(viewLifecycleOwner, observer)
    }

    private fun setDeviceOff(token: String) {
        viewModel.setDeviceOff(token)
        val observer = object : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                if (value) {
                    helper.setDeviceOnline(false, requireContext())
                    Log.v("device status", "account offline")
                } else {
                    Log.v("device status", "account failed to be offline")
                }
                viewModel.changeDeviceStatus.removeObserver(this)
            }
        }
        viewModel.changeDeviceStatus.observe(viewLifecycleOwner, observer)
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.homeFragment)
        /*if (!helper.getDeviceStatus(requireContext())) {
            setDeviceOn(helper.getToken(requireContext()))
        }*/
    }

    /*override fun onStop() {
        if (helper.getDeviceStatus(requireContext())) {
            setDeviceOff(helper.getToken(requireContext()))
        }
        super.onStop()
    }*/

    private fun validateToken(token: String, layoutRes: Int) {
        viewModel.validateToken(token)
        val observer = object : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                if (value) {
                    findNavController().navigate(layoutRes)
                } else {
                    AlertDialog.Builder(requireContext())
                        .buildDialog(title = resources.getString(R.string.warning),
                            msg = resources.getString(R.string.subscription_end),
                            positiveButton = resources.getString(R.string.ok),
                            positiveButtonFunction = {
                                Log.v("Premium version", "premium version requested")
                            }, negativeButton = resources.getString(R.string.cancel),
                            negativeButtonFunction = {

                            })
                }
                viewModel.validateTokenResponse.removeObserver(this)
            }
        }
        viewModel.validateTokenResponse.observe(viewLifecycleOwner, observer)
    }

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
        list.add(MenuData(R.drawable.about_icon, resources.getString(R.string.about)))
        list.add(MenuData(R.drawable.logout_icon, resources.getString(R.string.logout)))
        val version = if (helper.getLang(requireContext()).equals("ar")) {
            helper.getAppVersion(requireContext()).mappingNumbers()
        } else {
            helper.getAppVersion(requireContext())
        }
        list.add(
            MenuData(
                0,
                "${resources.getString(R.string.version)} : $version"
            )
        )

        adapter.updateAdapter(list)
    }

    private fun signOut(body: SignOutBody, token: String) {
        viewModel.signOut(body, token)
        viewModel.signOutResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                helper.setRemember(requireContext(), false)
                findNavController().navigate(R.id.registerFragment)
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onItemClickListener(position: Int, extras: Bundle?) {
        when (position) {
            //premium
            0 -> {
                Log.i(TAG, "onItemClickListener: premium")
                findNavController().navigate(R.id.parentPayFragment)
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
            //about
            3 -> {
                val bundle = Bundle()
                bundle.putInt(SOURCE, R.id.homeFragment)
                findNavController().navigate(R.id.aboutFragment, bundle)
                Log.i(TAG, "onItemClickListener: about")
            }
            //logout
            4 -> {
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    val phone = usersDB.dao().getUserPhone(id) ?: ""
                    val body = SignOutBody(
                        phoneNumber = phone,
                        device = helper.getDeviceID(requireContext())
                    )
                    signOut(body, helper.getToken(requireContext()))
                }
//                helper.setRemember(requireContext(), false)
//                findNavController().navigate(R.id.registerFragment)

                Log.i(TAG, "onItemClickListener: logout")
            }
        }
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {

    }
}