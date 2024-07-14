package com.correct.correctsoc.ui.applicationScan

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.APKAdapter
import com.correct.correctsoc.data.AppInfo
import com.correct.correctsoc.databinding.FragmentAPKsBinding
import com.correct.correctsoc.helper.AudioUtils
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.CLICKED
import com.correct.correctsoc.helper.Constants.DELETE
import com.correct.correctsoc.helper.Constants.LIST
import com.correct.correctsoc.helper.Constants.PKG_NAME
import com.correct.correctsoc.helper.DeleteReceiver
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.PackageListener


class APKsFragment : Fragment(), ClickListener, PackageListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentAPKsBinding
    private lateinit var adapter: APKAdapter
    private lateinit var list: MutableList<AppInfo>
    private lateinit var receiver: DeleteReceiver
    private lateinit var helper: HelperClass
    private var index = 0
    private lateinit var audioUtils: AudioUtils
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
        binding = FragmentAPKsBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        audioUtils = AudioUtils.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.APKsFragment)
        //list = getAppsInstalledFromUnknownSources().toMutableList()
        list = mutableListOf()
        adapter = APKAdapter(requireContext(), list, this)
        binding.appRecyclerView.adapter = adapter

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.applicationScanFragment)
        }

        //audioUtils.releaseMedia()


        if (arguments != null) {
            list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelableArrayList(LIST, AppInfo::class.java)!!
            } else {
                requireArguments().getParcelableArrayList(LIST)!!
            }
            for (app in list) {
                app.appIcon = helper.getAppIcon(requireContext(), app.packageName)
            }

            // to delay the app till destructor of mediaplayer called from last fragment
            Handler(Looper.getMainLooper()).postDelayed({
                // applications found
                if (list.isNotEmpty()) {
                    Log.v("List mohamed", "apps found")
                    if (helper.getLang(requireContext()).equals("en")) {
                        audioUtils.playAudio(requireContext(), R.raw.apk_msg_en)
                    } else {
                        audioUtils.playAudio(requireContext(), R.raw.apk_msg_ar)
                    }
                }
                // no apps found
                else {
                    Log.v("List mohamed", "apps not found")
                    if (helper.getLang(requireContext()).equals("en")) {
                        audioUtils.playAudio(requireContext(), R.raw.secure_en)
                    } else {
                        audioUtils.playAudio(requireContext(), R.raw.secure_ar)
                    }
                }
            }, 500)

            adapter.updateAdapter(list)

            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            intentFilter.addDataScheme("package")

            receiver = DeleteReceiver(this)
            requireActivity().registerReceiver(receiver, intentFilter)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    override fun onDestroyView() {
        requireActivity().unregisterReceiver(receiver)
        audioUtils.releaseMedia()
        super.onDestroyView()
    }

    private fun getAppsInstalledFromUnknownSources(): List<AppInfo> {
        val packageManager = requireContext().packageManager
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val unknownSourceApps = mutableListOf<AppInfo>()

        for (packageInfo in installedPackages) {
            val applicationInfo = packageInfo.applicationInfo
            if ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) { // Exclude system apps
                val installerPackageName: String?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val installSourceInfo =
                        packageManager.getInstallSourceInfo(packageInfo.packageName)
                    installerPackageName = installSourceInfo.installingPackageName
                } else {
                    installerPackageName =
                        packageManager.getInstallerPackageName(packageInfo.packageName)
                }
                if (installerPackageName == null || !isOfficialInstaller(installerPackageName)) {
                    val appIcon = applicationInfo.loadIcon(packageManager)
                    val appName = applicationInfo.loadLabel(packageManager).toString()
                    unknownSourceApps.add(AppInfo(packageInfo.packageName, appName, appIcon))
                }
            }
        }

        return unknownSourceApps
    }

    private fun isOfficialInstaller(installerPackageName: String): Boolean {
        // Add official installer package names here
        val officialInstallers = listOf(
            "com.android.vending",                  // Google Play Store
            "com.amazon.venezia",                   // Amazon Appstore
            "com.oppo.market",                      // OPPO App Market
            "com.xiaomi.market",                    // Xiaomi Mi Market
            "com.huawei.appmarket",                 // Huawei AppGallery
            "com.sec.android.app.samsungapps",      //Samsung Galaxy Sore
            "com.bbk.appstore",                     //VIVO Appstore
            "com.oneplus.store",                    //OnePlus Appstore
            "com.lenovo.leos.appstore",             //Lenovo Appstore
        )
        return officialInstallers.contains(installerPackageName)
    }

    override fun onItemClickListener(position: Int, extras: Bundle?) {
        if (extras != null) {
            val clicked = extras.getString(CLICKED)
            // click on delete button
            if (clicked.equals(DELETE)) {
                index = position
                val intent = Intent(Intent.ACTION_DELETE)
                intent.setData(Uri.parse("package:${extras.getString(PKG_NAME)}"))
                startActivity(intent)
            }
            // clicked on itemview (do something)
            else {
                //TODO("Not yet implemented")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.APKsFragment)
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {
        //TODO("Not yet implemented")
    }

    override fun onApplicationDeleted() {
        Log.i("package mohamed", "onApplicationDeleted: $index")
        list.removeAt(index)
        list.drop(index)
        adapter.updateAdapter(list)
    }
}