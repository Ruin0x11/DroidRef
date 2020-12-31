package com.xiaopo.flying.sticker;

import android.view.MotionEvent;

import timber.log.Timber;

/**
 * @author wupanjie
 */

public interface StickerIconEvent {
    void onActionDown(StickerView stickerView, MotionEvent event);

    void onActionMove(StickerView stickerView, MotionEvent event);

    void onActionUp(StickerView stickerView, MotionEvent event);

    default void onActionLongPress(StickerView stickerView, MotionEvent event) {
//        Timber.e("LongPress_detected on icon");
    }


}
