package com.correct.correctsoc

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.correct.correctsoc.databinding.ActivityMainBinding
import com.correct.correctsoc.helper.HelperClass
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.startUpdateFlowForResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var helper: HelperClass
    private var appUpdateType = AppUpdateType.IMMEDIATE
    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        helper = HelperClass.getInstance()

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        if (appUpdateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.registerListener(installStateUpdateListener)
        }

        checkForUpdate()

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController

        setLocale(helper.getLang(applicationContext))
//        enableEdgeToEdge()
        setContentView(binding.root)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val resources: Resources = resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        createConfigurationContext(configuration)
    }

    private fun checkForUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener {
            val isUpdateAllowed = when (appUpdateType) {
                AppUpdateType.IMMEDIATE -> it.isImmediateUpdateAllowed
                AppUpdateType.FLEXIBLE -> it.isFlexibleUpdateAllowed
                else -> false
            }
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && isUpdateAllowed
            ) {
                // request update
                /* update with custom dialog
                val dialog = Dialog(applicationContext)
                dialog.setContentView(R.layout.request_update_dialog)
                dialog.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(false)
                val btn_ok = dialog.findViewById<TextView>(R.id.btn_ok)
                val btn_cancel = dialog.findViewById<TextView>(R.id.btn_cancel)

                btn_ok.setOnClickListener {
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$packageName")
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                            )
                        )
                    }
                    dialog.dismiss()
                    dialog.cancel()
                }

                btn_cancel.setOnClickListener {
                    dialog.dismiss()
                    dialog.cancel()
                }

                dialog.show()*/
                /*update with google flow*/
                appUpdateManager.startUpdateFlowForResult(
                    it,
                    arl,
                    AppUpdateOptions.newBuilder(appUpdateType).build()
                )
            } else {
                // there is no update found
                Log.v("Update mohamed", "No update available ${it.availableVersionCode()}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    it,
                    arl,
                    AppUpdateOptions.newBuilder(appUpdateType).build()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (appUpdateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.unregisterListener(installStateUpdateListener)
        }
    }

    private val installStateUpdateListener = InstallStateUpdatedListener {
        if (it.installStatus() == InstallStatus.DOWNLOADED) {
            Toast.makeText(
                this, resources.getString(R.string.restart_required), Toast.LENGTH_SHORT
            ).show()
            lifecycleScope.launch {
                delay(5000)
                appUpdateManager.completeUpdate()
            }
        }
    }

    private val arl =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            val resultCode = result.resultCode
            when {
                resultCode == Activity.RESULT_OK -> {
                    Log.v("MyActivity", "Update flow completed!")
                }

                resultCode == Activity.RESULT_CANCELED -> {
                    Log.v("MyActivity", "User cancelled Update flow!")
                    // display dialog to request update
                    val dialog = Dialog(applicationContext)
                    dialog.setContentView(R.layout.request_update_dialog)
                    dialog.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
                    dialog.setCancelable(true)
                    dialog.setCanceledOnTouchOutside(false)
                    val btn_ok = dialog.findViewById<TextView>(R.id.btn_ok)
                    val btn_cancel = dialog.findViewById<TextView>(R.id.btn_cancel)

                    btn_ok.setOnClickListener {
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$packageName")
                                )
                            )
                        } catch (e: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                )
                            )
                        }
                        dialog.dismiss()
                        dialog.cancel()
                    }

                    btn_cancel.setOnClickListener {
                        dialog.dismiss()
                        dialog.cancel()
                    }

                    dialog.show()
                }

                else -> {
                    Log.v("MyActivity", "Update flow failed with resultCode:$resultCode")
                }
            }
        }
}