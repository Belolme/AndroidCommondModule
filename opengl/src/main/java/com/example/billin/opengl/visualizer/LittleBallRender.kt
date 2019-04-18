package com.example.billin.opengl.visualizer

import android.content.Context
import android.graphics.Color
import android.opengl.Matrix
import android.util.Log
import com.example.billin.opengl.BuildConfig
import com.example.billin.opengl.base.apply
import com.example.billin.opengl.base.draw
import com.example.billin.opengl.program.GradientTriangleProgram
import com.example.billin.opengl.program.RoundCapLineProgram
import com.example.billin.opengl.program.RoundPointProgram

class LittleBallRender(val context: Context) : VisualizerRender() {

    private val BALL_MAX_WIDTH = 120

    private val CYLINDER_WIDTH = 100

    private val LIGHT_LENGTH_ADDICTION = 200f

    // 1.6 月球的重力; 9.8 地球的重力
    private val GRAVITY = 0.0016f

    /**
     * 需要的数据大小
     */
    private val DATA_LENGTH = Data._1.size

    private val projectMatrix: FloatArray = FloatArray(16)

    private val viewMatrix: FloatArray = FloatArray(16)

    private val viewProjectMatrix: FloatArray = FloatArray(16)

    private val cylinderData: FloatArray = FloatArray(DATA_LENGTH * 4)

    private val triangleData: FloatArray = FloatArray(DATA_LENGTH * 6)

    private val ballData: FloatArray = FloatArray(DATA_LENGTH * 2)

    private val ballVelocity: FloatArray = FloatArray(DATA_LENGTH)

    private var lastDrawTime: Long = 0

    private var centerX = 0f

    private var centerY = 0f

    private var innerWidth = 0f

    private var size = 0f

    private lateinit var roundLineProgram: RoundCapLineProgram

    private lateinit var roundPointProgram: RoundPointProgram

    private lateinit var gradientTriangleProgram: GradientTriangleProgram

    override fun processData(rawData: FloatArray?, length: Int): Int {
        val cropLength = (length - DATA_LENGTH * 2) / 2
        VisualizerCalculateUtil.cropOriginFftData(rawData, cropLength, cropLength, length)
        VisualizerCalculateUtil.processFftMagnitudeData(rawData, DATA_LENGTH * 2)
        VisualizerCalculateUtil.flatData(
                rawData, PiecewiseFormula(
                Math.hypot(128.0, 128.0).toFloat(),
                CYLINDER_WIDTH.toFloat(), 10f, 10f
        ), DATA_LENGTH
        )

        return DATA_LENGTH
    }

    private companion object {
        private const val TAG = "LittleBallRender"
    }

    private val timeArray = LongArray(60)
    private var timeIndex = 0

