package xyz.ruin.droidref

import android.graphics.Matrix
import android.view.View
import androidx.databinding.BindingAdapter
import com.xiaopo.flying.sticker.BitmapStickerIcon
import com.xiaopo.flying.sticker.ObservableMatrix
import com.xiaopo.flying.sticker.Sticker
import com.xiaopo.flying.sticker.StickerView

object Adapters {
    @JvmStatic
    @BindingAdapter("onTouchListener")
    fun setTouchListener(view: View, callback: View.OnTouchListener) {
        view.setOnTouchListener(callback)
    }

    @JvmStatic
    @BindingAdapter("isLocked")
    fun setIsLocked(view: View, value: Boolean) {
        (view as StickerView).isLocked = value
    }

    @JvmStatic
    @BindingAdapter("mustLockToPan")
    fun setMustLockToPan(view: View, value: Boolean) {
        (view as StickerView).isMustLockToPan = value
    }

    @JvmStatic
    @BindingAdapter("rotationEnabled")
    fun setRotationEnabled(view: View, value: Boolean) {
        (view as StickerView).isRotationEnabled = value
    }

    @JvmStatic
    @BindingAdapter("constrained")
    fun setConstrained(view: View, value: Boolean) {
        (view as StickerView).isConstrained = value
    }

    @JvmStatic
    @BindingAdapter("isCropActive")
    fun setIsCropActive(view: View, value: Boolean) {
        (view as StickerView).isCropActive = value
    }

    @JvmStatic
    @BindingAdapter("canvasMatrix")
    fun setCanvasMatrix(view: View, value: ObservableMatrix) {
        (view as StickerView).canvasMatrix.set(value.getMatrix())
    }

    @JvmStatic
    @BindingAdapter("stickers")
    fun setStickers(view: View, value: List<Sticker>) {
        (view as StickerView).stickers = value
    }

    @JvmStatic
    @BindingAdapter("icons")
    fun setIcons(view: View, value: List<BitmapStickerIcon>) {
        (view as StickerView).icons = value
    }

    @JvmStatic
    @BindingAdapter("handlingSticker")
    fun setHandlingSticker(view: View, value: Sticker) {
        (view as StickerView).handlingSticker = value
    }

    @JvmStatic
    @BindingAdapter("currentMode")
    fun setCurrentMode(view: View, value: Int) {
        (view as StickerView).currentMode = value
    }

    @JvmStatic
    @BindingAdapter("currentIcon")
    fun setCurrentIcon(view: View, value: BitmapStickerIcon) {
        (view as StickerView).currentIcon = value
    }

    @JvmStatic
    @BindingAdapter("activeIcons")
    fun setActiveIcons(view: View, value: List<BitmapStickerIcon>) {
        (view as StickerView).activeIcons = value
    }
}
