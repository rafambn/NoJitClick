package com.rafambn.clickManager

import android.view.View
import android.view.View.OnClickListener

data class ClickableView(val view: View, val listener: OnClickListener, val isAsync: Boolean, val minClickInterval: Long)