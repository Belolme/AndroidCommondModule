package com.example.billin.opengl

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.billin.opengl.Data._1
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LittleBallRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val lightLength = 300

    private val projectMatrix: FloatArray = FloatArray(16)

    private val viewMatrix: FloatArray = FloatArray(16)

    private val viewProjectMatrix: FloatArray = FloatArray(16)

    private lateinit var roundLineProgram: RoundCapLineProgram

    private lateinit var roundPointProgram: RoundPointProgram

    private lateinit var gradientTriangleProgram: GradientTriangleProgram

    private var centerX = 0

    private var centerY = 0

    private var innerWidth = 0

    private var width = 0

    private val cylinderData = FloatArray(_1.size * 4)

    private val triangleData = FloatArray(_1.size * 6)

    private val random = Random()

    private var lastStartTime = 0L

    override fun onDrawFrame(gl: GL10?) {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "onDrawFrame:lastFrameTime=${startTime - lastStartTime}")
        lastStartTime = startTime

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        for (i in 0 until _1.size) {
            _1[i] += (random.nextInt(3) - 1).toDouble()
        }

        val radius = innerWidth / 2
        for (i in 0 until _1.size) {
            val degree = i.toDouble() / _1.size * 2.0 * Math.PI
            val cosDegree = Math.cos(degree)
            var sinDegree = Math.sqrt(1 - cosDegree * cosDegree)
            sinDegree = if (degree > Math.PI) -sinDegree else sinDegree

            val endRadius = radius + _1[i]

            val ci = i * 4
            cylinderData[ci] = (sinDegree * radius).toFloat()
            cylinderData[ci + 1] = (cosDegree * radius).toFloat()
            cylinderData[ci + 2] = (sinDegree * endRadius).toFloat()
            cylinderData[ci + 3] = (cosDegree * endRadius).toFloat()

            val tx = ((cylinderData[ci + 2] - cylinderData[ci]) / _1[i]).toFloat()
            val ty = ((cylinderData[ci + 3] - cylinderData[ci + 1]) / _1[i]).toFloat()
            val ox = -ty
            val oy = tx

            val ti = i * 6
            triangleData[ti] = cylinderData[ci + 2] + tx * lightLength
            triangleData[ti + 1] = cylinderData[ci + 3] + ty * lightLength
            triangleData[ti + 2] = cylinderData[ci + 2] + -ox * 5f
            triangleData[ti + 3] = cylinderData[ci + 3] + -oy * 5f
            triangleData[ti + 4] = cylinderData[ci + 2] + ox * 5f
            triangleData[ti + 5] = cylinderData[ci + 3] + oy * 5f
        }

        roundPointProgram.draw {
            bindPosition(100f, 100f)
        }

        gradientTriangleProgram.draw {
            bindTriangles(
                    triangleData,
                    0,
                    Color.parseColor("#7D0000FF"),
                    Color.parseColor("#7D0000FF"),
                    triangleData.size
            )
        }

        roundLineProgram.draw {
            bindLines(cylinderData, cylinderData.size)
        }


        val endTime = System.currentTimeMillis()
        Log.d(TAG, "onDrawFrame: ${endTime - startTime}")
    }

    private companion object {
        private const val TAG = "LittleBallRenderer"
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        centerX = width / 2
        centerY = height / 2

        this.width = Math.min(width, height)
        this.innerWidth = width * 2 / 3

        Matrix.orthoM(
                projectMatrix, 0, 0f, width.toFloat(),
                height.toFloat(), 0f, -1f, 1f
        )
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.translateM(viewMatrix, 0, centerX.toFloat(), centerY.toFloat(), 0f)
        Matrix.multiplyMM(viewProjectMatrix, 0, projectMatrix, 0, viewMatrix, 0)

        roundLineProgram.apply {
            setMatrix(viewProjectMatrix)
            setSize(10)
            setAntialias(1f)
            setColor(Color.parseColor("#FF0000FF"))
        }

        roundPointProgram.apply {
            setMatrix(viewProjectMatrix)
            setColor(Color.parseColor("#FF000000"))
            setScreenSize(width, height)
            setSize(100)
        }

        gradientTriangleProgram.apply {
            setMatrix(viewProjectMatrix)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        roundPointProgram = RoundPointProgram(context)
        roundLineProgram = RoundCapLineProgram(context)
        gradientTriangleProgram = GradientTriangleProgram(context)
    }
}