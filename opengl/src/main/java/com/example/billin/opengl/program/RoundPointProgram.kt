package com.example.billin.opengl.program

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import com.example.billin.opengl.R
import com.example.billin.opengl.base.Program
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RoundPointProgram(context: Context) :
        Program(context, R.raw.vertex_round_point, R.raw.fragment_round_point) {

    private val uMatrix = GLES20.glGetUniformLocation(programId, "uMatrix")

    private val uSize = GLES20.glGetUniformLocation(programId, "uSize")

    private val aPosition = GLES20.glGetAttribLocation(programId, "aPosition")

    private val uColor = GLES20.glGetUniformLocation(programId, "uColor")

    private val uScreenSize = GLES20.glGetUniformLocation(programId, "uScreenSize")

    /**
     * 最高支持一次性绘制 300 个点
     */
    private val floatBuffer = ByteBuffer.allocateDirect(300 * 2 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    private val onePosition: FloatArray = FloatArray(2)

    private var drawSize = 0

    fun setColor(color: Int) {
        GLES20.glUniform4f(
                uColor,
                Color.red(color).toFloat() / 255f,
                Color.green(color).toFloat() / 255f,
                Color.blue(color).toFloat() / 255f,
                Color.alpha(color).toFloat() / 255f
        )
    }

    fun setScreenSize(width: Int, height: Int) {
        GLES20.glUniform2f(uScreenSize, width.toFloat(), height.toFloat())
    }

    fun setSize(size: Int) {
        GLES20.glUniform1f(uSize, size.toFloat())
    }

    fun setMatrix(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0)
    }

    fun bindPosition(x: Float, y: Float) {
        onePosition[0] = x
        onePosition[1] = y
        bindPositions(onePosition, 2)
    }

    fun bindPositions(points: FloatArray, size: Int) {
        floatBuffer.clear()
        floatBuffer.put(points)
        floatBuffer.position(0)
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, floatBuffer)
        GLES20.glEnableVertexAttribArray(aPosition)
        floatBuffer.position(0)

        drawSize = size / 2
    }

    override fun draw() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, drawSize)
    }
}