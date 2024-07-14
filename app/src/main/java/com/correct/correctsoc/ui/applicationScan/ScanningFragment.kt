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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.AppInfo
import com.correct.correctsoc.databinding.FragmentScanningBinding
import com.correct.correctsoc.helper.AppsFetchedListener
import com.correct.correctsoc.helper.Constants.LIST
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.OnProgressUpdatedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.random.Random

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
                val random:Int = when(list.size) {
                    in 0..10 -> {
                        1500
                    }
                    in 11 .. 15 -> {
                        1000
                    }
                    in 15 .. 20 -> {
                        750
                    } else -> {
                        500
                    }
                }
                Log.i("random number mohamed", "displayAppName: $random")
                delay(random.toLong())
                if (i == list.size) {
                    break
                }
            }
            binding.txtAppName.visibility = View.GONE
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun fetchApps(): MutableList<AppInfo> {
        return suspendCancellableCoroutine {
            getApps(object : AppsFetchedListener {
                override fun onAllAppsFetched(apps: MutableList<AppInfo>) {
                    it.resume(apps, null)
                }
            })
        }
    }

    private fun getApps(listener: AppsFetchedListener) {
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
        listener.onAllAppsFetched(unknownSourceApps)
    }

    private fun isOfficialInstaller(installerPackageName: String): Boolean {
        // Add official installer package names here
        val officialInstallers = listOf(
            "com.android.vending",                  // Google Play Store
            "com.amazon.venezia",                   // Amazon Appstore
            "com.oppo.market",                      // OPPO App Market
            "com.xiaomi.market",                    // Xiaomi Mi Market
            "com.huawei.appmarket",                 // Huawei AppGallery
            "com.sec.android.app.samsungapps",      // Samsung Galaxy Sore
            "com.bbk.appstore",                     // VIVO Appstore
            "com.oneplus.store",                    // OnePlus Appstore
            "com.lenovo.leos.appstore",             // Lenovo Appstore
        )
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