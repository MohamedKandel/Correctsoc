package com.correct.correctsoc.helper

import com.correct.correctsoc.data.AppInfo

interface PackageListener {
    fun onApplicationDeleted()
}

interface AppsFetchedListener {
    fun onAllAppsFetched(apps: MutableList<AppInfo>)
}