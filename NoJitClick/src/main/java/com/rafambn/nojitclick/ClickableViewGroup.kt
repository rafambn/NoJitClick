package com.rafambn.nojitclick

data class ClickableViewGroup(val groupId: Int, val isAsync: Boolean, var isClickable: Boolean, var minClickInterval: Long, var mutableListClickableView: MutableList<ClickableView>)
