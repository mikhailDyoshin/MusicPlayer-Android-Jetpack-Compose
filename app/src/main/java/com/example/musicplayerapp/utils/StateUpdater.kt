package com.example.musicplayerapp.utils

import android.view.Choreographer

/**
The class is used to repeatedly execute client's logic witch he provides to the [StateUpdater]
using the [callBack] parameter. The [updatePeriodMillis] parameter defines the delay (in milliseconds)
between two closest (in time) calls of the user's logic.

 To control the class one can use the [start] and [stop] methods.
 */
class StateUpdater(
    private val callBack: () -> Unit,
    private val updatePeriodMillis: Long
) {

    private val choreographer = Choreographer.getInstance()

    private var isRunning = false

    private val extendedCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            callBack()
            if (isRunning) {
                choreographer.postFrameCallbackDelayed(this, updatePeriodMillis)
            }
        }
    }

    /**
     The [start] method starts the loop where client's logic is executed.
     */
    fun start() {
        if (!isRunning) {
            isRunning = true
            choreographer.postFrameCallbackDelayed(extendedCallback, updatePeriodMillis)
        }
    }

    /**
    The [stop] method stops the loop where client's logic is executed.
     */
    fun stop() {
        isRunning = false
    }

}