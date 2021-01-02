package com.xiaopo.flying.sticker.iconEvents;

import com.xiaopo.flying.sticker.StickerView;

/**
 * @author wupanjie
 */

public class FlipVerticallyEvent extends AbstractFlipEvent {

    @Override
    @StickerView.Flip
    protected int getFlipDirection() {
        return StickerView.FLIP_VERTICALLY;
    }
}
