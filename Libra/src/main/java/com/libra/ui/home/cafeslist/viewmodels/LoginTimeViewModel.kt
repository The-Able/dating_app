package com.libra.ui.home.cafeslist.viewmodels

import androidx.lifecycle.MutableLiveData
import com.libra.Constants
import com.libra.base.BaseViewModel
import com.libra.tools.LoginTimesHelper
import com.libra.tools.addTo
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * @author Alex on 21.01.2019.
 */
class LoginTimeViewModel : BaseViewModel() {

    private val loginEnabledSubject = PublishSubject.create<Boolean>()
    private val loginWindows: List<Pair<Calendar, Calendar>> = LoginTimesHelper.loginTimes.map(::createCalendarWindow)
    private lateinit var nextLoginWindow: Pair<Calendar, Calendar>

    val isLoginEnabled = MutableLiveData<Boolean>()
    val timeLeft = MutableLiveData<Long>()

    init {
        loginEnabledSubject
                .distinctUntilChanged()
                .subscribe({ isLoginEnabled.postValue(it) }, Throwable::printStackTrace)
                .addTo(disposables)
        loginEnabledSubject.onNext(false)
        calculateNextLoginWindow()
        runTimer()
    }

    private fun calculateNextLoginWindow() {
        val now = Date()
        //Try to find current open window
        loginWindows.forEach { (start, end) ->
            if (now.after(start.time) && now.before(end.time)) {
                nextLoginWindow = start to end
                return
            }
        }

        while (true) {
            val next = loginWindows.firstOrNull { (start, _) -> start.time.after(now) }
            if (next == null) {
                loginWindows.forEach { (start, end) ->
                    start.add(Calendar.DAY_OF_YEAR, 1)
                    end.add(Calendar.DAY_OF_YEAR, 1)
                }
            } else {
                nextLoginWindow = next
                return
            }
        }
    }

    private fun runTimer() {
        Flowable.interval(0, 1, TimeUnit.SECONDS)
                .map {
                    val now = Date()
                    val (start, end) = nextLoginWindow
                    if (now.after(start.time) && now.before(end.time)) {
                        isLoginEnabled.postValue(true)
                        return@map 0L
                    } else if (now.after(end.time)) {
                        isLoginEnabled.postValue(true)
                        calculateNextLoginWindow()
                        return@map 0L
                    } else {
                        isLoginEnabled.postValue(false)
                        return@map start.time.time - now.time
                    }
                }
                .onErrorResumeNext { t: Throwable -> Flowable.just(0L) }
                .subscribe({ timeLeft.postValue(it) }, Throwable::printStackTrace)
                .addTo(disposables)
    }

    private fun createCalendarWindow(calendarStart: Calendar): Pair<Calendar, Calendar> {
        val calendarEnd = Calendar.getInstance().apply{ time = calendarStart.time }
        calendarEnd.set(Calendar.MINUTE, 30)
        return calendarStart to calendarEnd
    }
}