package com.rafambn.clickManager

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicBoolean

class ClickManager(activity: Any) {
    internal val mClickables: MutableList<ClickableViewGroup>
    internal val mHandler = Handler(Looper.getMainLooper())

    init {
        this.mClickables = ArrayList()
        val fields = activity.javaClass.declaredFields.filter { it.isAnnotationPresent(ManageView::class.java) }
        for (field in fields) {
            try {
                field.isAccessible = true
                val view = field.get(activity)
                field.getAnnotation(ManageView::class.java)?.let { markView ->
                    val groupId = markView.gruopId
                    val isAsync = markView.isAsync
                    val minClickInterval = markView.minClickInterval
                    val listener = getListener(view as View)
                    mClickables.find { it.groupId == groupId }?.mutableListClickableView?.add(
                        ClickableView(
                            view,
                            listener,
                            isAsync,
                            minClickInterval
                        )
                    )
                        ?: run {
                            mClickables.add(
                                ClickableViewGroup(
                                    groupId,
                                    AtomicBoolean(true),
                                    arrayListOf(ClickableView(view, listener, isAsync, minClickInterval))
                                )
                            )
                        }
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    private fun getListener(view: View): OnClickListener {
        val listenerInfoFieldName = "mListenerInfo"
        val onCLickListenerFieldName = "mOnClickListener"

        val listenerInfo = getValueFromObject(view, listenerInfoFieldName)
        return listenerInfo?.let {
            getValueFromObject(it, onCLickListenerFieldName) as OnClickListener
        }!!
    }

    private fun getValueFromObject(`object`: Any, fieldName: String): Any? {
        var declaredFieldClass: Class<*>? = `object`.javaClass
        while (declaredFieldClass != null) {
            try {
                val field: Field = declaredFieldClass.getDeclaredField(fieldName)
                field.isAccessible = true
                return field.get(`object`)
            } catch (e: NoSuchFieldException) {
                declaredFieldClass = declaredFieldClass.superclass
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun getUnblocker(viewGroup: Int): Runnable {
        mClickables.find { it.groupId == viewGroup }?.let {
            return Runnable { it.isClickable.set(true) }
        } ?: run {
            throw IllegalStateException("There is no viewGroupId with this id = $viewGroup")
        }
    }
}