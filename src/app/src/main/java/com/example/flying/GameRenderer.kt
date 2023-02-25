package com.example.flying

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.flying.models.ObjectEgoFighter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class GameRenderer(context: Context) : GLSurfaceView.Renderer {

    // Model View Projection Matrix
    private val mPMatrix = FloatArray(16)

    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mContext = context

    private lateinit var mEgoFighter : ObjectEgoFighter

    var angleY = 0.0f

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glCullFace(GLES30.GL_BACK)       // Ignore back face
        GLES30.glFrontFace(GLES30.GL_CCW)        // Front face: Clockwise
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        // Initialize rendering objects
        mEgoFighter = ObjectEgoFighter(mContext)
    }

    /**
     * This method is called for each redraw of the view.
     * */
    override fun onDrawFrame(unused: GL10?) {

        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Set the camera position (View matrix)
        // eye at (0, 0, 0)
        // looking at (0, 0, -1)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 0f, 0f, 0f, -1f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        mEgoFighter.draw(mPMatrix)

    }

    fun screenTouchDownEvent(v:Float, u:Float){

    }

    fun screenTouchMoveEvent(u:Float, v:Float) {

        val n = 1.0f     // near: 1
        val z = 2.0f
        val w = z

        // distance from the eye (xe, ye, ze)
        // points projected on the near plane (xp, yp, zp)
        // ze:xe = n:xp
        // ze:ye = n:yp
        // xp = xe/n*ze
        // yp = ye/n*ze

        val x = u / n * w
        val y = v / n * w

        mEgoFighter.mInstancePositions[0] = x
        mEgoFighter.mInstancePositions[1] = y
    }

    fun screenTouchUpEvent(u:Float, v:Float){

    }

    /**
     * Called if the geometry of the view changes, for example whe the device's screen orientation changes.
     * */
    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {

        val viewWidth = min(width, height)
        val viewHeight = max(width, height)

        GLES30.glViewport(0, 0, viewWidth, viewHeight)

        val ratio: Float = viewWidth.toFloat() / viewHeight.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method

        // Coordinate
        //              y +1
        //              ^
        //              |
        //  -ratio -----+-----> x +ratio
        //              |
        //              | -1
        // upward: +z, downward: -z
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f,1f, 10f)
        //Matrix.perspectiveM()

//        mMinWorldX = -ratio
//        mMaxWorldX = ratio
//
//        mMaxWorldY = 1.0f - mTopMarginY
//        mMinWorldY = -1.0f
//
//        mWorldWidth = mMaxWorldX - mMinWorldX
//        mBubbleRadius = (mWorldWidth * 0.5f) / mCols.toFloat()
//        mBubbleDiameter = mBubbleRadius * 2.0f
//        mContactThreshold = mBubbleDiameter * 0.9f
//        mWorldHeight = mBubbleRadius + (sin(mAlignAngle) *mBubbleDiameter*mRows)
//        mGameOverHeight = mBubbleRadius + (sin(mAlignAngle) *mBubbleDiameter*(mRows-1.0f))
//
//        // update the origin
//        mOrigin = computeOrigin()
    }
}

