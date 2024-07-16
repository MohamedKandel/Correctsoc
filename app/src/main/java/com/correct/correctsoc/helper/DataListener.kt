package com.correct.correctsoc.helper

interface OnDataFetchedListener<T> {
    fun onAllDataFetched(data: MutableList<T>)
}

interface OnProgressUpdatedListener {
    fun onUpdateProgressLoad(progress: Int)
}