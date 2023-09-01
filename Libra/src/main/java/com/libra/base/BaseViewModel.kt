package com.libra.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 * @author Alex on 20.01.2019.
 */
abstract class BaseViewModel : ViewModel() {

  protected val disposables: CompositeDisposable = CompositeDisposable()

  override fun onCleared() {
    disposables.clear()
    super.onCleared()
  }
}