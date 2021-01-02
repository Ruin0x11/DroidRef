package com.xiaopo.flying.sticker;

import android.view.MotionEvent;

import timber.log.Timber;

/**
 * @author wupanjie
 */

public interface StickerIconEvent {
    void onActionDown(StickerView stickerView, StickerViewModel viewModel, MotionEvent event);

    void onActionMove(StickerView stickerView, StickerViewModel viewModel, MotionEvent event);

    void onActionUp(StickerView stickerView, StickerViewModel viewModel, MotionEvent event);

    default void onActionLongPress(StickerView stickerView, StickerViewModel viewModel, MotionEvent event) {
//        Timber.e("LongPress_detected on icon");
    }


}
