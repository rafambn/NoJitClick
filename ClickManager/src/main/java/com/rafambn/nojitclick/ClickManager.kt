package com.rafambn.nojitclick

import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicBoolean

class ClickManager {
    private val mClickables: MutableList<ClickableViewGroup>

    init {
        this.mClickables = ArrayList()
    }

    fun injectViews(activity: Any) {
        val fields = activity.javaClass.declaredFields
        for (field in fields) {
            if (field.isAnnotationPresent(ManageView::class.java)) {
                try {
                    field.isAccessible = true
                    val view = field.get(activity)
                    val markView = field.getAnnotation(ManageView::class.java)
                    val groupId = markView!!.gruopId
                    val isAsync = markView.isAsync
                    val minClickInterval = markView.minClickInterval
                    val listener = getListener(view as View)
                    mClickables.find { it.groupId == groupId }?.mutableListClickableView?.add(ClickableView(view, listener, isAsync, minClickInterval))
                        ?: run {
                        mClickables.add(ClickableViewGroup(groupId, AtomicBoolean(true), arrayListOf(ClickableView(view, listener, isAsync, minClickInterval))))
                    }
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getListener(view: View): OnClickListener {
        return ReflectionUtils.getOnClickListener(view)!!
    }

    object ReflectionUtils {
        private const val listenerInfoFieldName = "mListenerInfo"
        private const val onCLickListenerFieldName = "mOnClickListener"
        fun getOnClickListener(view: View): OnClickListener? {
            val listenerInfo = getValueFromObject(view, listenerInfoFieldName, Any::class.java)
            return listenerInfo?.let {
                getValueFromObject(
                    it, onCLickListenerFieldName,
                    OnClickListener::class.java
                )
            }
        }

        private fun <T> getValueFromObject(`object`: Any, fieldName: String, returnClazz: Class<T>): T? {
            return getValueFromObject(`object`, `object`.javaClass, fieldName, returnClazz)
        }

        private fun <T> getValueFromObject(`object`: Any, declaredFieldClass: Class<*>, fieldName: String, returnClazz: Class<T>): T? {
            try {
                val field: Field = declaredFieldClass.getDeclaredField(fieldName)
                field.isAccessible = true
                val value: Any = field.get(`object`) as Any
                return returnClazz.cast(value)
            } catch (e: NoSuchFieldException) {
                val superClass = declaredFieldClass.superclass
                if (superClass != null) {
                    return getValueFromObject(`object`, superClass, fieldName, returnClazz)
                }
            } catch (e: IllegalAccessException) {
                Log.e("fudeo", e.message.toString())
            }
            return null
        }
    }

//    override fun onClick(view: View) {
//        for (clickableViewGroup in this.mClickables)
//            for (clickableView in clickableViewGroup.mutableListClickableView)
//                if (clickableView.view == view) {
//                    if (clickableViewGroup.isClickable.get()) {
//                        clickableViewGroup.isClickable.set(false)
//                        clickableView.listener.onClick(view)
//                        if (!clickableView.isAsync)
//                            handler.postDelayed({ clickableViewGroup.isClickable.set(true) }, clickableViewGroup.minClickInterval)
//                    }
//                }
//    }
}