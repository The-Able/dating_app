package com.libra.tools

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TypefaceSpan
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.libra.R
import com.libra.support.TypefaceCache
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * @author Alex on 20.01.2019.
 */

fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
  compositeDisposable.add(this)
}

fun String.bold(boldPart: String, context: Context): SpannableString {
  val ss = SpannableString(this)
  val start = this.indexOf(boldPart)
  val end = start + boldPart.length
  if (start >= 0 && end <= this.length) {
    val typeface = TypefaceCache.getTypeface(context, TypefaceCache.GEO_MEDIUM)
    ss.setSpan(CustomTypefaceSpan("", typeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
  return ss
}