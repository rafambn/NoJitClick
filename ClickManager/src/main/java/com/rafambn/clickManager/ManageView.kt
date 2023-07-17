package com.rafambn.clickManager

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ManageView(val gruopId: Int = 0, val isAsync: Boolean = false, val minClickInterval: Long = 1000)
