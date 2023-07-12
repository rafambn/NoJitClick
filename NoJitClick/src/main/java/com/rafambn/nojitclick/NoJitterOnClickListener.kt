package com.rafambn.nojitclick


import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener

class NoJitterOnClickListener : OnClickListener {
    private val mClickables: MutableList<ClickableViewGroup>
    private val handler = Handler(Looper.getMainLooper())

    constructor() {
        this.mClickables = arrayListOf(ClickableViewGroup(0, false, true, 1000, ArrayList()))
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
        var hasGroupId = false
        for (clickableViewGroup in mClickables)
            if (clickableViewGroup.groupId == groupId) {
                clickableViewGroup.mutableListClickableView.add(ClickableView(view, listener))
                hasGroupId = true
            }
        if (!hasGroupId)
            this.mClickables.add(ClickableViewGroup(groupId, false, true, clickInterval, arrayListOf(ClickableView(view, listener))))
        return this
    }

    fun setAsyncListener(view: View, listener: OnSingleClickListener, groupId: Int): NoJitterOnClickListener? {
        var hasGroupId = false
        for (clickableViewGroup in mClickables)
            if (clickableViewGroup.groupId == groupId) {
                if (!clickableViewGroup.isAsync)
                    return null
                clickableViewGroup.mutableListClickableView.add(ClickableView(view, listener))
                hasGroupId = true
            }
        if (!hasGroupId)
            this.mClickables.add(ClickableViewGroup(groupId, true, true, 1, arrayListOf(ClickableView(view, listener))))
        return this
    }

    fun setClickInterval(interval: Long, groupId: Int) {
        for (clickableViewGroup in mClickables)
            if (clickableViewGroup.groupId == groupId)
                clickableViewGroup.minClickInterval = interval
    }

    fun finishClick(view: View) {
        for (clickableViewGroup in mClickables)
            for (clickableView in clickableViewGroup.mutableListClickableView)
                if (clickableView.view == view)
                    clickableViewGroup.isClickable = true
    }

    override fun onClick(view: View) {
        for (clickableViewGroup in mClickables)
            for (clickableView in clickableViewGroup.mutableListClickableView)
                if (clickableView.view == view) {
                    if (clickableViewGroup.isAsync) {
                        if (clickableViewGroup.isClickable) {
                            clickableView.listener.onSingleClick(view)
                            clickableViewGroup.isClickable = false
                        }
                    } else {
                        if (clickableViewGroup.isClickable) {
                            clickableView.listener.onSingleClick(view)
                            clickableViewGroup.isClickable = false
                            handler.postDelayed({ clickableViewGroup.isClickable = true }, clickableViewGroup.minClickInterval)
                        }
                    }
                }
    }
}