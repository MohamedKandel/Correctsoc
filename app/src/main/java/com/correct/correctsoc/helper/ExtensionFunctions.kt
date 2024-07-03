package com.correct.correctsoc.helper

fun String.isContainsNumbers(): Boolean {
    var flag = false
    val numbers = "0123456789"
    for (c in this) {
        if (c in numbers) {
            flag = true
            break
        }
    }
    return flag
}

fun String.isContainsSpecialCharacter(): Boolean {
    var flag = false
    val special = "@#$!&%"
    for (c in this) {
        if (c in special) {
            flag = true
            break
        }
    }
    return flag
}