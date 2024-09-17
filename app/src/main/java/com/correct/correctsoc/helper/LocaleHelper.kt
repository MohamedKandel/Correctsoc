package com.correct.correctsoc.helper

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class LocaleHelper(base: Context?) : ContextWrapper(base) {
    @Suppress("deprecation")
    fun wrap(context: Context, language: String): ContextWrapper {
        var context = context
        val config: Configuration = context.resources.configuration
        var sysLocale: Locale? = null
        sysLocale = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            getSystemLocale(config)
        } else {
            getSystemLocaleLegacy(config)
        }
        if (language != "" && sysLocale.language != language) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            setSystemLocale(config, locale)
        }
        context = context.createConfigurationContext(config)
        return LocaleHelper(context)
    }

    @Suppress("deprecation")
    fun getSystemLocaleLegacy(config: Configuration): Locale {
        return config.locale
    }

    fun getSystemLocale(config: Configuration): Locale {
        return config.getLocales().get(0)
    }

    @Suppress("deprecation")
    fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
        config.locale = locale
    }

    fun setSystemLocale(config: Configuration, locale: Locale?) {
        config.setLocale(locale)
    }
}