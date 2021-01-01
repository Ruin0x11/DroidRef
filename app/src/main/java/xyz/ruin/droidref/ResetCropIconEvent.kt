package xyz.ruin.droidref

import com.xiaopo.flying.sticker.StickerIconEvent
import com.xiaopo.flying.sticker.StickerView
import android.view.MotionEvent
import android.widget.Toast

/**
 * @author wupanjie
 * @see StickerIconEvent
 */
class ResetCropIconEvent : StickerIconEvent {
    override fun onActionDown(stickerView: StickerView, event: MotionEvent) {}
    override fun onActionMove(stickerView: StickerView, event: MotionEvent) {}
    override fun onActionUp(stickerView: StickerView, event: MotionEvent) {
//        stickerView.resetCurrentStickerCropping();
    }
}