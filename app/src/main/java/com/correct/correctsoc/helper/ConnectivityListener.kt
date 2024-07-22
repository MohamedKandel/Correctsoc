package com.correct.correctsoc.helper

import kotlinx.coroutines.flow.Flow

interface ConnectivityListener {
    fun onConnectionStatusChangedListener(): Flow<Status>

    enum class Status {
        AVAILABLE, UNAVAILABLE, LOST, LOSING
    }
}