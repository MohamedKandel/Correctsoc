package com.correct.correctsoc.ui.applicationScan

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.AppInfo
import com.correct.correctsoc.databinding.FragmentScanningBinding
import com.correct.correctsoc.helper.AppsFetchedListener
import com.correct.correctsoc.helper.Constants.LIST
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.OnProgressUpdatedListener
import com.correct.correctsoc.helper.hide
import com.correct.correctsoc.room.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class ScanningFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentScanningBinding
    private lateinit var helper: HelperClass
    private var progressJob: Job? = null
    private var fetchAppssJob: Job? = null
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
        binding = FragmentScanningBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.scanningFragment)

        binding.progressCircular.startAnimation(helper.circularAnimation(3000))

        binding.root.keepScreenOn = true

        CoroutineScope(Dispatchers.Main).launch {
            var list = mutableListOf<AppInfo>()
            val progressJob = launch(Dispatchers.Main) {
                progress(object : OnProgressUpdatedListener {
                    @SuppressLint("SetTextI18n")
                    override fun onUpdateProgressLoad(progress: Int) {
                        binding.txtPercent.text = "$progress%"

                        if (progress == 100) {
                            Log.e("List Devices mohamed", "onCreateView: ${list.size}")
                            Log.d("List Devices mohamed", "onCreateView: finished")
                            val bundle = Bundle()
                            bundle.putParcelableArrayList(LIST, ArrayList(list))
                            findNavController().navigate(R.id.appScanResultFragment, bundle)
                        }
                    }
                }) {
                    fetchAppssJob?.isActive == false
                }
            }
            fetchAppssJob = launch(Dispatchers.IO) {
                list = fetchApps()
                withContext(Dispatchers.Main) {
                    displayAppName(list)
                }
                Log.e("List Devices mohamed", "onCreateView: ${list.size}")
                progressJob.join()
            }
        }

        binding.btnStop.setOnClickListener {
            stopOperations()
        }



        helper.onBackPressed(this) {
            stopOperations()
            findNavController().navigate(R.id.applicationScanFragment)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.scanningFragment)
    }

    private fun displayAppName(list: MutableList<AppInfo>) {
        CoroutineScope(Dispatchers.Main).launch {
            var i = 0
            for (app in list) {
                binding.txtAppName.text = app.appName
                i++
                val random: Int = when (list.size) {
                    in 0..10 -> {
                        1500
                    }

                    in 11..15 -> {
                        1000
                    }

                    in 15..20 -> {
                        750
                    }

                    else -> {
                        500
                    }
                }
                Log.i("random number mohamed", "displayAppName: $random")
                delay(random.toLong())
                if (i == list.size) {
                    break
                }
            }
            binding.txtAppName.hide()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun fetchApps(): MutableList<AppInfo> {
        return suspendCancellableCoroutine {
            getApps(object : AppsFetchedListener<AppInfo> {
                override fun onAllAppsFetched(apps: MutableList<AppInfo>) {
                    it.resume(apps, null)
                }
            })
        }
    }

    private fun getApps(listener: AppsFetchedListener<AppInfo>) {
        val apps =
            requireContext().packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val list = mutableListOf<AppInfo>()
        for (app in apps) {
            val info = app.applicationInfo
            val installerPackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val installSourceInfo =
                    requireContext().packageManager.getInstallSourceInfo(app.packageName)
                installSourceInfo.installingPackageName
            } else {
                requireContext().packageManager.getInstallerPackageName(app.packageName)
            }
            if (installerPackageName != null) {
                if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    && info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0
                    && info.sourceDir.contains("/data/app/")
                    && !isOfficialInstaller(installerPackageName)
                    && !isPreInstalledApp(info)
                ) {
                    // apk is com.google.android.packageinstaller
                    Log.v("App found source", installerPackageName)
                    if (isUserInstalledApp(
                            installerPackageName,
                            info.packageName
                        ) || isPackageInstaller(installerPackageName, info.packageName)
                        || installerPackageName == "com.whatsapp"
                    ) {
                        Log.v("App source info mohamed", info.sourceDir)
                        //Log.v("Installing mohamed", installerPackageName)
                        list.add(
                            AppInfo(
                                packageName = info.packageName,
                                appName = info.loadLabel(requireContext().packageManager)
                                    .toString(),
                                appIcon = info.loadIcon(requireContext().packageManager)
                            )
                        )
                    }
                }
            }
        }
        listener.onAllAppsFetched(list)
    }

    private fun isPreInstalledApp(appInfo: ApplicationInfo): Boolean {
        val sourceDir = appInfo.sourceDir
        return sourceDir.startsWith("/system/") || sourceDir.startsWith("/vendor/") ||
                sourceDir.startsWith("/product/") || sourceDir.startsWith("/oem/") ||
                sourceDir.startsWith("/system_ext/") || sourceDir.startsWith("/data/preload/")
    }

    /*private fun getApps(listener: AppsFetchedListener<AppInfo>) {
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
                    if(!whiteListAppsPackages(packageInfo.packageName) && !isAllowedAppWithAndroid(packageInfo.packageName)) {
                        val appIcon = applicationInfo.loadIcon(packageManager)
                        val appName = applicationInfo.loadLabel(packageManager).toString()
                        Log.d(
                            "InstallerCheck",
                            "Package: ${packageInfo.packageName}, Installer: $installerPackageName"
                        )
                        unknownSourceApps.add(AppInfo(packageInfo.packageName, appName, appIcon))
                    }
                }
            }
        }
        listener.onAllAppsFetched(unknownSourceApps)
    }

    private fun whiteListAppsPackages(packageName: String): Boolean {
        val whiteList = listOf(
            "com.android.calendar.go",
            "com.xiaomi.midrop",
            "com.miui.calculator.go"
        )
        return packageName in whiteList
    }

    private fun isAllowedAppWithAndroid(packageName: String): Boolean {
        val officialApps = listOf(
            "com.wego.android",
            "com.amazon.appmanager"
        )
        return officialApps.contains(packageName)
    }*/

    private fun isUserInstalledApp(InstallerPackageName: String, packageName: String): Boolean {
        val packageManager = requireContext().packageManager
        val appInfo: ApplicationInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return false
        }

        // Check if it's not a system app or an updated system app
        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

        // Check if the app is installed in the user apps directory
        val isInUserAppsDirectory = appInfo.sourceDir.startsWith("/data/app/")

        // Return true if installer is "android", it's not a system app, and it's in the user apps directory
        return InstallerPackageName == "android" && !isSystemApp && !isUpdatedSystemApp && isInUserAppsDirectory
    }

    private fun isPackageInstaller(installerPackageName: String, packageName: String): Boolean {
        val packageInstaller = "com.google.android.packageinstaller"
        return installerPackageName == packageInstaller
    }

    private fun isOfficialInstaller(installerPackageName: String): Boolean {
        // Add official installer package names here
        val officialInstallers = listOf(
            "com.facebook.system",                  // Facebook app manager
            "com.xiaomi.mipicks",                   // Xiaomi's MI Picks app
            "com.heytap.market",                    // Realme & OPPO
            "com.android.vending",                  // Google Play Store
            "com.xiaomi.discover",                  // Xiaomi's own app store or app discovery service
            "com.amazon.venezia",                   // Amazon Appstore
            "com.oppo.market",                      // OPPO App Market
            "com.xiaomi.market",                    // Xiaomi Mi Market
            "com.huawei.appmarket",                 // Huawei AppGallery
            "com.sec.android.app.samsungapps",      // Samsung Galaxy Store
            "com.bbk.appstore",                     // VIVO Appstore
            "com.oneplus.store",                    // OnePlus Appstore
            "com.lenovo.leos.appstore"              // Lenovo Appstore
        )

        Log.d("Installing source mohamed", "application installed from $installerPackageName")
        return officialInstallers.contains(installerPackageName)
    }

    suspend fun progress(progress: OnProgressUpdatedListener, isCanceled: () -> Boolean) {
        withContext(Dispatchers.Main) {
            for (i in 0..100) {
                if (isCanceled()) {
                    break
                }
                progress.onUpdateProgressLoad(i)
                delay(150)
            }
        }
    }

    private fun stopOperations() {
        // Cancel progress job
        progressJob?.cancel()
        // Cancel fetch devices job
        fetchAppssJob?.cancel()
        findNavController().navigate(R.id.applicationScanFragment)
    }

}