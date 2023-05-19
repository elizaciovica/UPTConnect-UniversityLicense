package edu.licenta.uptconnect.util

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class PanningImageView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
    private val matrixImage = Matrix()
    private val startPoint = PointF()
    private val previousPoint = PointF()
    private val defaultZoom = 2.5f // Adjust the default zoom level as needed
    //private var scaleFactor = 1.0f
    //private val scaleGestureDetector: ScaleGestureDetector

    init {
        matrixImage.postScale(defaultZoom, defaultZoom)
        imageMatrix = matrixImage
        //todo zoom possible
        //scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //scaleGestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startPoint.set(event.x, event.y)
                previousPoint.set(startPoint)
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - previousPoint.x
                val dy = event.y - previousPoint.y
                matrixImage.postTranslate(dx, dy)
                imageMatrix = matrixImage
                previousPoint.set(event.x, event.y)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

//    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
////            scaleFactor *= scaleGestureDetector.scaleFactor
////            scaleFactor = max(0.1f, min(scaleFactor, 10.0f))
////            this@PanningImageView.scaleX = scaleFactor
////            this@PanningImageView.scaleY = scaleFactor
////            return true
//            val focusX = scaleGestureDetector.focusX
//            val focusY = scaleGestureDetector.focusY
//            scaleFactor *= scaleGestureDetector.scaleFactor
//            scaleFactor = scaleFactor.coerceIn(0.1f, 10.0f)
//            matrixImage.postScale(scaleFactor, scaleFactor, focusX, focusY)
//            imageMatrix = matrixImage
//            return true
//        }
//    }
}
