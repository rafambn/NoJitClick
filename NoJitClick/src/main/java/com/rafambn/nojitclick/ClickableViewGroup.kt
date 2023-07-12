package com.rafambn.nojitclick

import java.util.concurrent.atomic.AtomicBoolean

data class ClickableViewGroup(val groupId: Int, val isAsync: Boolean, var isClickable: AtomicBoolean, var minClickInterval: Long, var mutableListClickableView: MutableList<ClickableView>)
