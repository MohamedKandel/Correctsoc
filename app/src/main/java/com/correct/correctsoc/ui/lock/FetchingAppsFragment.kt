package com.correct.correctsoc.ui.lock

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentFetchingAppsBinding
import com.correct.correctsoc.helper.AppsFetchedListener
import com.correct.correctsoc.helper.Constants
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

class FetchingAppsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentFetchingAppsBinding
    private lateinit var helper: HelperClass
    private var progressJob: Job? = null
    private var fetchAppssJob: Job? = null
    private lateinit var fragmentListener: FragmentChangedListener
    private var list = mutableListOf<App>()

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
        binding = FragmentFetchingAppsBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        fragmentListener.onFragmentChangedListener(R.id.fetchingAppsFragment)


        binding.root.keepScreenOn = true

        binding.btnStop.setOnClickListener {
            stopOperations()
        }

        helper.onBackPressed(this) {
            stopOperations()
        }

        CoroutineScope(Dispatchers.Main).launch {
            //var list = mutableListOf<App>()
            val progressJob = launch(Dispatchers.Main) {
                progress(object : OnProgressUpdatedListener {
                    @SuppressLint("SetTextI18n")
                    override fun onUpdateProgressLoad(progress: Int) {
                        binding.txtPercent.text = "$progress%"

                        if (progress == 100) {
                            Log.e("List Devices mohamed", "onCreateView: ${list.size}")
                            Log.d("List Devices mohamed", "onCreateView: finished")
                            val bundle = Bundle()
                            bundle.putParcelableArrayList(Constants.LIST, ArrayList(list))
                            findNavController().navigate(R.id.appsFragment, bundle)
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

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val progress = binding.txtPercent.text
        if (progress == "100%") {
            Log.i("Progress completed", "Progress is completed")
            Log.e("List Devices onResume mohamed", "onCreateView: ${list.size}")
            Log.d("List Devices mohamed", "onCreateView: finished")
            val bundle = Bundle()
            bundle.putParcelableArrayList(Constants.LIST, ArrayList(list))
            findNavController().navigate(R.id.appsFragment, bundle)
        } else {
            binding.progressCircular.startAnimation(helper.circularAnimation(3000))
        }
        fragmentListener.onFragmentChangedListener(R.id.fetchingAppsFragment)
    }

    override fun onPause() {
        super.onPause()
        binding.progressCircular.clearAnimation()
    }

    private fun displayAppName(list: MutableList<App>) {
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
                        300
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
    suspend fun fetchApps(): MutableList<App> {
        return suspendCancellableCoroutine {
            getApps(object : AppsFetchedListener<App> {
                override fun onAllAppsFetched(apps: MutableList<App>) {
                    it.resume(apps, null)
                }
            })
        }
    }

    private fun getApps(listener: AppsFetchedListener<App>) {
        val packageManager = requireContext().packageManager
        val list = mutableListOf<App>()
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        for (packageInfo in installedPackages) {
            val applicationInfo = packageInfo.applicationInfo
            val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val hasLauncherIntent =
                packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null

            if (!isSystemApp || hasLauncherIntent) {
                val appName = applicationInfo.loadLabel(packageManager).toString()
                val packageName = packageInfo.packageName
                list.add(App(packageName = packageName, appName = appName, "", 1))
            }
        }
        listener.onAllAppsFetched(list)
    }

    suspend fun progress(progress: OnProgressUpdatedListener, isCanceled: () -> Boolean) {
        withContext(Dispatchers.Main) {
            for (i in 0..100) {
                if (isCanceled()) {
                    break
                }
                progress.onUpdateProgressLoad(i)
                delay(50)
            }
        }
    }

    private fun stopOperations() {
        // Cancel progress job
        progressJob?.cancel()
        // Cancel fetch devices job
        fetchAppssJob?.cancel()
        findNavController().navigate(R.id.homeFragment)
    }
}