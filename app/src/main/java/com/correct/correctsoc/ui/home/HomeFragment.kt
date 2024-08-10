package com.correct.correctsoc.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.MenuAdapter
import com.correct.correctsoc.data.MenuData
import com.correct.correctsoc.data.auth.SignOutBody
import com.correct.correctsoc.data.user.UserPlanResponse
import com.correct.correctsoc.databinding.FragmentHomeBinding
import com.correct.correctsoc.helper.AppMonitorService
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.ConnectionManager
import com.correct.correctsoc.helper.ConnectivityListener
import com.correct.correctsoc.helper.Constants.CLICKED
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TYPE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.OnSwipeGestureListener
import com.correct.correctsoc.helper.buildDialog
import com.correct.correctsoc.helper.hide
import com.correct.correctsoc.helper.isLightweightVersion
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.helper.show
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
    //private lateinit var slideIn: Animation
    //private lateinit var slideOut: Animation
    private lateinit var fadeIn: Animation
    private lateinit var fadeOut: Animation
    private lateinit var gestureDetector: GestureDetector
    private lateinit var usersDB: UsersDB
    private lateinit var viewModel: AuthViewModel
    private lateinit var fragmentListener: FragmentChangedListener
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var connectionManager: ConnectionManager
    private var isInternetConnected = false
    private var isConnected = MutableLiveData<Boolean>()
    private val notificationLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                // permission is granted
                val intent = Intent(requireContext(), AppMonitorService::class.java)
                ContextCompat.startForegroundService(requireContext(), intent)
                findNavController().navigate(R.id.fetchingAppsFragment)
            } else {
                // permission is denied
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.notification_required),
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            )
                    == PackageManager.PERMISSION_GRANTED -> {
                // permission granted
                val intent = Intent(requireContext(), AppMonitorService::class.java)
                ContextCompat.startForegroundService(requireContext(), intent)
                findNavController().navigate(R.id.fetchingAppsFragment)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // show an explanation to the user
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.notification_sorry),
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }

            else -> {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangedListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class]

        fragmentListener.onFragmentChangedListener(R.id.homeFragment)

        connectionManager = ConnectionManager(requireContext())


        Log.d("Is lightweight version mohamed","${requireContext().isLightweightVersion()}")

        connectionManager.observe()
        connectionManager.statusLiveData.observe(viewLifecycleOwner) {
            when (it) {
                ConnectivityListener.Status.AVAILABLE -> {
                    isInternetConnected = true
                    isConnected.postValue(true)
                    if (!helper.getDeviceStatus(requireContext())) {
                        setDeviceOn(helper.getToken(requireContext()))
                    }
                }

                ConnectivityListener.Status.UNAVAILABLE -> {
                    isInternetConnected = false
                    isConnected.postValue(false)
                }

                ConnectivityListener.Status.LOST -> {
                    isInternetConnected = false
                    isConnected.postValue(false)
                }

                ConnectivityListener.Status.LOSING -> {
                    isInternetConnected = false
                    isConnected.postValue(false)
                }
            }
        }

        isConnected.observe(viewLifecycleOwner) {
            if (!it) {
                noInternet()
            }
        }

        list = mutableListOf()
        adapter = MenuAdapter(requireContext(), list, this)

        binding.drawerMenu.recyclerView.adapter = adapter

        lifecycleScope.launch {
            val id = usersDB.dao().getUserID() ?: ""
            val user = usersDB.dao().getUser(id)
            if (user != null) {
                Log.v(TAG, user.id)
                Log.v(TAG, user.phone)
                Log.v(TAG, user.token)
                Log.v(TAG, user.password)
                Log.v(TAG, user.username)
                isConnected.observe(requireActivity()) {
                    if (it) {
                        getUserPlan(user.id)
                    }
                }
            }
        }

        fillList()

        fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        binding.btnRouter.setOnClickListener {
            if (isInternetConnected) {
                validateToken(helper.getToken(requireContext())) {
                    findNavController().navigate(R.id.selfPenFragment)
                }
            } else {
                noInternet()
            }
        }

        binding.btnIpScan.setOnClickListener {
            if (isInternetConnected) {
                validateToken(helper.getToken(requireContext())) {
                    val bundle = Bundle()
                    bundle.putString(TYPE, IP_ADDRESS)
                    findNavController().navigate(R.id.insertLinkFragment, bundle)
                }
            } else {
                noInternet()
            }
            /*isConnected.observe(viewLifecycleOwner) {
                if (it) {
                    validateToken(helper.getToken(requireContext())) {
                        val bundle = Bundle()
                        bundle.putString(TYPE, IP_ADDRESS)
                        findNavController().navigate(R.id.insertLinkFragment, bundle)
                    }
                } else {
                    noInternet()
                }
            }*/
        }

        binding.btnScan.setOnClickListener {
            if (isInternetConnected) {
                if (requireContext().isLightweightVersion()) {
                    val msg = if (helper.getLang(requireContext()).equals("ar")) {
                        resources.getString(R.string.android14).mappingNumbers()
                    } else {
                        resources.getString(R.string.android14)
                    }
                    AlertDialog.Builder(requireContext())
                        .buildDialog(title = resources.getString(R.string.warning),
                            msg = msg,
                            positiveButton = resources.getString(R.string.ok),
                            negativeButton = resources.getString(R.string.cancel),
                            positiveButtonFunction = {

                            },
                            negativeButtonFunction = {

                            })
                } else {
                    validateToken(helper.getToken(requireContext())) {
                        // request notification permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestNotificationPermission()
                        } else {
                            val intent = Intent(requireContext(), AppMonitorService::class.java)
                            ContextCompat.startForegroundService(requireContext(), intent)
                            findNavController().navigate(R.id.fetchingAppsFragment)
                        }
                    }
                }
                /*if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                    validateToken(helper.getToken(requireContext())) {
                        // request notification permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestNotificationPermission()
                        } else {
                            val intent = Intent(requireContext(), AppMonitorService::class.java)
                            ContextCompat.startForegroundService(requireContext(), intent)
                            findNavController().navigate(R.id.fetchingAppsFragment)
                        }
                    }
                } else {
                    val msg = if (helper.getLang(requireContext()).equals("ar")) {
                        resources.getString(R.string.android14).mappingNumbers()
                    } else {
                        resources.getString(R.string.android14)
                    }
                    AlertDialog.Builder(requireContext())
                        .buildDialog(title = resources.getString(R.string.warning),
                            msg = msg,
                            positiveButton = resources.getString(R.string.ok),
                            negativeButton = resources.getString(R.string.cancel),
                            positiveButtonFunction = {

                            },
                            negativeButtonFunction = {

                            })
                }*/
            } else {
                noInternet()
            }
            /*isConnected.observe(viewLifecycleOwner) {
                if (it) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                        validateToken(helper.getToken(requireContext())) {
                            // request notification permission
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestNotificationPermission()
                            } else {
                                val intent = Intent(requireContext(), AppMonitorService::class.java)
                                ContextCompat.startForegroundService(requireContext(), intent)
                                findNavController().navigate(R.id.fetchingAppsFragment)
                            }
                        }
                    } else {
                        val msg = if (helper.getLang(requireContext()).equals("ar")) {
                            resources.getString(R.string.android14).mappingNumbers()
                        } else {
                            resources.getString(R.string.android14)
                        }
                        AlertDialog.Builder(requireContext())
                            .buildDialog(title = resources.getString(R.string.warning),
                                msg = msg,
                                positiveButton = resources.getString(R.string.ok),
                                negativeButton = resources.getString(R.string.cancel),
                                positiveButtonFunction = {

                                },
                                negativeButtonFunction = {

                                })
                    }
                } else {
                    noInternet()
                }
            }*/
        }

        binding.btnAppScan.setOnClickListener {
            if (isInternetConnected) {
                validateToken(helper.getToken(requireContext())) {
                    findNavController().navigate(R.id.applicationScanFragment)
                }
            } else {
                noInternet()
            }
            /*isConnected.observe(viewLifecycleOwner) {
                if (it) {
                    validateToken(helper.getToken(requireContext())) {
                        findNavController().navigate(R.id.applicationScanFragment)
                    }
                } else {
                    noInternet()
                }
            }*/
        }

        binding.btnConnectedDevices.setOnClickListener {
            if (isInternetConnected) {
                validateToken(helper.getToken(requireContext())) {
                    findNavController().navigate(R.id.deviceScanningFragment)
                }
            } else {
                noInternet()
            }
            /*isConnected.observe(viewLifecycleOwner) {
                if (it) {
                    validateToken(helper.getToken(requireContext())) {
                        findNavController().navigate(R.id.deviceScanningFragment)
                    }
                } else {
                    noInternet()
                }
            }*/
        }

        binding.btnWebScan.setOnClickListener {
            if (isInternetConnected) {
                validateToken(helper.getToken(requireContext())) {
                    findNavController().navigate(R.id.insertLinkFragment)
                }
            } else {
                noInternet()
            }
            /*isConnected.observe(viewLifecycleOwner) {
                if (it) {
                    validateToken(helper.getToken(requireContext())) {
                        findNavController().navigate(R.id.insertLinkFragment)
                    }
                } else {
                    noInternet()
                }
            }*/
            //findNavController().navigate(R.id.insertLinkFragment)
        }

        val version = if (helper.getLang(requireContext()).equals("ar")) {
            helper.getAppVersion(requireContext()).mappingNumbers()
        } else {
            helper.getAppVersion(requireContext())
        }

        binding.txtVersion.append(" $version")

        helper.onBackPressed(this) {
            if (isVisible) {
                closeMenu()
            } else {
                requireActivity().finish()
            }
        }

        /*if (helper.getLang(requireContext()).equals("en")) {
            slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_en)
            slideOut = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_en)
        } else {
            slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_ar)
            slideOut = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_ar)
        }*/
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


        binding.root.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        binding.relativeLayout.setOnTouchListener { v, event ->
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
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("SetTextI18n")
    private fun getUserPlan(userID: String) {
        lifecycleScope.launch {
            val user = usersDB.dao().getUser(userID)
            if (user != null) {
                Log.v("Current user username", user.username)
                binding.drawerMenu.txtUsername.text =
                    "${resources.getString(R.string.hello)} ${user.username}"
            }
        }
        homeViewModel.getUserPlan(userID)
        val observer = object : Observer<UserPlanResponse?> {
            @SuppressLint("SetTextI18n")
            override fun onChanged(value: UserPlanResponse?) {
                if (value != null) {
                    if (value.isSuccess) {
                        val model = value.result
                        if (model != null) {
                            binding.drawerMenu.txtAccountType.text = model.planName
                            if (helper.getLang(requireContext()).equals("ar")) {
                                binding.drawerMenu.txtRemind.text =
                                    "${model.months} ${resources.getString(R.string.months)}".mappingNumbers()
                            } else {
                                binding.drawerMenu.txtRemind.text =
                                    "${model.months} ${resources.getString(R.string.months)}"
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            value.errorMessages,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    homeViewModel.userPlanResponse.removeObserver(this)
                }
            }
        }
        homeViewModel.userPlanResponse.observe(viewLifecycleOwner, observer)
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.homeFragment)
    }

    private fun validateToken(token: String, function: () -> Unit) {
        viewModel.validateToken(token)
        val observer = object : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                if (value) {
                    function()
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
        binding.placeholder.show()
        binding.drawerMenu.root.show()
        //binding.drawerMenu.root.startAnimation(slideIn)
        binding.drawerMenu.recyclerView.scrollToPosition(0)
        isVisible = true
    }

    private fun closeMenu() {
        binding.placeholder.hide()
        //binding.drawerMenu.root.startAnimation(slideOut)
        binding.drawerMenu.root.hide()
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
                    Log.v("device status", "account online")
                } else {
                    Log.v("device status", "account failed to be online")
                }
                viewModel.changeDeviceStatus.removeObserver(this)
            }
        }
        viewModel.changeDeviceStatus.observe(viewLifecycleOwner, observer)
    }

    private fun signOut(body: SignOutBody, token: String) {
        viewModel.signOut(body, token)
        viewModel.signOutResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                helper.setRemember(requireContext(), false)
                setDeviceOff(token)
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    if (id.isNotEmpty()) {
                        usersDB.dao().deleteUser(id)
                    }
                }
                findNavController().navigate(R.id.registerFragment)
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun noInternet() {
        AlertDialog.Builder(requireContext())
            .buildDialog(title = resources.getString(R.string.warning),
                msg = resources.getString(R.string.no_internet_connection),
                icon = R.drawable.no_internet_icon,
                positiveButton = resources.getString(R.string.ok),
                negativeButton = resources.getString(R.string.cancel),
                positiveButtonFunction = {

                },
                negativeButtonFunction = {

                })
    }

    override fun onItemClickListener(position: Int, extras: Bundle?) {
        if (extras != null) {
            //isConnected.observe(viewLifecycleOwner) {
            if (isInternetConnected) {
                val clicked = extras.getString(CLICKED)
                if (clicked.equals("menu")) {
                    when (position) {
                        //premium
                        0 -> {
                            Log.i(TAG, "onItemClickListener: premium")
                            findNavController().navigate(R.id.parentPayFragment)
                        }
                        //support
                        1 -> {
                            findNavController().navigate(R.id.liveChatFragment)
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

                            Log.i(TAG, "onItemClickListener: logout")
                        }
                    }
                }
            } else {
                noInternet()
            }
            //}
        }
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {

    }
}