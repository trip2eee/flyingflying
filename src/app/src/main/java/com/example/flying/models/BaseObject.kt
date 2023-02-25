package com.example.flying.models

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

open class BaseObject {

    protected val COORDS_PER_VERTEX = 3

    protected var mProgram: Int = 0

    protected var mVertexBuffer : FloatBuffer? = null
    protected var mNormalBuffer: FloatBuffer? = null

    protected var mVertexCoordHandle: Int = 0
    protected var mVertexNormalHandle: Int = 0
    protected var mTextureCoordHandle: Int = 0

    private var mTextureBitmap: Bitmap? = null
    private var mTextureHandle = IntBuffer.allocate(1)
    protected var mTextureCoordBuffer: FloatBuffer? = null

    protected val mVertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    protected var mVertexCount: Int = 0
    var mNumInstances: Int = 1

    // Set color with red, green, blue and alpha (opacity) values
    open var mInstanceColors : MutableList<Float> = arrayListOf(
        0.63671875f, 0.76953125f, 0.22265625f, 0.1f,
    )
    open var mInstancePositions : MutableList<Float> = arrayListOf(
        0.0f,  -1.0f, 0.0f, 0.0f,
    )
    open var mScale = floatArrayOf(
        0.05f, 0.05f, 0.05f, 1.0f,
    )

    open val mVertexShaderCode =
        "#version 300 es\n" +
        "uniform mat4 uMVPMatrix;" +
        "uniform mat4 uRotMatrix;" +
        "uniform vec4 vScale;" +
        "layout(location = 0) in vec4 vVertexCoord;" +
        "layout(location = 1) in vec3 vNormal;" +
        "layout(location = 2) in vec2 vTexCoord;" +

        "uniform vec4 vInstancePositions[100];" +
        "out vec4 transVertexNormal;" +
        "out vec2 vTextureCoord;" +
        "void main() {" +

        "  gl_Position = uMVPMatrix * ((uRotMatrix*vVertexCoord*vScale) + vInstancePositions[gl_InstanceID]);" +
        "  transVertexNormal = normalize(uMVPMatrix * uRotMatrix*vec4(vNormal, 0.0));" +
        "  vTextureCoord = vTexCoord;" +
        "}"

    open val mFragmentShaderCode =
        "#version 300 es\n" +
        "uniform sampler2D textureObject;" +
        "out vec4 fragColor;" +
        "in vec2 vTextureCoord;" +
        "in vec4 transVertexNormal;" +
        "void main() {" +
        "  vec4 diffuseLightIntensity = vec4(1.0, 1.0, 1.0, 1.0);" +
        "  vec4 inverseLightDirection = normalize(vec4(0.0, 0.0, 1.0, 0.0));" +
        "  float normalDotLight = max(0.0, dot(transVertexNormal, inverseLightDirection));" +
        "  vec4 vColorLight = vec4(0.5f, 0.5f, 0.5f, 1.0f);" +
        "  fragColor = texture(textureObject, vTextureCoord);" +
        "  fragColor += normalDotLight * vColorLight * diffuseLightIntensity;" +
        "  clamp(fragColor, 0.0, 1.0);" +
        "}"

//    open val mVertexShaderCode =
//        "#version 300 es\n" +
//                "uniform mat4 uMVPMatrix;" +
//                "uniform vec4 vScale;" +
//                "layout(location = 0) in vec4 vVertexCoord;" +
//                "layout(location = 1) in vec3 vNormal;" +
//                "layout(location = 2) in vec2 vTexCoord;" +
//
//                "uniform vec4 vInstancePositions[100];" +
//                "out vec4 transVertexNormal;" +
//                "out vec2 vTextureCoord;" +
//                "void main() {" +
//
//                "  gl_Position = uMVPMatrix * vVertexCoord;" +
//                "  transVertexNormal = normalize(uMVPMatrix * vec4(vNormal, 0.0));" +
//                "  vTextureCoord = vTexCoord;" +
//                "}"
//
//    open val mFragmentShaderCode =
//        "#version 300 es\n" +
//        "uniform sampler2D textureObject;" +
//        "out vec4 fragColor;" +
//        "in vec2 vTextureCoord;" +
//        "in vec4 transVertexNormal;" +
//        "void main() {" +
//        "  fragColor = vec4(0.0f, 1.0f, 0.0f, 1.0f);"+
//        "  clamp(fragColor, 0.0, 1.0);" +
//        "}"

