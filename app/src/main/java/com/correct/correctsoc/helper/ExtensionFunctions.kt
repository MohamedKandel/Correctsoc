package com.correct.correctsoc.helper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.text.Editable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


fun String.isContainsNumbers(): Boolean {
    var flag = false
    val numbers = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
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
    val special = listOf('@', '#', '$', '!', '&', '%')
    for (c in this) {
        if (c in special) {
            flag = true
            break
        }
    }
    return flag
}

fun String.isContainsUpperCase(): Boolean {
    var flag = false
    val special = listOf(
        'Z', 'X', 'C', 'V', 'B', 'N', 'M', 'A',
        'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'
    )
    for (c in this) {
        if (c in special) {
            flag = true
            break
        }
    }
    return flag
}

fun String.isContainsLowerCase(): Boolean {
    var flag = false
    val special = listOf(
        'z',
        'x',
        'c',
        'v',
        'b',
        'n',
        'm',
        'a',
        's',
        'd',
        'f',
        'g',
        'h',
        'j',
        'k',
        'l',
        'q',
        'w',
        'e',
        'r',
        't',
        'y',
        'u',
        'i',
        'o',
        'p'
    )
    for (c in this) {
        if (c in special) {
            flag = true
            break
        }
    }
    return flag
}

fun MutableLiveData<MutableList<Int>>.add(item: Int) {
    val currentList = this.value
    currentList?.add(item)
    this.value = currentList
}

fun MutableLiveData<MutableList<Int>>.remove(item: Int) {
    val currentList = this.value
    currentList?.remove(item)
    this.value = currentList
}

fun MutableLiveData<MutableList<Int>>.clear() {
    this.value?.clear()
}

fun MutableLiveData<MutableList<Int>>.contains(item: Int): Boolean {
    return this.value?.contains(item) ?: false
}

fun String.reMappingNumbers():String {
    var str = ""
    val map = mapOf(
        '١' to "1",
        '٢' to "2",
        '٣' to "3",
        '٤' to "4",
        '٥' to "5",
        '٦' to "6",
        '٧' to "7",
        '٨' to "8",
        '٩' to "9",
        '٠' to "0"
    )
    for (c in this) {
        if (c in map.keys) {
            str += map[c]
        } else {
            str += c
        }
    }
    return str
}

fun String.mappingNumbers(): String {
    var str = ""
    val map = mapOf(
        '1' to "١",
        '2' to "٢",
        '3' to "٣",
        '4' to "٤",
        '5' to "٥",
        '6' to "٦",
        '7' to "٧",
        '8' to "٨",
        '9' to "٩",
        '0' to "٠"
    )
    for (c in this) {
        if (c in map.keys) {
            str += map[c]
        } else {
            str += c
        }
    }
    return str
}

fun TextView.gradientText(startColor: Int, endColor: Int) {
    val paint = this.paint
    val width = paint.measureText(this.text.toString())
    val shader = LinearGradient(
        0f, 0f, 0f, this.textSize,
        intArrayOf(startColor, endColor),
        floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
    )
    this.paint.shader = shader
}

fun TextView.setSpannable(
    startIndex: Int,
    endIndex: Int,
    text: String,
    color: Int,
    executableFun: () -> Unit
) {
    val spannableString = SpannableString(text)
    val clickableSpan: ClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            executableFun()
        }
    }

    spannableString.setSpan(clickableSpan, startIndex, endIndex, 0)
    spannableString.setSpan(
        android.text.style.ForegroundColorSpan(color),
        startIndex,
        endIndex,
        0
    )

    this.text = spannableString
}