    override fun draw(processedData: FloatArray?) {
        processedData ?: return

        val radius = innerWidth / 2

        val currentDrawTime = System.currentTimeMillis()
        val intervalTime = currentDrawTime - lastDrawTime

        if (BuildConfig.DEBUG) {
            timeArray[timeIndex] = intervalTime
            timeIndex = (timeIndex + 1) % timeArray.size
            if (timeIndex == 0) Log.d(TAG, "draw: ${1000.0 / timeArray.average()}")
        }

        lastDrawTime = currentDrawTime

        for (i in 0 until DATA_LENGTH) {
            val degree = i.toDouble() / DATA_LENGTH * 2.0 * Math.PI
            val cosDegree = Math.cos(degree)
            var sinDegree = Math.sqrt(1 - cosDegree * cosDegree)
            sinDegree = if (degree > Math.PI) -sinDegree else sinDegree

            val ci = i * 4
            val endRadius = radius + processedData[i]
            val preEndRadius =
                    if (sinDegree == 0.0) cylinderData[ci + 3]
                    else (cylinderData[ci + 2] / sinDegree).toFloat()

            cylinderData[ci] = (sinDegree * radius).toFloat()
            cylinderData[ci + 1] = (cosDegree * radius).toFloat()
            cylinderData[ci + 2] = (sinDegree * endRadius).toFloat()
            cylinderData[ci + 3] = (cosDegree * endRadius).toFloat()

            val tx = (cylinderData[ci + 2] - cylinderData[ci]) / processedData[i]
            val ty = (cylinderData[ci + 3] - cylinderData[ci + 1]) / processedData[i]
            val ox = -ty
            val oy = tx

            val ti = i * 6
            triangleData[ti] = cylinderData[ci + 2] + tx * LIGHT_LENGTH_ADDICTION
            triangleData[ti + 1] = cylinderData[ci + 3] + ty * LIGHT_LENGTH_ADDICTION
            triangleData[ti + 2] = cylinderData[ci + 2] + -ox * 5f
            triangleData[ti + 3] = cylinderData[ci + 3] + -oy * 5f
            triangleData[ti + 4] = cylinderData[ci + 2] + ox * 5f
            triangleData[ti + 5] = cylinderData[ci + 3] + oy * 5f

            val bi = i * 2
            val ballVelocityI = bi / 2

            val ballRadius =
                    if (sinDegree == 0.0) ballData[bi + 1]
                    else (ballData[bi] / sinDegree).toFloat()

            if (ballRadius > endRadius) {
                // 球的半径比柱体大，根据重力改变球的速度
                val newBallRadius: Float = ballRadius +
                        ballVelocity[ballVelocityI] * intervalTime -
                        GRAVITY * intervalTime.toFloat() * intervalTime.toFloat() / 2f

                // 超出限高，强制速度为 0
                if (newBallRadius > BALL_MAX_WIDTH + radius) {
                    ballData[bi] = ((BALL_MAX_WIDTH + radius) * sinDegree).toFloat()
                    ballData[bi + 1] = ((BALL_MAX_WIDTH + radius) * cosDegree).toFloat()
                    ballVelocity[ballVelocityI] = 0f
                } else {
                    ballData[bi] = (newBallRadius * sinDegree).toFloat()
                    ballData[bi + 1] = (newBallRadius * cosDegree).toFloat()
                    ballVelocity[ballVelocityI] = ballVelocity[ballVelocityI] - GRAVITY * intervalTime
                }
            } else {
                // 球的半径比柱体小，根据动量守恒和能量守恒改变球的速度(柱体的重量假设为无限大，球的重量为 1)
                val v = 2f * (endRadius - preEndRadius) / intervalTime
                if (v > ballVelocity[ballVelocityI]) ballVelocity[ballVelocityI] = v

                ballData[bi] = cylinderData[ci + 2]
                ballData[bi + 1] = cylinderData[ci + 3]
            }
        }

        roundPointProgram.draw {
            bindPositions(ballData, ballData.size)
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
    }

    override fun onResize(width: Float, height: Float) {
        centerX = width / 2
        centerY = height / 2

        this.size = Math.min(width, height)
        this.innerWidth = width * 2 / 3

        Matrix.orthoM(
                projectMatrix, 0, 0f, width,
                height, 0f, -1f, 1f
        )
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.translateM(viewMatrix, 0, centerX, centerY, 0f)
        Matrix.multiplyMM(viewProjectMatrix, 0, projectMatrix, 0, viewMatrix, 0)

        roundPointProgram.apply {
            setMatrix(viewProjectMatrix)
            setSize(10)
            setScreenSize(width.toInt(), height.toInt())
            setColor(Color.parseColor("#FF0000FF"))
        }

        roundLineProgram.apply {
            setMatrix(viewProjectMatrix)
            setSize(10)
            setAntialias(1f)
            setColor(Color.parseColor("#FF0000FF"))
        }

        gradientTriangleProgram.apply {
            setMatrix(viewProjectMatrix)
        }
    }

    override fun surfaceCreated() {
        roundLineProgram = RoundCapLineProgram(context)
        roundPointProgram = RoundPointProgram(context)
        gradientTriangleProgram = GradientTriangleProgram(context)
    }
}