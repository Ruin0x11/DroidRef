package com.xiaopo.flying.sticker;

import android.graphics.*;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author wupanjie
 */
public abstract class Sticker {

    @IntDef(flag = true, value = {
            Position.CENTER, Position.TOP, Position.BOTTOM, Position.LEFT, Position.RIGHT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Position {
        int CENTER = 1;
        int TOP = 1 << 1;
        int LEFT = 1 << 2;
        int RIGHT = 1 << 3;
        int BOTTOM = 1 << 4;
    }

    private final float[] matrixValues = new float[9];
    private final float[] unrotatedWrapperCorner = new float[8];
    private final float[] unrotatedPoint = new float[2];
    private final float[] boundPoints = new float[8];
    private final float[] mappedBounds = new float[8];
    private final RectF trappedRect = new RectF();
    private final Matrix matrix = new Matrix();
    private final Matrix canvasMatrix = new Matrix();
    private Matrix finalMatrix = new Matrix();
    private boolean isFlippedHorizontally;
    private boolean isFlippedVertically;
    protected Rect realBounds;
    protected RectF croppedBounds;

    private boolean visible = true;

    public Sticker() {}

    public Sticker(Sticker other) {
        this.matrix.set(other.matrix);
        this.canvasMatrix.set(other.canvasMatrix);
        this.realBounds = new Rect(other.realBounds);
        this.croppedBounds = new RectF(other.croppedBounds);
        this.isFlippedHorizontally = other.isFlippedHorizontally;
        this.isFlippedVertically = other.isFlippedVertically;

        this.recalcFinalMatrix();
    }

    public boolean isFlippedHorizontally() {
        return isFlippedHorizontally;
    }

    @NonNull
    public Sticker setFlippedHorizontally(boolean flippedHorizontally) {
        isFlippedHorizontally = flippedHorizontally;
        return this;
    }

    public boolean isFlippedVertically() {
        return isFlippedVertically;
    }

    @NonNull
    public Sticker setFlippedVertically(boolean flippedVertically) {
        isFlippedVertically = flippedVertically;
        return this;
    }

    @NonNull
    public Matrix getMatrix() {
        return matrix;
    }

    public Sticker setMatrix(@Nullable Matrix matrix) {
        this.finalMatrix = null;
        this.matrix.set(matrix);
        return this;
    }

    @NonNull
    public Matrix getCanvasMatrix() {
        return canvasMatrix;
    }

    public Sticker setCanvasMatrix(@Nullable Matrix matrix) {
        this.finalMatrix = null;
        this.canvasMatrix.set(matrix);
        return this;
    }

    public void recalcFinalMatrix() {
        if (finalMatrix == null) {
            finalMatrix = new Matrix();
        }
        finalMatrix.setConcat(this.canvasMatrix, this.matrix);
    }

    @NonNull
    public Matrix getFinalMatrix() {
        recalcFinalMatrix();
        return finalMatrix;
    }

    public abstract void draw(@NonNull Canvas canvas);

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract Sticker setDrawable(@NonNull Drawable drawable);

    @NonNull
    public abstract Drawable getDrawable();

    @NonNull
    public abstract Sticker setAlpha(@IntRange(from = 0, to = 255) int alpha);

    public float[] getBoundPoints() {
        float[] points = new float[8];
        getBoundPoints(points);
        return points;
    }

    public void getBoundPoints(@NonNull float[] points) {
        if (!isFlippedHorizontally) {
            if (!isFlippedVertically) {
                points[0] = 0f;
                points[1] = 0f;
                points[2] = getWidth();
                points[3] = 0f;
                points[4] = 0f;
                points[5] = getHeight();
                points[6] = getWidth();
                points[7] = getHeight();
            } else {
                points[0] = 0f;
                points[1] = getHeight();
                points[2] = getWidth();
                points[3] = getHeight();
                points[4] = 0f;
                points[5] = 0f;
                points[6] = getWidth();
                points[7] = 0f;
            }
        } else {
            if (!isFlippedVertically) {
                points[0] = getWidth();
                points[1] = 0f;
                points[2] = 0f;
                points[3] = 0f;
                points[4] = getWidth();
                points[5] = getHeight();
                points[6] = 0f;
                points[7] = getHeight();
            } else {
                points[0] = getWidth();
                points[1] = getHeight();
                points[2] = 0f;
                points[3] = getHeight();
                points[4] = getWidth();
                points[5] = 0f;
                points[6] = 0f;
                points[7] = 0f;
            }
        }
    }

    public float[] getCroppedBoundPoints() {
        float[] points = new float[8];
        getCroppedBoundPoints(points);
        return points;
    }

    public void getCroppedBoundPoints(@NonNull float[] points) {
        if (!isFlippedHorizontally) {
            if (!isFlippedVertically) {
                points[0] = croppedBounds.left;
                points[1] = croppedBounds.top;
                points[2] = croppedBounds.right;
                points[3] = croppedBounds.top;
                points[4] = croppedBounds.left;
                points[5] = croppedBounds.bottom;
                points[6] = croppedBounds.right;
                points[7] = croppedBounds.bottom;
            } else {
                points[0] = croppedBounds.left;
                points[1] = croppedBounds.bottom;
                points[2] = croppedBounds.right;
                points[3] = croppedBounds.bottom;
                points[4] = croppedBounds.left;
                points[5] = croppedBounds.top;
                points[6] = croppedBounds.right;
                points[7] = croppedBounds.top;
            }
        } else {
            if (!isFlippedVertically) {
                points[0] = croppedBounds.right;
                points[1] = croppedBounds.top;
                points[2] = croppedBounds.left;
                points[3] = croppedBounds.top;
                points[4] = croppedBounds.right;
                points[5] = croppedBounds.bottom;
                points[6] = croppedBounds.left;
                points[7] = croppedBounds.bottom;
            } else {
                points[0] = croppedBounds.right;
                points[1] = croppedBounds.bottom;
                points[2] = croppedBounds.left;;
                points[3] = croppedBounds.bottom;
                points[4] = croppedBounds.right;
                points[5] = croppedBounds.top;
                points[6] = croppedBounds.left;
                points[7] = croppedBounds.top;
            }
        }
    }

    @NonNull
    public float[] getMappedBoundPoints() {
        float[] dst = new float[8];
        getMappedPoints(dst, getBoundPoints());
        return dst;
    }

    @NonNull
    public float[] getMappedPoints(@NonNull float[] src) {
        recalcFinalMatrix();
        float[] dst = new float[src.length];
        finalMatrix.mapPoints(dst, src);
        return dst;
    }

    public void getMappedPoints(@NonNull float[] dst, @NonNull float[] src) {
        recalcFinalMatrix();
        finalMatrix.mapPoints(dst, src);
    }

    public void getMappedPointsPre(@NonNull float[] dst, @NonNull float[] src) {
        matrix.mapPoints(dst, src);
    }

    @NonNull
    public RectF getBound() {
        RectF bound = new RectF();
        getBound(bound);
        return bound;
    }

    public void getBound(@NonNull RectF dst) {
        dst.set(0, 0, getWidth(), getHeight());
    }

    @NonNull
    public RectF getMappedBound() {
        RectF dst = new RectF();
        getMappedBound(dst, getBound());
        return dst;
    }

    public void getMappedBound(@NonNull RectF dst, @NonNull RectF bound) {
        recalcFinalMatrix();
        finalMatrix.mapRect(dst, bound);
    }

    @NonNull
    public RectF getMappedBoundPre() {
        RectF dst = new RectF();
        getMappedBoundPre(dst, getBound());
        return dst;
    }

    public void getMappedBoundPre(@NonNull RectF dst, @NonNull RectF bound) {
        recalcFinalMatrix();
        finalMatrix.mapRect(dst, bound);
    }

    @NonNull
    public PointF getCenterPoint() {
        PointF center = new PointF();
        getCenterPoint(center);
        return center;
    }

    public void getCenterPoint(@NonNull PointF dst) {
        dst.set(getWidth() * 1f / 2, getHeight() * 1f / 2);
    }

    @NonNull
    public PointF getCenterPointCropped() {
        PointF center = new PointF();
        getCenterPointCropped(center);
        return center;
    }

    public void getCenterPointCropped(@NonNull PointF dst) {
        dst.set(croppedBounds.left + (croppedBounds.right - croppedBounds.left) / 2,
                croppedBounds.top  + (croppedBounds.bottom - croppedBounds.top) / 2);
    }

    @NonNull
    public PointF getMappedCenterPoint() {
        PointF pointF = getCenterPoint();
        getMappedCenterPoint(pointF, new float[2], new float[2]);
        return pointF;
    }

    public void getMappedCenterPoint(@NonNull PointF dst, @NonNull float[] mappedPoints,
                                     @NonNull float[] src) {
        getCenterPoint(dst);
        src[0] = dst.x;
        src[1] = dst.y;
        getMappedPointsPre(mappedPoints, src);
        dst.set(mappedPoints[0], mappedPoints[1]);
    }


    public void getMappedCenterPointCropped(@NonNull PointF dst, @NonNull float[] mappedPoints,
                                     @NonNull float[] src) {
        getCenterPointCropped(dst);
        src[0] = dst.x;
        src[1] = dst.y;
        getMappedPointsPre(mappedPoints, src);
        dst.set(mappedPoints[0], mappedPoints[1]);
    }

    public float getCurrentScale() {
        return getMatrixScale(matrix);
    }

    public float getCurrentHeight() {
        return getMatrixScale(matrix) * getHeight();
    }

    public float getCurrentWidth() {
        return getMatrixScale(matrix) * getWidth();
    }

    /**
     * This method calculates scale value for given Matrix object.
     */
    public float getMatrixScale(@NonNull Matrix matrix) {
        return (float) Math.sqrt(Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X), 2) + Math.pow(
                getMatrixValue(matrix, Matrix.MSKEW_Y), 2));
    }

