package com.xiaopo.flying.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.core.content.ContextCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author wupanjie
 */
public class BitmapStickerIcon extends DrawableSticker implements StickerIconEvent {
    public static final float DEFAULT_ICON_RADIUS = 35f;
    public static final float DEFAULT_ICON_EXTRA_RADIUS = 10f;

    @IntDef({LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gravity {

    }

    public static final int LEFT_TOP = 0;
    public static final int RIGHT_TOP = 1;
    public static final int LEFT_BOTTOM = 2;
    public static final int RIGHT_BOTTOM = 3;

    private float iconRadius = DEFAULT_ICON_RADIUS;
    private float iconExtraRadius = DEFAULT_ICON_EXTRA_RADIUS;
    private float x;
    private float y;
    @Gravity
    private int position;

    private String iconResName;

    private StickerIconEvent iconEvent;

    public BitmapStickerIcon(Drawable drawable, @Gravity int gravity) {
        super(drawable);
        this.position = gravity;
    }

    public BitmapStickerIcon(Context context, @DrawableRes int drawableRes, @Gravity int gravity) {
        super(ContextCompat.getDrawable(context, drawableRes));
        this.iconResName = context.getResources().getResourceEntryName(drawableRes);
        this.position = gravity;
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.save();
        canvas.concat(getCanvasMatrix());
        canvas.drawCircle(x, y, iconRadius, paint);
        canvas.restore();
        super.draw(canvas);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public PointF getMappedPos() {
        float[] pt = {x, y};
        getCanvasMatrix().mapPoints(pt);
        return new PointF(pt[0], pt[1]);
    }

    public float getIconRadius() {
        return iconRadius;
    }

    public void setIconRadius(float iconRadius) {
        this.iconRadius = iconRadius;
    }

    public float getIconExtraRadius() {
        return iconExtraRadius;
    }

    public void setIconExtraRadius(float iconExtraRadius) {
        this.iconExtraRadius = iconExtraRadius;
    }

    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {
        if (iconEvent != null) {
            iconEvent.onActionDown(stickerView, event);
        }
    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        if (iconEvent != null) {
            iconEvent.onActionMove(stickerView, event);
        }
    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        if (iconEvent != null) {
            iconEvent.onActionUp(stickerView, event);
        }
    }

    @Override
    public void onActionLongPress(StickerView stickerView, MotionEvent event) {
        if (iconEvent != null) {
            iconEvent.onActionLongPress(stickerView, event);
        }
    }

    public StickerIconEvent getIconEvent() {
        return iconEvent;
    }

    public void setIconEvent(StickerIconEvent iconEvent) {
        this.iconEvent = iconEvent;
    }

    @Gravity
    public int getPosition() {
        return position;
    }

    public void setPosition(@Gravity int position) {
        this.position = position;
    }

    public String getDrawableName() {
        return iconResName;
    }
}
