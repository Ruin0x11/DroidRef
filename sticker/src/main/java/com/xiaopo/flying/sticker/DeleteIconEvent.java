package com.xiaopo.flying.sticker;

import android.view.MotionEvent;
import android.widget.Toast;

/**
 * @author wupanjie
 */

public class DeleteIconEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        Toast.makeText(stickerView.getContext(), "Long press to delete", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionLongPress(StickerView stickerView, MotionEvent event) {
//        stickerView.removeCurrentSticker();
    }
}
