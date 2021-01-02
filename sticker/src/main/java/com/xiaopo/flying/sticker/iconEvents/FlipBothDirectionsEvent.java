package com.xiaopo.flying.sticker.iconEvents;

import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.iconEvents.AbstractFlipEvent;

/**
 * @author wupanjie
 */

public class FlipBothDirectionsEvent extends AbstractFlipEvent {

    @Override
    @StickerView.Flip
    protected int getFlipDirection() {
        return StickerView.FLIP_VERTICALLY | StickerView.FLIP_HORIZONTALLY;
    }
}
