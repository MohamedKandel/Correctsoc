package com.correct.correctsoc.ui.deviceScan

import android.content.Context
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
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.OnSwipeGestureListener
import com.correct.correctsoc.helper.mappingNumbers
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
    /*private var isVisible = false
    private lateinit var slideIn: Animation
    private lateinit var slideOut: Animation
    private lateinit var gestureDetector: GestureDetector
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var menuList: MutableList<MenuData>*/
    private lateinit var helper: HelperClass
    private val TAG = "DevicesFragment mohamed"
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
        binding = FragmentDevicesBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        fragmentListener.onFragmentChangedListener(R.id.devicesFragment)
        list = mutableListOf()
        adapter = DevicesAdapter(requireContext(), helper, list, this)
        binding.devicesRecyclerView.adapter = adapter


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

        /*fillList()

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
        }*/

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    /*private fun fillList() {

        val version = if (helper.getLang(requireContext()).equals("ar")) {
            helper.getAppVersion(requireContext()).mappingNumbers()
        } else {
            helper.getAppVersion(requireContext())
        }

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
                "${resources.getString(R.string.version)} : $version"
            )
        )

        menuAdapter.updateAdapter(menuList)
    }*/

    private fun search(keyword: String): MutableList<DevicesData> {
        var filterList = mutableListOf<DevicesData>()
        for (device in list) {
            if (device.ipAddress.contains(keyword)) {
                filterList.add(device)
            }
        }

        return filterList
    }

    /*private fun openMenu() {
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
    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.devicesFragment)
    }
}