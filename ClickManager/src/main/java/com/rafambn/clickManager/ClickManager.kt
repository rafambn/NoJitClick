package com.rafambn.clickManager

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import java.lang.reflect.Field
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ClickManager is designed to prevent unintended double-click events.
 *
 * @constructor Creates a new instance of ClickManager and performs the necessary setup. It must be placed after all interfaces and listerners on view be set and before any manipulations of the interfaces.
 *
 * @param activity [Activity] - The Activity where the annotations are present.
 * @param numberParents [Int] - The number of parent Activities to check for annotations. This parameter is required if you are using a hierarchy and there are annotations on the parent classes.
 *
 * @see <a href="https://github.com/rafambn/ClickManager/">ClickManager</a>
 */
class ClickManager(activity: Activity, numberParents: Int = 0) {
    private val mClickables: MutableList<ClickableViewGroup>
    private val mHandler = Handler(Looper.getMainLooper())

    init {
        this.mClickables = ArrayList()
        var currentClass: Class<*>? = activity.javaClass
        var parentsChecked = 0
        while (currentClass != null && parentsChecked <= numberParents) {
            val fieldList = activity.javaClass.declaredFields
            for (field in fieldList) {
                field.isAccessible = true
                field.getAnnotation(ManageClick::class.java)?.let { markView ->
                    val groupId = markView.gruopId
                    val isAsync = markView.isAsync
                    val minClickInterval = markView.minClickInterval
                    val fieldType = field.type
                    mClickables.find { it.groupId == groupId } ?: run {
                        mClickables.add(ClickableViewGroup(groupId, AtomicBoolean(true)))
                    }
                    if (fieldType.isInterface) {
                        val originalObject = field.get(activity)
                        val proxyInstance = Proxy.newProxyInstance(fieldType.classLoader, arrayOf(fieldType)) { proxy, method, args ->
                            if (method.name == "onClick") {
                                mClickables.find { it.groupId == groupId }?.let { viewGroup ->
                                    if (viewGroup.isClickable.get()) {
                                        viewGroup.isClickable.set(false)
                                        method.invoke(originalObject, *args.orEmpty())
                                        if (!isAsync)
                                            mHandler.postDelayed({ viewGroup.isClickable.set(true) }, minClickInterval)
                                    }
                                }
                                null
                            } else {
                                method.invoke(originalObject, *args.orEmpty())
                            }
                        }
                        field.set(activity, proxyInstance)
                    } else {
                        val fieldView = field.get(activity) as View
                        getListener(fieldView)?.let { onClickListener ->
                            fieldView.setOnClickListener { view ->
                                mClickables.find { it.groupId == groupId }?.let { viewGroup ->
                                    if (viewGroup.isClickable.get()) {
                                        viewGroup.isClickable.set(false)
                                        onClickListener.onClick(view)
                                        if (!isAsync)
                                            mHandler.postDelayed({ viewGroup.isClickable.set(true) }, minClickInterval)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            currentClass = currentClass.superclass
            parentsChecked++
        }
    }

    private fun getListener(view: View): OnClickListener? {
        val listenerInfoFieldName = "mListenerInfo"
        val onCLickListenerFieldName = "mOnClickListener"

        val listenerInfo = getValueFromObject(view, listenerInfoFieldName)
        return listenerInfo?.let {
            getValueFromObject(it, onCLickListenerFieldName) as OnClickListener
        }
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

    /**
     * This method returns a [Runnable] that should be passed as a parameters to your async function and be executed when its finished.
     *
     * @param viewGroup [Int] - The id of the viewGroup associated with the view or interface being called.
     *
     * @return [Runnable]
     */
    fun getUnblocker(viewGroup: Int): Runnable {
        mClickables.find { it.groupId == viewGroup }?.let {
            return Runnable { it.isClickable.set(true) }
        } ?: run {
            throw IllegalStateException("There is no viewGroupId with this id = $viewGroup")
        }
    }
}