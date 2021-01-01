package com.xiaopo.flying.sticker

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.IntDef
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xiaopo.flying.sticker.StickerView.Flip
import com.xiaopo.flying.sticker.StickerView.OnStickerAreaTouchListener
import timber.log.Timber
import kotlin.math.pow

open class StickerViewModel :
    ViewModel() {
    var isLocked: MutableLiveData<Boolean> = MutableLiveData(false)
    var mustLockToPan: MutableLiveData<Boolean> = MutableLiveData(false)
    var isCropActive: MutableLiveData<Boolean> = MutableLiveData(false)
    var rotationEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    var constrained: MutableLiveData<Boolean> = MutableLiveData(false)
    var bringToFrontCurrentSticker = MutableLiveData(true)

    var canvasMatrix: CustomMutableLiveData<ObservableMatrix> = CustomMutableLiveData(ObservableMatrix())

    var stickers: MutableLiveData<ArrayList<Sticker>> = MutableLiveData(ArrayList())
    var icons: MutableLiveData<ArrayList<BitmapStickerIcon>> = MutableLiveData(ArrayList())
    var activeIcons: MutableLiveData<ArrayList<BitmapStickerIcon>> = MutableLiveData(ArrayList(4))
    var handlingSticker: MutableLiveData<Sticker?> = MutableLiveData(null)

    var isFirstRun: Boolean = true

    private val onStickerAreaTouchListener: OnStickerAreaTouchListener? = null

    lateinit var stickerOperationListener: StickerView.OnStickerOperationListener

    private val stickerWorldMatrix = Matrix()
    private val stickerScreenMatrix = Matrix()
    private val moveMatrix = Matrix()
    private val point = FloatArray(2)
    private val currentCenterPoint = PointF()
    private val tmp = FloatArray(2)
    private var pointerId = -1
    private var midPoint = PointF()

    private val DEFAULT_MIN_CLICK_DELAY_TIME = 200
    private var minClickDelayTime = DEFAULT_MIN_CLICK_DELAY_TIME

    //the first point down position
    private var downX = 0f
    private var downY = 0f
    private var downXScaled = 0f
    private var downYScaled = 0f

    private var oldDistance = 0f
    private var oldRotation = 0f
    private var previousRotation = 0f

    private var lastClickTime: Long = 0

    @IntDef(
        ActionMode.NONE,
        ActionMode.DRAG,
        ActionMode.ZOOM_WITH_TWO_FINGER,
        ActionMode.ICON,
        ActionMode.CLICK,
        ActionMode.CANVAS_DRAG,
        ActionMode.CANVAS_ZOOM_WITH_TWO_FINGER
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    protected annotation class ActionMode {
        companion object {
            const val NONE = 0
            const val DRAG = 1
            const val ZOOM_WITH_TWO_FINGER = 2
            const val ICON = 3
            const val CLICK = 4
            const val CANVAS_DRAG = 5
            const val CANVAS_ZOOM_WITH_TWO_FINGER = 6
        }
    }

    var currentMode = MutableLiveData(ActionMode.NONE)

    var currentIcon: MutableLiveData<BitmapStickerIcon?> = MutableLiveData(null)

    @SuppressLint("ClickableViewAccessibility")
    val onTouchListener = View.OnTouchListener { v, event -> onTouchEvent(v, event) }

    fun addSticker(sticker: Sticker) {
        addSticker(sticker, Sticker.Position.CENTER)
    }

    fun addSticker(sticker: Sticker, position: Int) {
        sticker.setCanvasMatrix(canvasMatrix.value!!.getMatrix())
        sticker.recalcFinalMatrix()
        stickers.value!!.add(sticker)
        stickerOperationListener.onStickerAdded(sticker, position)
    }

    fun resetView() {
        canvasMatrix.value!!.setMatrix(Matrix())
        updateCanvasMatrix()
        stickerOperationListener.onInvalidateView()
    }

    private fun updateCanvasMatrix() {
        for (i in stickers.value!!.indices) {
            val sticker: Sticker = stickers.value!![i]
            sticker.setCanvasMatrix(canvasMatrix.value!!.getMatrix())
        }
    }

    fun removeCurrentSticker(): Boolean {
        if (handlingSticker.value != null) {
            return removeSticker(handlingSticker.value!!)
        } else {
            return false
        }
    }

    fun removeSticker(sticker: Sticker): Boolean {
        if (stickers.value!!.contains(sticker)) {
            stickers.value!!.remove(sticker)
            stickerOperationListener.onStickerDeleted(sticker)
            if (handlingSticker.value == sticker) {
                handlingSticker.value = null
            }
            stickerOperationListener.onInvalidateView()
            return true
        } else {
            Timber.d("remove: the sticker is not in this StickerView")
            return false
        }
    }

    fun removeAllStickers() {
        stickers.value?.clear()
        if (handlingSticker.value != null) {
            handlingSticker.value!!.release()
            handlingSticker.value = null
        }
        stickerOperationListener.onInvalidateView()
    }

    fun resetCurrentStickerCropping() {
        handlingSticker.value?.let(this::resetStickerCropping)
    }

    private fun resetStickerCropping(sticker: Sticker) {
        if (isLocked.value != true) {
            sticker.setCroppedBounds(RectF(sticker.getRealBounds()))
            stickerOperationListener.onInvalidateView()
        }
    }

    fun resetCurrentStickerZoom() {
        handlingSticker.value?.let(this::resetStickerZoom)
    }

    private fun resetStickerZoom(sticker: Sticker) {
        if (isLocked.value != true) {
            val temp2 = floatArrayOf(sticker.centerPoint.x, sticker.centerPoint.y)
            sticker.matrix.mapPoints(temp2)
            sticker.matrix.reset()
            sticker.matrix.postTranslate(
                temp2[0] - sticker.width / 2f,
                temp2[1] - sticker.height / 2f
            )
            sticker.recalcFinalMatrix()
            stickerOperationListener.onInvalidateView()
        }
    }

    private fun handleCanvasMotion(view: View, event: MotionEvent): Boolean {
        handlingSticker.value = null
        currentIcon.value = null
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> //                Timber.d("CANVAS MotionEvent.ACTION_DOWN event__: %s", event.toString());
                if (pointerId == -1) {
                    if (!onTouchDownCanvas(event)) {
                        return false
                    }
                }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDistance = StickerMath.calculateDistance(event)
                oldRotation = StickerMath.calculateRotation(event)
                midPoint = calculateMidPoint(event)
                stickerWorldMatrix.set(canvasMatrix.value!!.getMatrix())
                currentMode.value = ActionMode.CANVAS_ZOOM_WITH_TWO_FINGER
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.getPointerId(0) != pointerId) {
                    calculateDown(event)
                    pointerId = event.getPointerId(0)
                }
                handleMoveActionCanvas(event)
            }
            MotionEvent.ACTION_UP -> {
                var i = 0
                while (i < event.pointerCount) {
                    if (event.getPointerId(i) == pointerId) {
                        onTouchUpCanvas(event)
                        break
                    }
                    i++
                }
            }
            MotionEvent.ACTION_POINTER_UP -> if (currentMode.value == ActionMode.CANVAS_ZOOM_WITH_TWO_FINGER) {
                if (!onTouchDownCanvas(event)) {
                    return false
                }
            } else {
                currentMode.value = ActionMode.NONE
            }
        }
        stickerOperationListener.onInvalidateView()
        return true
    }

    /**
     * @param event MotionEvent received from [)][.onTouchEvent]
     */
    protected fun onTouchDownCanvas(event: MotionEvent): Boolean {
        currentMode.value = ActionMode.CANVAS_DRAG
        calculateDown(event)
        pointerId = event.getPointerId(0)
        midPoint = calculateMidPoint()
        oldDistance = StickerMath.calculateDistance(midPoint.x, midPoint.y, downX, downY)
        oldRotation = StickerMath.calculateRotation(midPoint.x, midPoint.y, downX, downY)
        stickerWorldMatrix.set(canvasMatrix.value!!.getMatrix())
        return true
    }

    protected fun onTouchUpCanvas(event: MotionEvent) {
        val currentTime = SystemClock.uptimeMillis()
        pointerId = -1
        currentMode.value = ActionMode.NONE
        lastClickTime = currentTime
    }

    protected fun handleMoveActionCanvas(event: MotionEvent) {
        when (currentMode.value) {
            ActionMode.CANVAS_DRAG -> {
                moveMatrix.set(stickerWorldMatrix)
                moveMatrix.postTranslate(event.x - downX, event.y - downY)
                canvasMatrix.value!!.setMatrix(moveMatrix)
                updateCanvasMatrix()
            }
            ActionMode.CANVAS_ZOOM_WITH_TWO_FINGER -> {
                val newDistance = StickerMath.calculateDistance(event)
                //float newRotation = StickerMath.calculateRotation(event);
                moveMatrix.set(stickerWorldMatrix)
                moveMatrix.postScale(
                    newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                    midPoint.y
                )
                //moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y);
                canvasMatrix.value!!.setMatrix(moveMatrix)
                updateCanvasMatrix()
            }
        }
    }

    private fun isMovingCanvas(): Boolean {
        return currentMode.value == ActionMode.CANVAS_DRAG || currentMode.value == ActionMode.CANVAS_ZOOM_WITH_TWO_FINGER
    }

    @SuppressLint("ClickableViewAccessibility")
    fun onTouchEvent(view: View, event: MotionEvent): Boolean {
        if (isLocked.value == true || isMovingCanvas()) {
            return handleCanvasMotion(view, event)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                //                Timber.d("MotionEvent.ACTION_DOWN event__: %s", event.toString());
                if (onStickerAreaTouchListener != null) {
                    onStickerAreaTouchListener.onStickerAreaTouch()
                }
                if (!onTouchDown(view, event)) {
                    return if (mustLockToPan.value != true) {
                        handleCanvasMotion(view, event)
                    } else false
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
                handleMoveAction(view, event)
            }
            MotionEvent.ACTION_UP -> onTouchUp(view, event)
            MotionEvent.ACTION_POINTER_UP -> if (currentMode.value == ActionMode.ZOOM_WITH_TWO_FINGER && handlingSticker.value != null) {
                stickerOperationListener.onStickerZoomFinished(handlingSticker.value!!)
                if (!onTouchDown(view, event)) {
                    return if (mustLockToPan.value != true) {
                        handleCanvasMotion(view, event)
                    } else false
                }
                currentMode.value = ActionMode.DRAG
            } else {
                currentMode.value = ActionMode.NONE
            }
        }
        stickerOperationListener.onInvalidateView()
        return true
    }

    /**
     * @param event MotionEvent received from [)][.onTouchEvent]
     */
    protected fun onTouchDown(view: View, event: MotionEvent): Boolean {
        currentMode.value = ActionMode.DRAG
        calculateDown(event)
        midPoint = StickerMath.calculateMidPoint(handlingSticker.value)
        oldDistance = StickerMath.calculateDistance(midPoint.x, midPoint.y, downX, downY)
        oldRotation = StickerMath.calculateRotation(midPoint.x, midPoint.y, downX, downY)
        currentIcon.value = findCurrentIconTouched()
        // gestureDetector.onTouchEvent(event)
        if (currentIcon.value != null) {
            currentMode.value = ActionMode.ICON
            currentIcon.value!!.onActionDown(view as StickerView, event)
            //            Timber.d("current_icon: %s", currentIcon.getDrawableName());
        } else {
            handlingSticker.value = findHandlingSticker()
        }

       handlingSticker.value?.let {
            stickerWorldMatrix.set(it.getMatrix())
            stickerScreenMatrix.set(it.getFinalMatrix())
            if (bringToFrontCurrentSticker.value == true) {
                stickers.value!!.remove(it)
                stickers.value!!.add(it)
            }
            stickerOperationListener.onStickerTouchedDown(it)
        }
        return currentIcon.value != null || handlingSticker.value != null
    }

    protected fun onTouchUp(view: View, event: MotionEvent) {
        val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

        val currentTime = SystemClock.uptimeMillis()
        if (currentMode.value == ActionMode.ICON && currentIcon.value != null && handlingSticker.value != null) {
            currentIcon.value!!.onActionUp(view as StickerView, event)
            //            Timber.d("current_icon: %s", currentIcon.getDrawableName());
        }
        if (currentMode.value == ActionMode.DRAG && Math.abs(event.x - downX) < touchSlop && Math.abs(
                event.y - downY
            ) < touchSlop && handlingSticker.value != null
        ) {
            currentMode.value = ActionMode.CLICK
                stickerOperationListener.onStickerClicked(handlingSticker.value!!)
            if (currentTime - lastClickTime < minClickDelayTime) {
                stickerOperationListener.onStickerDoubleTapped(handlingSticker.value!!)
            }
        }
        if (currentMode.value == ActionMode.DRAG && handlingSticker.value != null) {
              stickerOperationListener.onStickerDragFinished(handlingSticker.value!!)
        }
        currentMode.value = ActionMode.NONE
        lastClickTime = currentTime
    }

    private fun screenToWorld(vx: Float, vy: Float): PointF {
        val vec = floatArrayOf(vx, vy)
        val a = Matrix()
        canvasMatrix.value!!.invert(a)
        a.mapVectors(vec)
        return PointF(vec[0], vec[1])
    }

    protected fun handleMoveAction(view: View, event: MotionEvent) {
        when (currentMode.value) {
            ActionMode.NONE, ActionMode.CLICK -> {
            }
            ActionMode.DRAG -> if (handlingSticker.value != null) {
                moveMatrix.set(stickerWorldMatrix)
                val vec = screenToWorld(event.x - downX, event.y - downY)
                moveMatrix.postTranslate(vec.x, vec.y)
                handlingSticker.value!!.setMatrix(moveMatrix)
                if (constrained.value == true) {
                    constrainSticker(view, handlingSticker.value!!)
                }
                stickerOperationListener.onStickerMoved(handlingSticker.value!!)
            }
            ActionMode.ZOOM_WITH_TWO_FINGER -> if (handlingSticker.value != null) {
                val newDistance = StickerMath.calculateDistanceScaled(event, canvasMatrix.value!!.getMatrix())
                val newRotation = StickerMath.calculateRotation(event)
                moveMatrix.set(stickerWorldMatrix)
                moveMatrix.postScale(
                    newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                    midPoint.y
                )
                if (rotationEnabled.value == true) {
                    moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y)
                }
                handlingSticker.value!!.setMatrix(moveMatrix)
            }
            ActionMode.ICON -> if (handlingSticker.value != null && currentIcon.value != null) {
                currentIcon.value!!.onActionMove(view as StickerView, event)
            }
        }
    }

    fun zoomAndRotateCurrentSticker(event: MotionEvent) {
        zoomAndRotateSticker(handlingSticker.value, event)
    }

    fun zoomAndRotateSticker(sticker: Sticker?, event: MotionEvent) {
        if (sticker != null) {
            val temp = floatArrayOf(event.x, event.y)
            val a = Matrix()
            canvasMatrix.value!!.invert(a)
            a.mapPoints(temp)
            val temp2 = floatArrayOf(sticker.centerPoint.x, sticker.centerPoint.y)
            stickerWorldMatrix.mapPoints(temp2)
            //canvasMatrix.mapPoints(temp2);
            midPoint.x = temp2[0]
            midPoint.y = temp2[1]
            val oldDistance = StickerMath.calculateDistance(
                midPoint.x,
                midPoint.y,
                downXScaled,
                downYScaled
            )
            val newDistance = StickerMath.calculateDistance(
                midPoint.x,
                midPoint.y,
                temp[0],
                temp[1]
            )
            val newRotation = StickerMath.calculateRotation(
                midPoint.x,
                midPoint.y,
                temp[0],
                temp[1]
            )
            moveMatrix.set(stickerWorldMatrix)
            moveMatrix.postScale(
                newDistance / oldDistance,
                newDistance / oldDistance,
                midPoint.x,
                midPoint.y
            )
            if (rotationEnabled.value == true) {
                moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y)
            }
            handlingSticker.value!!.setMatrix(moveMatrix)
        }
    }

    protected fun constrainSticker(view: View, sticker: Sticker) {
        var moveX = 0f
        var moveY = 0f
        val width: Int = view.getWidth()
        val height: Int = view.getHeight()
        sticker.getMappedCenterPoint(currentCenterPoint, point, tmp)
        if (currentCenterPoint.x < 0) {
            moveX = -currentCenterPoint.x
        }
        if (currentCenterPoint.x > width) {
            moveX = width - currentCenterPoint.x
        }
        if (currentCenterPoint.y < 0) {
            moveY = -currentCenterPoint.y
        }
        if (currentCenterPoint.y > height) {
            moveY = height - currentCenterPoint.y
        }
        sticker.matrix.postTranslate(moveX, moveY)
    }

    fun cropCurrentSticker(event: MotionEvent, gravity: Int) {
        cropSticker(handlingSticker.value, event, gravity)
    }

    protected fun cropSticker(sticker: Sticker?, event: MotionEvent, gravity: Int) {
        if (sticker == null) {
            return
        }
        val dx = event.x
        val dy = event.y
        val inv = Matrix()
        sticker.canvasMatrix.invert(inv)
        val inv2 = Matrix()
        sticker.matrix.invert(inv2)
        val temp = floatArrayOf(dx, dy)
        inv.mapPoints(temp)
        inv2.mapPoints(temp)
        val pointOnSticker = PointF(temp[0], temp[1])
        val cropped = RectF(sticker.getCroppedBounds())
        val px = temp[0].toInt()
        val py = temp[1].toInt()
        when (gravity) {
            BitmapStickerIcon.LEFT_TOP -> {
                cropped.left = Math.min(px.toFloat(), cropped.right)
                cropped.top = Math.min(py.toFloat(), cropped.bottom)
            }
            BitmapStickerIcon.RIGHT_TOP -> {
                cropped.right = Math.max(px.toFloat(), cropped.left)
                cropped.top = Math.min(py.toFloat(), cropped.bottom)
            }
            BitmapStickerIcon.LEFT_BOTTOM -> {
                cropped.left = Math.min(px.toFloat(), cropped.right)
                cropped.bottom = Math.max(py.toFloat(), cropped.top)
            }
            BitmapStickerIcon.RIGHT_BOTTOM -> {
                cropped.right = Math.max(px.toFloat(), cropped.left)
                cropped.bottom = Math.max(py.toFloat(), cropped.top)
            }
        }
        sticker.setCroppedBounds(cropped)
    }

    protected fun findCurrentIconTouched(): BitmapStickerIcon? {
        for (icon in activeIcons.value!!) {
            val pos = icon.mappedPos
            val x: Float = icon.x + icon.iconRadius - downXScaled
            val y: Float = icon.y + icon.iconRadius - downYScaled
            val distancePow2 = x * x + y * y
            if (distancePow2 <= ((icon.iconRadius + icon.iconRadius) * 1.2f.toDouble()).pow(2.0)
            ) {
                return icon
            }
        }
        return null
    }

    /**
     * find the touched Sticker
     */
    protected fun findHandlingSticker(): Sticker? {
        stickers.value?.let {
            for (i in it.indices.reversed()) {
                if (isInStickerArea(it[i], downX, downY)) {
                    return it[i]
                }
            }
        }
        return null
    }

    protected fun isInStickerArea(sticker: Sticker, downX: Float, downY: Float): Boolean {
        tmp[0] = downX
        tmp[1] = downY
        return sticker.contains(tmp)
    }

    protected fun calculateMidPoint(event: MotionEvent?): PointF {
        if (event == null || event.pointerCount < 2) {
            midPoint.set(0f, 0f)
            return midPoint
        }
        val pts = floatArrayOf(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
        //canvasMatrix.mapPoints(pts);
        val x = (pts[0] + pts[2]) / 2
        val y = (pts[1] + pts[3]) / 2
        midPoint.set(x, y)
        return midPoint
    }

    protected fun calculateDown(event: MotionEvent?) {
        if (event == null || event.pointerCount < 1) {
            downX = 0f
            downY = 0f
            downXScaled = 0f
            downYScaled = 0f
            return
        }
        val pts = floatArrayOf(event.getX(0), event.getY(0))
        downX = pts[0]
        downY = pts[1]
        val a = Matrix()
        canvasMatrix.value!!.invert(a)
        a.mapPoints(pts)
        downXScaled = pts[0]
        downYScaled = pts[1]
    }

    protected fun calculateMidPoint(): PointF {
        if (handlingSticker.value == null) {
            midPoint.set(0f, 0f)
            return midPoint
        }
        handlingSticker.value!!.getMappedCenterPoint(midPoint, point, tmp)
        return midPoint
    }


    fun flipCurrentSticker(direction: Int) {
        handlingSticker.value?.let { flip(it, direction) }
    }

    fun flip(sticker: Sticker, @Flip direction: Int) {
        sticker.getCenterPoint(midPoint)
        if (direction and StickerView.FLIP_HORIZONTALLY > 0) {
            sticker.matrix.preScale(-1f, 1f, midPoint.x, midPoint.y)
            sticker.isFlippedHorizontally = !sticker.isFlippedHorizontally
        }
        if (direction and StickerView.FLIP_VERTICALLY > 0) {
            sticker.matrix.preScale(1f, -1f, midPoint.x, midPoint.y)
            sticker.isFlippedVertically = !sticker.isFlippedVertically
        }
        sticker.recalcFinalMatrix()
        stickerOperationListener.onStickerFlipped(sticker)
    }

    fun showCurrentSticker() {
        handlingSticker.value?.setVisible(true)
    }

    fun hideCurrentSticker() {
        handlingSticker.value?.setVisible(false)
    }
}