fun TextView.colorfulTextView(
    startIndex: Int,
    endIndex: Int,
    color: Int
) {
    val spannableString = SpannableString(this.text)
    val colorSpan = ForegroundColorSpan(color)

    // Apply the color span to the spannable string
    spannableString.setSpan(
        colorSpan, startIndex, endIndex,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    this.text = spannableString
    //return spannableString
}

fun TextView.updateRequirements(
    password: String,
    startIndex: Array<Int>,
    endIndx: Array<Int>,
    text: String,
    checkedColor: Int,
    unCheckedColor: Int
): Boolean {

    var allRequirement = true
    val requirementsText = text
    val spannableString = SpannableString(requirementsText)

    if (password.length >= 6) {
        val start = startIndex[0]
        val end = endIndx[0]
        spannableString.setSpan(
            ForegroundColorSpan(checkedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

    } else {
        val start = startIndex[0]
        val end = endIndx[0]
        spannableString.setSpan(
            ForegroundColorSpan(unCheckedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        allRequirement = false
    }

    if (password.any { it.isDigit() }) {
        val start = startIndex[1]
        val end = endIndx[1]
        spannableString.setSpan(
            ForegroundColorSpan(checkedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

    } else {
        val start = startIndex[1]
        val end = endIndx[1]
        spannableString.setSpan(
            ForegroundColorSpan(unCheckedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        allRequirement = false
    }

    if (password.any { it.isUpperCase() }) {
        val start = startIndex[2]
        val end = endIndx[2]
        spannableString.setSpan(
            ForegroundColorSpan(checkedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

    } else {
        val start = startIndex[2]
        val end = endIndx[2]
        spannableString.setSpan(
            ForegroundColorSpan(unCheckedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        allRequirement = false
    }

    if (password.any { it.isLowerCase() }) {
        val start = startIndex[3]
        val end = endIndx[3]
        spannableString.setSpan(
            ForegroundColorSpan(checkedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

    } else {
        val start = startIndex[3]
        val end = endIndx[3]
        spannableString.setSpan(
            ForegroundColorSpan(unCheckedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        allRequirement = false
    }

    if (password.any { !it.isLetterOrDigit() }) {
        val start = startIndex[4]
        val end = endIndx[4]
        spannableString.setSpan(
            ForegroundColorSpan(checkedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

    } else {
        val start = startIndex[4]
        val end = endIndx[4]
        spannableString.setSpan(
            ForegroundColorSpan(unCheckedColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        allRequirement = false
    }

    this.text = spannableString
    return allRequirement
}

fun AlertDialog.Builder.buildDialog(
    title: String,
    msg: String,
    icon: Int? = null,
    positiveButton: String,
    positiveButtonFunction: () -> Unit,
    negativeButton: String,
    negativeButtonFunction: () -> Unit
) {
    val dialog = AlertDialog.Builder(this.context)
    dialog.setCancelable(false)
    dialog.setTitle(title)
    dialog.setMessage(msg)
    if (icon != null) {
        dialog.setIcon(icon)
    }
    dialog.setPositiveButton(positiveButton) { dialog, which ->
        dialog.dismiss()
        dialog.cancel()
        positiveButtonFunction()
    }
    dialog.setNegativeButton(negativeButton) { dialog, which ->
        dialog.dismiss()
        dialog.cancel()
        negativeButtonFunction()
    }
    dialog.setCancelable(false)
    dialog.show()
}

fun EditText.appendFilter(newFilter: InputFilter) {
    val editFilters = this.filters
    val newFilters = editFilters.copyOf(editFilters.size + 1)
    System.arraycopy(editFilters, 0, newFilters, 0, editFilters.size)
    newFilters[editFilters.size] = newFilter
    this.filters = newFilters
}

fun EditText.upperCaseOnly() {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            this@upperCaseOnly.removeTextChangedListener(this) // Remove listener to prevent infinite loop
            s?.let {
                val upperCaseText = it.toString().uppercase()
                if (upperCaseText != it.toString()) {
                    this@upperCaseOnly.setText(upperCaseText)
                    this@upperCaseOnly.setSelection(upperCaseText.length) // Set cursor to end of text
                }
            }
            this@upperCaseOnly.addTextChangedListener(this) // Re-attach listener
        }
    })
}

fun String.parseBase64(): Bitmap {
    val decodedString: ByteArray = Base64.decode(this, Base64.DEFAULT)
    val decodedByte: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    return decodedByte
}

fun View.hideKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Fragment.openWhatsApp(tel: String) {
    val url = "https://api.whatsapp.com/send?phone=$tel"
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setData(Uri.parse(url))
    this.startActivity(intent)
}

fun View.hide(){
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun TextView.getDisplayedText(): String {
    val layout = this.layout
    val lines = this.lineCount.coerceAtMost(this.maxLines)
    val displayedText = StringBuilder()
    for (i in 0 until lines) {
        val start = layout.getLineStart(i)
        val end = layout.getLineEnd(i)
        val lineText = this.text.substring(start, end)
        displayedText.append(lineText)
    }
    return displayedText.toString()
}

fun Context.readFile(fileRes: Int): String {
    val inputStram = this.resources.openRawResource(fileRes)
    val bufferedReader = BufferedReader(InputStreamReader(inputStram))
    return bufferedReader.use { it.readText() }
}