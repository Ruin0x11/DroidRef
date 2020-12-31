package com.xiaopo.flying.sticker;

import android.view.MotionEvent;

/**
 * @author wupanjie
 */

public class StyleIconEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {
    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
//        Sticker currentSticker = stickerView.getCurrentSticker();
//        if (currentSticker instanceof TextSticker) {
//            TextSticker textSticker = (TextSticker) currentSticker;
//            Timber.d(textSticker.getText());
//            textSticker.toggleUnderline();
//            stickerView.invalidate();
//        }
    }

}
