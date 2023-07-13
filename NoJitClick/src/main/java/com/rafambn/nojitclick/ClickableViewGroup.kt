package com.rafambn.nojitclick

import java.util.concurrent.atomic.AtomicBoolean

data class ClickableViewGroup(val groupId: Int, var isClickable: AtomicBoolean, var minClickInterval: Long, var mutableListClickableView: MutableList<ClickableView>)
