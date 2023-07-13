package com.rafambn.nojitclick

import android.view.View

data class ClickableView(val view: View, val listener: OnSingleClickListener, val isAsync: Boolean)