    open fun initialize(vertexCoord:FloatArray, vertexNormal:FloatArray, vertexTexture:FloatArray, texture:Bitmap) {
        val vertexShader: Int = loadShader(GLES30.GL_VERTEX_SHADER, mVertexShaderCode)
        val fragmentShader: Int = loadShader(GLES30.GL_FRAGMENT_SHADER, mFragmentShaderCode)

        mTextureBitmap = texture

        mProgram = GLES30.glCreateProgram().also {
            // add the vertex shader to program
            GLES30.glAttachShader(it, vertexShader)
            // add the fragment shader to program
            GLES30.glAttachShader(it, fragmentShader)
            // create OpenGL ES program executables
            GLES30.glLinkProgram(it)
        }

        GLES30.glUseProgram(mProgram)

        GLES30.glGenTextures(1, mTextureHandle)     // generate one texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureHandle[0])

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mTextureBitmap, 0)

        mTextureBitmap?.recycle()  // To reclaim texture memory as soon as possible

        mVertexBuffer =
                // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(vertexCoord.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())
                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    put(vertexCoord)		// add the coordinates to the FloatBuffer
                    position(0)	// set the buffer to read the first coordinate
                }
            }

        mNormalBuffer =
                // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(vertexNormal.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())
                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    put(vertexNormal)		// add the coordinates to the FloatBuffer
                    position(0)	// set the buffer to read the first coordinate
                }
            }

        mTextureCoordBuffer = ByteBuffer.allocateDirect(vertexTexture.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())
            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                put(vertexTexture)		// add the coordinates to the FloatBuffer
                position(0)	// set the buffer to read the first coordinate
            }
        }

        mVertexCount = vertexCoord.size / COORDS_PER_VERTEX
    }


    private fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
        return GLES30.glCreateShader(type).also { shader ->

            // add the source code to the shader and compile it
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)

            val log = GLES30.glGetShaderInfoLog(shader)
            Log.d("[Shader]", "Compile Log:")
            Log.d("[Shader]", log)
        }
    }

    var mAngleY = 0.0f

    open fun draw(mvpMatrix: FloatArray, scale: Float) {
        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram)

        // get handle to shape's transformation matrix
        GLES30.glGetUniformLocation(mProgram, "uMVPMatrix").also {
            // Pass the projection and view transformation to the shader
            GLES30.glUniformMatrix4fv(it, 1, false, mvpMatrix, 0)
        }

        var rotMatrix = FloatArray(16)
        Matrix.setRotateM(rotMatrix, 0, mAngleY, 0.0f, 1.0f, 0.0f)
        mAngleY += 1.0f

        GLES30.glGetUniformLocation(mProgram, "uRotMatrix").also {
            // Pass the projection and view transformation to the shader
            GLES30.glUniformMatrix4fv(it, 1, false, rotMatrix, 0)
        }

        GLES30.glGetUniformLocation(mProgram, "vInstancePositions").also {
            GLES30.glUniform4fv(it, mNumInstances, mInstancePositions.toFloatArray(), 0)
        }

        // get handle to vertex shader's vPosition member
        mVertexCoordHandle = GLES30.glGetAttribLocation(mProgram, "vVertexCoord").also {
            GLES30.glEnableVertexAttribArray(it)
            GLES30.glVertexAttribPointer(it, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false, mVertexStride, mVertexBuffer)
        }

        mVertexNormalHandle = GLES30.glGetAttribLocation(mProgram, "vNormal").also {
            GLES30.glEnableVertexAttribArray(it)
            GLES30.glVertexAttribPointer(it, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false, 0, mNormalBuffer)
        }

        mTextureCoordHandle = GLES30.glGetAttribLocation(mProgram, "vTexCoord").also {
            GLES30.glEnableVertexAttribArray(it)
            GLES30.glVertexAttribPointer(it, 2, GLES30.GL_FLOAT, false, 0, mTextureCoordBuffer)
        }

        mScale = floatArrayOf(scale, scale, scale, 1.0f)
        GLES30.glGetUniformLocation(mProgram, "vScale").also {
            GLES30.glUniform4fv(it, 1, mScale, 0)
        }

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureHandle[0])
        GLES30.glGetUniformLocation(mProgram, "textureObject").also {
            // Set color for drawing the triangle
            GLES30.glUniform1i(it, 0)
        }

        // Draw the triangle
        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, mVertexCount, mNumInstances)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mVertexCoordHandle)
        GLES30.glDisableVertexAttribArray(mVertexNormalHandle)
        GLES30.glDisableVertexAttribArray(mTextureCoordHandle)
    }
}

