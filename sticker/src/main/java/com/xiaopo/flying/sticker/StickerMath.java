package com.xiaopo.flying.sticker;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StickerMath {
    private static final float[] point = new float[2];
    private static final float[] tmp = new float[2];

    @NonNull
    public static PointF calculateMidPoint(Sticker sticker) {
        PointF midPoint = new PointF();
        if (sticker == null) {
            midPoint.set(0, 0);
            return midPoint;
        }
        sticker.getMappedCenterPointCropped(midPoint, point, tmp);
        return midPoint;
    }

    /**
     * calculate rotation in line with two fingers and x-axis
     **/
    public static float calculateRotation(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        return calculateRotation(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
    }

    public static float calculateRotation(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        double radians = java.lang.Math.atan2(y, x);
        return (float) java.lang.Math.toDegrees(radians);
    }

    /**
     * calculate Distance in two fingers
     **/
    public static float calculateDistance(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        return calculateDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
    }

    public static float calculateDistance(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;

        return (float) java.lang.Math.sqrt(x * x + y * y);
    }

    /**
     * calculate Distance in two fingers
     **/
    public static float calculateDistanceScaled(@Nullable MotionEvent event, Matrix canvasMatrix) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        return calculateDistanceScaled(event.getX(0), event.getY(0), event.getX(1), event.getY(1), canvasMatrix);
    }

    public static float calculateDistanceScaled(float x1, float y1, float x2, float y2, Matrix canvasMatrix) {
        float[] pts = {x1, y1, x2, y2};
        canvasMatrix.mapPoints(pts);
        return calculateDistance(pts[0], pts[1], pts[2], pts[3]);
    }
}