    /**
     * @return - current image rotation angle.
     */
    public float getCurrentAngle() {
        return getMatrixAngle(matrix);
    }

    /**
     * This method calculates rotation angle for given Matrix object.
     */
    public float getMatrixAngle(@NonNull Matrix matrix) {
        return (float) Math.toDegrees(-(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X),
                Math.abs(getMatrixValue(matrix, Matrix.MSCALE_X)))));
    }

    public float getMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = 9) int valueIndex) {
        matrix.getValues(matrixValues);
        return matrixValues[valueIndex];
    }

    public boolean contains(float x, float y) {
        return contains(new float[]{x, y});
    }

    public boolean contains(@NonNull float[] point) {
        Matrix tempMatrix = new Matrix();
        tempMatrix.setRotate(-getCurrentAngle());
        getBoundPoints(boundPoints);
        getMappedPoints(mappedBounds, boundPoints);
        tempMatrix.mapPoints(unrotatedWrapperCorner, mappedBounds);
        tempMatrix.mapPoints(unrotatedPoint, point);
        StickerUtils.trapToRect(trappedRect, unrotatedWrapperCorner);
        return trappedRect.contains(unrotatedPoint[0], unrotatedPoint[1]);
    }

    public boolean containsCropped(float x, float y) {
        return contains(new float[]{x, y});
    }

    public boolean containsCropped(@NonNull float[] point) {
        Matrix tempMatrix = new Matrix();
        tempMatrix.setRotate(-getCurrentAngle());
        getCroppedBoundPoints(boundPoints);
        getMappedPoints(mappedBounds, boundPoints);
        tempMatrix.mapPoints(unrotatedWrapperCorner, mappedBounds);
        tempMatrix.mapPoints(unrotatedPoint, point);
        StickerUtils.trapToRect(trappedRect, unrotatedWrapperCorner);
        return trappedRect.contains(unrotatedPoint[0], unrotatedPoint[1]);
    }

    public void release() {
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Rect getRealBounds() {
        return realBounds;
    }

    public RectF getCroppedBounds() {
        return croppedBounds;
    }

    public void setCroppedBounds(RectF cropped) {
        croppedBounds.right = Math.max(Math.min(realBounds.right, cropped.right), 1);
        croppedBounds.bottom = Math.max(Math.min(realBounds.bottom, cropped.bottom), 1);
        croppedBounds.left = Math.min(Math.max(realBounds.left, cropped.left), croppedBounds.right - 1);
        croppedBounds.top = Math.min(Math.max(realBounds.top, cropped.top), croppedBounds.bottom - 1);
    }
}
