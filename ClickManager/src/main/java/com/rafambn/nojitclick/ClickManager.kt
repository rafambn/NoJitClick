package com.rafambn.nojitclick

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import java.util.concurrent.atomic.AtomicBoolean

class ClickManager() : OnClickListener { //TODO optimize methods
    private val mClickables: MutableList<ClickableViewGroup>
    private val handler = Handler(Looper.getMainLooper())

    init {
        this.mClickables = ArrayList()
    }

    fun setListener( //TODO use notation instead of methods
        view: View,
        groupId: Int = 0,
        clickInterval: Long = 1000,
        isAsync: Boolean = false,
        listener: OnClickListener //TODO adapt to be used in recyclerView
    ): ClickManager {
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
                        clickableViewGroup.isClickable.set(false)
                        clickableView.listener.onClick(view)
                        if (!clickableView.isAsync)
                            handler.postDelayed({ clickableViewGroup.isClickable.set(true) }, clickableViewGroup.minClickInterval)
                    }
                }
    }
}