package com.correct.correctsoc.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DeleteReceiver(private val listener: PackageListener) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        listener.onApplicationDeleted()
    }
}