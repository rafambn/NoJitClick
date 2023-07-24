package com.rafambn.clickManager

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import java.lang.reflect.Field
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean

class ClickManager(activity: Activity) {
    private val mClickables: MutableList<ClickableViewGroup>
    private val mHandler = Handler(Looper.getMainLooper())

    init {
        this.mClickables = ArrayList()
        val fieldList = activity.javaClass.declaredFields
        for (field in fieldList) {
            field.isAccessible = true
            if (field.isAnnotationPresent(ManageView::class.java)) {
                field.getAnnotation(ManageView::class.java)?.let { markView ->
                    val view = field.get(activity) as View
                    val groupId = markView.gruopId
                    val isAsync = markView.isAsync
                    val minClickInterval = markView.minClickInterval
                    val listener = getListener(view)
                    mClickables.find { it.groupId == groupId }?.let { } ?: run {
                        mClickables.add(
                            ClickableViewGroup(
                                groupId,
                                AtomicBoolean(true)
                            )
                        )
                    }
                    view.setOnClickListener { view2 ->
                        val clickableViewGroup = mClickables.find { it.groupId == groupId }!!
                        if (clickableViewGroup.isClickable.get()) {
                            clickableViewGroup.isClickable.set(false)
                            listener.onClick(view2)
                            if (!isAsync)
                                mHandler.postDelayed({ clickableViewGroup.isClickable.set(true) }, minClickInterval)
                        }
                    }
                }
            } else if (field.isAnnotationPresent(ManageListener::class.java)) {
                val fieldType = field.type
                if (fieldType.isInterface) {
                    field.getAnnotation(ManageListener::class.java)?.let { markView ->
                        val groupId = markView.gruopId
                        val isAsync = markView.isAsync
                        val minClickInterval = markView.minClickInterval
                        val proxyInstance = Proxy.newProxyInstance(fieldType.classLoader, arrayOf(fieldType)) { proxy, method, args ->
                            {
                                if (method.name == "onClick") {
                                    val clickableViewGroup = mClickables.find { it.groupId == groupId }!!
                                    if (clickableViewGroup.isClickable.get()) {
                                        clickableViewGroup.isClickable.set(false)
                                        if (!isAsync)
                                            mHandler.postDelayed({ clickableViewGroup.isClickable.set(true) }, minClickInterval)
                                        method.invoke(proxy, *args.orEmpty())
                                    } else
                                        null
                                } else
                                    method.invoke(proxy, *args.orEmpty())
                            }
                        }
                        field.set(activity, proxyInstance)
                    }
                }
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