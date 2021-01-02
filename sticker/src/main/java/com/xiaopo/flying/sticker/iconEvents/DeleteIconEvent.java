package com.xiaopo.flying.sticker.iconEvents;

import android.view.MotionEvent;
import android.widget.Toast;

import com.xiaopo.flying.sticker.StickerIconEvent;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.StickerViewModel;

/**
 * @author wupanjie
 */

public class DeleteIconEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, StickerViewModel viewModel, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, StickerViewModel viewModel, MotionEvent event) {

    }

    @Override
    public void onActionUp(StickerView stickerView, StickerViewModel viewModel, MotionEvent event) {
        Toast.makeText(stickerView.getContext(), "Long press to delete", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionLongPress(StickerView stickerView, StickerViewModel viewModel, MotionEvent event) {
        viewModel.removeCurrentSticker();
    }
}
