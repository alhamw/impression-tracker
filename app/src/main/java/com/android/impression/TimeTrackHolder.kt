package com.android.impression

import android.os.CountDownTimer

/**
 *   Created by Alham Wa on 2019-08-30
 */
class TimeTrackHolder(val position: Int, listener: () -> Unit) {
    var isTracked: Boolean = false
    var onTracking: Boolean = false

    private var countDownTimer: CountDownTimer = object : CountDownTimer(20000, 1000) {
        override fun onFinish() {
            isTracked = true
            onTracking = false
            listener()
        }

        override fun onTick(millisUntilFinished: Long) {
            //TODO: Nothing
        }
    }

    init {
        start()
    }

    fun start() {
        onTracking = true
        countDownTimer.start()
    }

    fun stop() {
        onTracking = false
        countDownTimer.cancel()
    }
}