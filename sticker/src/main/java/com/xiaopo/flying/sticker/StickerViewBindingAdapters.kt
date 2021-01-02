package com.xiaopo.flying.sticker

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.*
import androidx.databinding.adapters.ListenerUtil


object StickerViewBindingAdapters {
    @SuppressLint("ClickableViewAccessibility")
    @JvmStatic
    @BindingAdapter("onTouchListener")
    fun setTouchListener(view: StickerView, callback: View.OnTouchListener) {
        view.setOnTouchListener(callback)
    }

    @JvmStatic
    @BindingAdapter("isLocked")
    fun setIsLocked(view: StickerView, value: Boolean) {
        view.isLocked = value
    }

    @JvmStatic
    @BindingAdapter("mustLockToPan")
    fun setMustLockToPan(view: StickerView, value: Boolean) {
        view.isMustLockToPan = value
    }

    @JvmStatic
    @BindingAdapter("rotationEnabled")
    fun setRotationEnabled(view: StickerView, value: Boolean) {
        view.isRotationEnabled = value
    }

    @JvmStatic
    @BindingAdapter("constrained")
    fun setConstrained(view: StickerView, value: Boolean) {
        view.isConstrained = value
    }

    @JvmStatic
    @BindingAdapter("isCropActive")
    fun setIsCropActive(view: StickerView, value: Boolean) {
        view.isCropActive = value
    }

    @JvmStatic
    @BindingAdapter("canvasMatrix")
    fun setCanvasMatrix(view: StickerView, value: ObservableMatrix) {
        view.canvasMatrix.setMatrix(value.getMatrix())
    }

    @JvmStatic
    @BindingAdapter("stickers")
    fun setStickers(view: StickerView, value: List<Sticker>) {
        view.stickers = value
    }

    @JvmStatic
    @BindingAdapter("activeIcons")
    fun setActiveIcons(view: StickerView, value: List<BitmapStickerIcon>) {
        view.setActiveIcons(value)
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "app:activeIcons")
    fun getActiveIcons(view: StickerView): List<BitmapStickerIcon> {
        return view.activeIcons.get()!!
    }

    @BindingAdapter("app:activeIconsAttrChanged")
    @JvmStatic fun setActiveIconListener(
        view: StickerView,
        attrChange: InverseBindingListener
    ) {
        val cb = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                attrChange.onChange()
            }
        }

        val oldValue = ListenerUtil.trackListener(view, cb, R.id.activeIconWatcher)
        if (oldValue != null) {
            view.activeIcons.removeOnPropertyChangedCallback(oldValue)
        }

        view.activeIcons.addOnPropertyChangedCallback(cb)
    }

    @JvmStatic
    @BindingAdapter("icons")
    fun setIcons(view: StickerView, value: List<BitmapStickerIcon>) {
        view.icons = value
    }

    @JvmStatic
    @BindingAdapter("handlingSticker")
    fun setHandlingSticker(view: StickerView, value: Sticker?) {
        view.handlingSticker = value
    }

    @JvmStatic
    @BindingAdapter("currentMode")
    fun setCurrentMode(view: StickerView, value: Int) {
        view.currentMode = value
    }

    @JvmStatic
    @BindingAdapter("currentIcon")
    fun setCurrentIcon(view: StickerView, value: BitmapStickerIcon?) {
        view.currentIcon = value
    }

    @JvmStatic
    @BindingAdapter("gestureDetector")
    fun setGestureDetector(view: StickerView, value: GestureListener?) {
        view.setGestureDetector(value)
    }
}
