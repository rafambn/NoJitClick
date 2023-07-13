package com.rafambn.nojitclick

import android.view.View
import android.view.View.OnClickListener

data class ClickableView(val view: View, val listener: OnClickListener, val isAsync: Boolean)