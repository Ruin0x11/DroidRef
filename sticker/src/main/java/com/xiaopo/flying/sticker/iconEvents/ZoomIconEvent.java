package com.xiaopo.flying.sticker.iconEvents;

import android.view.MotionEvent;

import com.xiaopo.flying.sticker.StickerIconEvent;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.StickerViewModel;

/**
 * @author wupanjie
 */

public class ZoomIconEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, StickerViewModel viewModel, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, StickerViewModel viewModel, MotionEvent event) {
        viewModel.zoomAndRotateCurrentSticker(event);
    }

    @Override
    public void onActionUp(StickerView stickerView, StickerViewModel viewModel, MotionEvent event) {
    }

}
