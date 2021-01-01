package xyz.ruin.droidref

import android.view.MotionEvent
import android.view.View
import androidx.databinding.BindingAdapter

object Adapters {
    @JvmStatic
    @BindingAdapter("onTouchListener")
    fun setTouchListener(view: View, callback: View.OnTouchListener) {
        view.setOnTouchListener(callback)
    }
}