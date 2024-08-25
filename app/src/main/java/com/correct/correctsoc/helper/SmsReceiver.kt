package com.correct.correctsoc.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsReceiver : BroadcastReceiver() {

    var listener: SmsReceiverListener ?= null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras?.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
            } else {
                extras?.getParcelable<Status>(SmsRetriever.EXTRA_STATUS)
            }
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val messageIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        extras?.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT, Intent::class.java)
                    } else {
                        extras?.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    }
                    listener?.onSuccess(messageIntent)
                }

                CommonStatusCodes.TIMEOUT -> {
                    listener?.onFailure()
                }
            }
        }
    }

    interface SmsReceiverListener {
        fun onSuccess(intent: Intent?)
        fun onFailure()
    }
}