package com.example.flying

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class SurfaceView(context: Context) : GLSurfaceView(context) {

    private val mRenderer: GameRenderer
    private val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f
    private var nPreviousX: Float = 0f
    private var mPreviousY: Float = 0f

    init {
        setEGLContextClientVersion(2)
        mRenderer = GameRenderer(context)

        // Set the renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        val x: Float = e.x
        val y: Float = e.y

        //           y=+1
        //           ^
        //           |
        //   -x -----+-----> x
        //           |
        //           | y=-1
        val normX = (x - width.toFloat()*0.5f) / height.toFloat() * 2.0f
        val normY = (height.toFloat() * 0.5f - y) / height.toFloat() * 2.0f

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                mRenderer.screenTouchDownEvent(normX, normY)
                requestRender()
            }
            MotionEvent.ACTION_MOVE -> {
                val u = x - width.toFloat()*0.5f
                val v = height.toFloat() * 0.5f - y
                mRenderer.screenTouchMoveEvent(normX, normY)
                requestRender()
            }
            MotionEvent.ACTION_UP -> {
                mRenderer.screenTouchUpEvent(normX, normY)
                requestRender()
            }
        }

        nPreviousX = x
        mPreviousY = y

        return true
    }
}


