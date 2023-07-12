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
        this.mClickables = arrayListOf(ClickableViewGroup(0, false, AtomicBoolean(true), 1000, ArrayList()))
    }

    fun setListener(view: View, listener: OnSingleClickListener): NoJitterOnClickListener {
        setListener(view, listener, 0, 1000)
        return this
    }

    fun setListener(view: View, listener: OnSingleClickListener, groupId: Int): NoJitterOnClickListener {
        setListener(view, listener, groupId, 1000)
        return this
    }

    fun setListener(view: View, listener: OnSingleClickListener, groupId: Int, clickInterval: Long): NoJitterOnClickListener {
        for (clickableViewGroup in this.mClickables)
            if (clickableViewGroup.groupId == groupId) {
                clickableViewGroup.mutableListClickableView.add(ClickableView(view, listener))
                return this
            }
        this.mClickables.add(
            ClickableViewGroup(
                groupId,
                false,
                AtomicBoolean(true),
                clickInterval,
                arrayListOf(ClickableView(view, listener))
            )
        )
        return this
    }

    fun setAsyncListener(view: View, listener: OnSingleClickListener, groupId: Int): NoJitterOnClickListener? {
        for (clickableViewGroup in this.mClickables)
            if (clickableViewGroup.groupId == groupId) {
                if (!clickableViewGroup.isAsync)
                    return null
                clickableViewGroup.mutableListClickableView.add(ClickableView(view, listener))
                return this
            }
        this.mClickables.add(ClickableViewGroup(groupId, true, AtomicBoolean(true), 1, arrayListOf(ClickableView(view, listener))))
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
                    if (clickableViewGroup.isAsync) {
                        if (clickableViewGroup.isClickable.get()) {
                            clickableViewGroup.isClickable.set(false)
                            clickableView.listener.onSingleClick(view)
                        }
                    } else {
                        if (clickableViewGroup.isClickable.get()) {
                            clickableViewGroup.isClickable.set(false)
                            clickableView.listener.onSingleClick(view)
                            handler.postDelayed({ clickableViewGroup.isClickable.set(true) }, clickableViewGroup.minClickInterval)
                        }
                    }
                }
    }
}