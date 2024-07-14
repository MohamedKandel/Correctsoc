package com.correct.correctsoc.Retrofit

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import com.correct.correctsoc.ui.auth.AuthRepository

class APIWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val repository = AuthRepository(
        RetrofitClient.getClient()
            .create(APIService::class.java)
    )

    override suspend fun doWork(): Result {
        return try {
            val token = inputData.getString(TOKEN_KEY) ?: return Result.failure()
            val response = repository.setDeviceOff(token)
            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (ex: Exception) {
            Log.e("Exception", "doWork: ", ex)
            Result.retry()
        }
    }
}