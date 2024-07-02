package com.correct.correctsoc.helper;

import android.os.Bundle

interface ClickListener {
    fun onItemClickListener(position: Int, extras: Bundle?)
    fun onLongItemClickListener(position: Int, extras: Bundle?)
}