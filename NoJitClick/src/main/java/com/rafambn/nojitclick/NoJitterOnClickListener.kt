package com.rafambn.nojitclick

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import java.util.concurrent.atomic.AtomicBoolean

class NoJitterOnClickListener : OnClickListener {
    private val mClickables: MutableList<ClickableViewGroup>
    private val handler = Handler(Looper.getMainLooper())

    constructor() {
        this.mClickables = ArrayList()
    }

    fun setListener(view: View, groupId: Int = 0, clickInterval: Long = 1000, isAsync: Boolean = false, listener: OnSingleClickListener): NoJitterOnClickListener {
        for (clickableViewGroup in this.mClickables)
            if (clickableViewGroup.groupId == groupId) {
                clickableViewGroup.mutableListClickableView.add(ClickableView(view, listener, isAsync))
                return this
            }
        this.mClickables.add(
            ClickableViewGroup(
                groupId,
                AtomicBoolean(true),
                clickInterval,
                arrayListOf(ClickableView(view, listener, isAsync))
            )
        )
        return this
    }

    fun setClickInterval(interval: Long, groupId: Int) {
        for (clickableViewGroup in this.mClickables)
            if (clickableViewGroup.groupId == groupId)
                clickableViewGroup.minClickInterval = interval
    }

    fun getUnblocker(view: View): Runnable {
        return Runnable {
            for (clickableViewGroup in this.mClickables)
                for (clickableView in clickableViewGroup.mutableListClickableView)
                    if (clickableView.view == view)
                        clickableViewGroup.isClickable.set(true)
        }
    }

    override fun onClick(view: View) {
        for (clickableViewGroup in this.mClickables)
            for (clickableView in clickableViewGroup.mutableListClickableView)
                if (clickableView.view == view) {
                    if (clickableViewGroup.isClickable.get()) {
                        if (clickableView.isAsync) {
                            clickableViewGroup.isClickable.set(false)
                            clickableView.listener.onSingleClick(view)
                        } else {
                            clickableViewGroup.isClickable.set(false)
                            clickableView.listener.onSingleClick(view)
                            handler.postDelayed({ clickableViewGroup.isClickable.set(true) }, clickableViewGroup.minClickInterval)
                        }
                    }
                }
    }
}