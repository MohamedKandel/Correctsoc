package com.correct.correctsoc.helper

import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.correct.correctsoc.data.openPorts.Port
import com.correct.correctsoc.helper.Constants.FIRST_TIME
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.Constants.LANG
import com.correct.correctsoc.helper.Constants.SPLASH_TIME
import android.text.Spannable
import android.util.Log
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R

class HelperClass {
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

    fun setSplashTime(duration: Long, context: Context) {
        initSP(context)
        editor.apply {
            putLong(SPLASH_TIME, duration)
            commit()
            apply()
        }
    }

    fun getSplashTime(context: Context): Long {
        initSP(context)
        return sp.getLong(SPLASH_TIME, 0)
    }

    fun deleteSplashTime(context: Context) {
        initSP(context)
        editor.apply {
            remove(SPLASH_TIME)
            commit()
            apply()
        }
    }

    fun getAppVersion(context: Context): String {
        val pkgInfo = context.packageManager.getPackageInfo(
            context.packageName, 0
        )
        return pkgInfo.versionName
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

    fun gradientText(startColor: Int, endColor: Int, txtView: TextView) {
        val paint = txtView.paint
        val width = paint.measureText(txtView.text.toString())
        val shader = LinearGradient(
            0f, 0f, 0f, txtView.textSize,
            intArrayOf(startColor, endColor),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        txtView.paint.shader = shader
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

    fun getDeviceType(vendorName: String): String {
        val map = mapOf(
            "Apple" to "Mobile Phone",
            "Samsung" to "Mobile Phone",
            "Huawei" to "Mobile Phone",
            "Dell" to "PC",
            "HP" to "PC",
            "Lenovo" to "PC",
            "Cisco" to "Access Point",
            "TP-Link" to "Access Point",
            "Netgear" to "Access Point",
            "Zhejiang Uniview Technologies Co.,Ltd." to "Security Camera"
        )

        val keys = map.keys
        var value = "Unknown"
        for (key in keys) {
            if (key.contains(vendorName, true)) {
                value = map[key].toString()
                break
            }
        }
        return value
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

    fun setIPAddress(context: Context, ip: String) {
        initSP(context)
        editor.apply {
            putString(IP_ADDRESS, ip)
            commit()
            apply()
        }
    }

    fun getIPAddress(context: Context): String {
        initSP(context)
        return sp.getString(IP_ADDRESS, "") ?: ""
    }

    fun setSpannable(
        startIndex: Int,
        endIndex: Int,
        text: String,
        color: Int,
        executableFun: () -> Unit
    ): SpannableString {
        val spannableString = SpannableString(text)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                executableFun()
            }
        }

        spannableString.setSpan(clickableSpan, startIndex, endIndex, 0)
        spannableString.setSpan(
            android.text.style.ForegroundColorSpan(color),
            startIndex,
            endIndex,
            0
        )

        return spannableString
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

    fun colorfulTextView(
        startIndex: Int,
        endIndex: Int,
        color: Int,
        text: String
    ): SpannableString {
        val spannableString = SpannableString(text)
        val colorSpan = ForegroundColorSpan(color)

        // Apply the color span to the spannable string
        spannableString.setSpan(
            colorSpan, startIndex, endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
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

    fun mappingNumbers(txt: String): String {
        var str = ""
        val map = mapOf(
            '1' to "١",
            '2' to "٢",
            '3' to "٣",
            '4' to "٤",
            '5' to "٥",
            '6' to "٦",
            '7' to "٧",
            '8' to "٨",
            '9' to "٩",
            '0' to "٠"
        )
        for (c in txt) {
            if (c in map.keys) {
                str += map[c]
            } else {
                str += c
            }
        }
        return str
    }
}