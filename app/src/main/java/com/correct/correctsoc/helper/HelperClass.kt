package com.correct.correctsoc.helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.correct.correctsoc.data.openPorts.Port
import com.correct.correctsoc.helper.Constants.FIRST_TIME
import com.correct.correctsoc.helper.Constants.IS_LOGGED
import com.correct.correctsoc.helper.Constants.LANG
import com.correct.correctsoc.helper.Constants.NOTIFICATION
import com.correct.correctsoc.helper.Constants.SPLASH_TIME
import com.correct.correctsoc.helper.Constants.STATUS
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import com.correct.correctsoc.helper.Constants.TOKEN_VALUE

class HelperClass private constructor() {
    private lateinit var sp: SharedPreferences
    private lateinit var editor: Editor

    companion object {
        private var helper: HelperClass? = null
        fun getInstance(): HelperClass {
            if (helper == null) {
                helper = HelperClass()
            }
            return helper as HelperClass
        }
    }

    private fun initSP(context: Context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        editor = sp.edit()
    }

    fun setNotification(context: Context, notificationText: String) {
        initSP(context)
        editor.apply {
            putString(NOTIFICATION, notificationText)
            commit()
            apply()
        }
    }

    fun getNotificationText(context: Context): String {
        initSP(context)
        return sp.getString(NOTIFICATION, "Correctsoc Applocker") ?: "Correctsoc Applocker"
    }

    fun deleteSplashTime(context: Context) {
        initSP(context)
        editor.apply {
            remove(SPLASH_TIME)
            commit()
            apply()
        }
    }

    fun setToken(token: String, context: Context) {
        initSP(context)
        editor.apply {
            putString(TOKEN_KEY, "$TOKEN_VALUE $token")
            commit()
            apply()
        }
    }

    fun getToken(context: Context): String {
        initSP(context)
        return sp.getString(TOKEN_KEY, "") ?: ""
    }

    fun setRemember(context: Context, isLoggedIn: Boolean) {
        initSP(context)
        editor.apply {
            putBoolean(IS_LOGGED, isLoggedIn)
            commit()
            apply()
        }
    }

    fun getRemember(context: Context): Boolean {
        initSP(context)
        return sp.getBoolean(IS_LOGGED, false)
    }

    fun setLang(lang: String, context: Context) {
        initSP(context)
        editor.apply {
            putString(LANG, lang)
            commit()
            apply()
        }
    }

    fun getLang(context: Context): String {
        initSP(context)
        return sp.getString(LANG, "en") ?: "en"
    }

    fun setFirstStartApp(isFirstTime: Boolean, context: Context) {
        initSP(context)
        editor.apply {
            putBoolean(FIRST_TIME, isFirstTime)
            commit()
            apply()
        }
    }

    fun isFirstStartApp(context: Context): Boolean {
        initSP(context)
        return sp.getBoolean(FIRST_TIME, true)
    }

    fun circularAnimation(duration: Long): RotateAnimation {
        val anim = RotateAnimation(
            0.0f,
            360.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        //Setup anim with desired properties
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE //Repeat animation indefinitely

        anim.duration = duration //Put desired duration per anim cycle here, in milliseconds

        return anim
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        val packageManager = context.packageManager
        try {
            return packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun isSecureSite(list: MutableList<Port>): Boolean {
        var isSecure = true
        for (port in list) {
            if (port.cvEs.isNotEmpty()) {
                isSecure = false
                break
            }
        }
        return isSecure
    }

    fun onBackPressed(fragment: Fragment, navFunction: () -> Unit) {
        (fragment.activity as AppCompatActivity).supportFragmentManager
        fragment.requireActivity().onBackPressedDispatcher.addCallback(
            fragment.requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navFunction()
                }
            })
    }

    fun isEmpty(vararg edits: EditText): Boolean {
        var isAnyEmpty = false
        for (edit in edits) {
            if (edit.text.isEmpty()) {
                isAnyEmpty = true
                break
            }
        }
        return isAnyEmpty
    }

    @SuppressLint("HardwareIds")
    fun getDeviceID(context: Context) =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    fun generateFirstCode(): String {
        val string = "ZXCVBNMASDFGHJKLQWERTYUIOP1234567890"
        var code = ""
        code += string.generatNRandom(8)
        code += "-"
        code += string.generatNRandom(4)
        code += "-"
        code += string.generatNRandom(4)
        code += "-"
        code += string.generatNRandom(4)
        code += "-"
        code += string.generatNRandom(12)

        return code
    }

    private fun String.generatNRandom(length: Int): String {
        var randomStr = ""
        for (i in 0..<length) {
            val random = this.indices.random()
            randomStr += this[random]
        }
        return randomStr
    }

    fun isUnknownSourcesEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.REQUEST_INSTALL_PACKAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return context.packageManager.canRequestPackageInstalls()
            } else {
                return false
            }
        } else {
            val unknownSources = Settings.Secure.getInt(
                context.contentResolver, Settings
                    .Secure.INSTALL_NON_MARKET_APPS, 0
            )
            return unknownSources == 1
        }
    }

    fun setDeviceOnline(isOnline: Boolean, context: Context) {
        initSP(context)
        editor.apply {
            putBoolean(STATUS, isOnline)
            commit()
            apply()
        }
    }

    fun getDeviceStatus(context: Context): Boolean {
        initSP(context)
        return sp.getBoolean(STATUS, false)
    }
}