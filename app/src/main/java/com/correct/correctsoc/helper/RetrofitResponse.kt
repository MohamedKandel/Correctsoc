package com.correct.correctsoc.helper

interface RetrofitResponse<T> {
    fun onSuccess(response: T)
    fun onFailed(error: String)
}