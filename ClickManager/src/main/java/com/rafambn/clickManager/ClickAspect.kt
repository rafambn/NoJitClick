package com.rafambn.clickManager

import android.app.Activity
import android.view.View
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

@Aspect
class ClickAspect {
    @Around("execution(* android.view.View.OnClickListener.onClick(..)) && args(view)")
    @Throws(Throwable::class)
    fun onClick(joinPoint: ProceedingJoinPoint, view: View) {
        val activity = view.context as Activity
        val clickManagerFields = activity.javaClass.declaredFields.filter { it.isAnnotationPresent(Manager::class.java) }
        if (clickManagerFields.size != 1) {
            throw IllegalStateException("Expected exactly one field annotated with @Manager")
        }
        val clickManagerField = clickManagerFields[0]
        clickManagerField.isAccessible = true
        val clickManager = clickManagerField.get(activity) as ClickManager

        for (clickableViewGroup in clickManager.mClickables) {
            for (clickableView in clickableViewGroup.mutableListClickableView) {
                if (clickableView.view == view) {
                    if (clickableViewGroup.isClickable.get()) {
                        clickableViewGroup.isClickable.set(false)
                        joinPoint.proceed()
                        if (!clickableView.isAsync) {
                            clickManager.mHandler.postDelayed({ clickableViewGroup.isClickable.set(true) }, clickableView.minClickInterval)
                        }
                    }
                    return
                }
            }
        }
    }
}