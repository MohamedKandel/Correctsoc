package com.correct.correctsoc.ui.deviceScan

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.DevicesAdapter
import com.correct.correctsoc.adapter.MenuAdapter
import com.correct.correctsoc.data.DevicesData
import com.correct.correctsoc.data.MenuData
import com.correct.correctsoc.databinding.FragmentDevicesBinding
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants
import com.correct.correctsoc.helper.Constants.ITEM
import com.correct.correctsoc.helper.Constants.LIST
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.OnSwipeGestureListener
import tej.androidnetworktools.lib.Device
import tej.androidnetworktools.lib.scanner.NetworkScanner
import tej.androidnetworktools.lib.scanner.OnNetworkScanListener
import tej.wifitoolslib.DevicesFinder
import tej.wifitoolslib.interfaces.OnDeviceFindListener
import tej.wifitoolslib.models.DeviceItem


class DevicesFragment : Fragment(), ClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentDevicesBinding
    private lateinit var list: MutableList<DevicesData>
    private lateinit var adapter: DevicesAdapter
    private var isVisible = false
    private lateinit var slideIn: Animation
    private lateinit var slideOut: Animation
    private lateinit var gestureDetector: GestureDetector
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var menuList: MutableList<MenuData>
    private lateinit var helper: HelperClass
    private val TAG = "DevicesFragment mohamed"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDevicesBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        list = mutableListOf()
        adapter = DevicesAdapter(requireContext(), helper, list, this)
        binding.devicesRecyclerView.adapter = adapter

        menuList = mutableListOf()
        menuAdapter = MenuAdapter(requireContext(), menuList, object : ClickListener {
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
                        val bundle = Bundle()
                        bundle.putInt(Constants.SOURCE, R.id.devicesFragment)
                        bundle.putParcelableArrayList(LIST, ArrayList(list))
                        findNavController().navigate(R.id.settingFragment, bundle)
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
                    }
                    //about
                    6 -> {
                        val bundle = Bundle()
                        bundle.putInt(Constants.SOURCE, R.id.devicesFragment)
                        bundle.putParcelableArrayList(LIST, ArrayList(list))
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
//                TODO("Not yet implemented")
            }
        })
        binding.drawerMenu.recyclerView.adapter = menuAdapter


        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        if (arguments != null) {
            list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelableArrayList(LIST, DevicesData::class.java)!!
            } else {
                requireArguments().getParcelableArrayList<DevicesData>(LIST)!!
            }
            adapter.updateAdapter(list)


            binding.txtSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
//                    TODO("Not yet implemented")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    TODO("Not yet implemented")
                    //Log.i("Text mohamed", "onTextChanged: ${s.toString()}")
                    val keyword = s.toString()
                    if (keyword.length > 0) {
                        var filtered = search(keyword)
                        if (filtered.size == 0) {
                            Toast.makeText(
                                requireContext(), resources.getText(R.string.not_found_search),
                                Toast.LENGTH_SHORT
                            ).show()
                            filtered = list
                        }
                        adapter.updateAdapter(filtered)
                    }
                }

                override fun afterTextChanged(s: Editable?) {
//                    TODO("Not yet implemented")
                }
            })
        }

        fillList()

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

        binding.placeholder.setOnClickListener {
            closeMenu()
        }
        binding.menuIcon.setOnClickListener {
            if (isVisible) {
                closeMenu()
            } else {
                openMenu()
            }
        }
        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    private fun fillList() {
        menuList.add(MenuData(R.drawable.premium_icon, resources.getString(R.string.premium)))
        menuList.add(MenuData(R.drawable.support_icon, resources.getString(R.string.support)))
        menuList.add(MenuData(R.drawable.setting_icon, resources.getString(R.string.setting)))
        menuList.add(MenuData(R.drawable.reward_icon, resources.getString(R.string.reward)))
        menuList.add(MenuData(R.drawable.community_icon, resources.getString(R.string.community)))
        menuList.add(MenuData(R.drawable.rate_icon, resources.getString(R.string.rate)))
        menuList.add(MenuData(R.drawable.about_icon, resources.getString(R.string.about)))
        menuList.add(MenuData(R.drawable.logout_icon, resources.getString(R.string.logout)))
        menuList.add(
            MenuData(
                0,
                "${resources.getString(R.string.version)} : ${helper.getAppVersion(requireContext())}"
            )
        )

        menuAdapter.updateAdapter(menuList)
    }

    private fun search(keyword: String): MutableList<DevicesData> {
        var filterList = mutableListOf<DevicesData>()
        for (device in list) {
            if (device.ipAddress.contains(keyword)) {
                filterList.add(device)
            }
        }

        return filterList
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

    /*private fun onBackPressed() {
        (activity as AppCompatActivity).supportFragmentManager
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Back is pressed... Finishing the activity
                    findNavController().navigate(R.id.homeFragment)
                }
            })
    }*/

    override fun onItemClickListener(position: Int, extras: Bundle?) {
        //TODO("Not yet implemented")
        val model: DevicesData
        if (extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                model = extras.getParcelable(ITEM, DevicesData::class.java)!!
            } else {
                model = extras.getParcelable<DevicesData>(ITEM)!!
            }
            requireArguments().putParcelable(ITEM, model)
            findNavController().navigate(R.id.deviceDataFragment, requireArguments())
            Log.i(TAG, "onItemClickListener: ${model.macAddress}")
        }
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {
        //TODO("Not yet implemented")
    }
}