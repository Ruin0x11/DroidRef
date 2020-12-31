package com.xiaopo.flying.sticker;

import android.view.MotionEvent;

/**
 * @author wupanjie
 */

public class CropIconEvent implements StickerIconEvent {
    private final int gravity;

    public CropIconEvent(int gravity) {
        this.gravity = gravity;
    }

    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        stickerView.cropCurrentSticker(event, gravity);
    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
    }

}
