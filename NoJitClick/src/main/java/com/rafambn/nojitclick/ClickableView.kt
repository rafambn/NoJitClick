package com.rafambn.nojitclick

import android.view.View

class ClickableView(val view: View, val listener: OnSingleClickListener, var isClickable: Boolean, var isIndependent: Boolean) {

    constructor(view: View, listener: OnSingleClickListener) : this(view, listener, true)

    constructor(view: View, listener: OnSingleClickListener, isClickable: Boolean) : this(view, listener, isClickable, false)

}