package com.rafambn.nojitclick


import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener

class MyOnClickListener : OnClickListener {
    private var mIsClickable = true
    private val mClickables: MutableList<ClickableView>
    private val handler = Handler(Looper.getMainLooper())

    constructor(viewIndependence: Int) {
        if (viewIndependence == NOT_INDEPENDENT || viewIndependence == PARTIALLY_INDEPENDENT || viewIndependence == FULLY_INDEPENDENT)
            sViewIndependence = viewIndependence

        this.mClickables = ArrayList()
    }

    constructor() : this(NOT_INDEPENDENT)

    fun setClickable(view: View, listener: OnSingleClickListener): MyOnClickListener {
        this.mClickables.add(ClickableView(view, listener))
        return this
    }

    fun setClickable(view: View, listener: OnSingleClickListener, isIndependent: Boolean): MyOnClickListener {
        this.mClickables.add(ClickableView(view, listener, true, isIndependent))
        return this
    }

    fun setMinClickInterval(interval: Long): MyOnClickListener {
        sMinClickInterval = interval
        return this
    }

    override fun onClick(view: View) {
        when (sViewIndependence) {
            NOT_INDEPENDENT -> if (mIsClickable) {
                for (clickable in mClickables) {
                    if (clickable.view === view) {
                        clickable.listener.onSingleClick(view)
                        mIsClickable = false
                        handler.postDelayed({ mIsClickable = true }, sMinClickInterval)
                        break
                    }
                }
            }

            PARTIALLY_INDEPENDENT -> for (clickable in mClickables) {
                if (clickable.view === view) {
                    if (clickable.isIndependent) {
                        if (!clickable.isClickable) return

                        clickable.listener.onSingleClick(view)
                        clickable.isClickable = false
                        handler.postDelayed({ clickable.isClickable = true }, sMinClickInterval)
                        break
                    } else {
                        if (!mIsClickable) return

                        clickable.listener.onSingleClick(view)
                        mIsClickable = false
                        handler.postDelayed({ mIsClickable = true }, sMinClickInterval)
                        break
                    }
                }
            }

            FULLY_INDEPENDENT -> for (clickable in mClickables) {
                if (clickable.view === view) {
                    if (!clickable.isClickable) return

                    clickable.listener.onSingleClick(view)
                    clickable.isClickable = false
                    handler.postDelayed({ clickable.isClickable = true }, sMinClickInterval)
                    break
                }
            }
        }
    }

    companion object {
        private var sMinClickInterval = 1000L
        private var sViewIndependence = 0
        private const val NOT_INDEPENDENT = 0
        private const val PARTIALLY_INDEPENDENT = 1
        private const val FULLY_INDEPENDENT = 2
    }
}