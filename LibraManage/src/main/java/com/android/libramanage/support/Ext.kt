package com.android.libramanage.support

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * @author Alex on 24.01.2019.
 */
fun Disposable.addTo(disposables: CompositeDisposable) {
  disposables.add(this)
}