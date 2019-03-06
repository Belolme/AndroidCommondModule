package com.example.billin.opengl

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RoundCapLineProgram(context: Context) :
        Program(context, R.raw.vertex_round_cap_line, R.raw.fragment_round_cap_line) {

    private var trianglesNum = 0

    private val uMatrix = GLES20.glGetUniformLocation(programId, "uMatrix")

    private val uThickness = GLES20.glGetUniformLocation(programId, "uThickness")

    private val uAntialias = GLES20.glGetUniformLocation(programId, "uAntialias")

    private val ap0 = GLES20.glGetAttribLocation(programId, "ap0")

    private val ap1 = GLES20.glGetAttribLocation(programId, "ap1")

    private val auv = GLES20.glGetAttribLocation(programId, "auv")

    private val uColor = GLES20.glGetUniformLocation(programId, "uColor")

    /**
     * 最高支持一次性绘制 300 条线段.
     * 以下表示 （300 条线段 * 6 个顶点 * 每个顶点需要 8 个 float * 每个 float 需要 4 个字节）
     */
    private val floatBuffer = ByteBuffer.allocateDirect(300 * 6 * 6 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    private val oneLine = FloatArray(4)

    fun setColor(color: Int) {
        GLES20.glUniform4f(
                uColor,
                Color.red(color).toFloat() / 255f,
                Color.green(color).toFloat() / 255f,
                Color.blue(color).toFloat() / 255f,
                Color.alpha(color).toFloat() / 255f
        )
    }

    fun setAntialias(pixel: Float) {
        GLES20.glUniform1f(uAntialias, pixel)
    }

    fun setSize(size: Int) {
        GLES20.glUniform1f(uThickness, size.toFloat())
    }

    fun setMatrix(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0)
    }

    fun bindLine(x0: Float, y0: Float, x1: Float, y1: Float) {
        oneLine[0] = x0
        oneLine[1] = y0
        oneLine[2] = x1
        oneLine[3] = y1
        bindLines(oneLine, 4)
    }

    /**
     * 绑定以 [x0, y0, x1, y1, ...] 组成的线段，其中 [length] 表示 [floatArray] 中表示线段数据的长度。
     */
    fun bindLines(floatArray: FloatArray, length: Int) {
        floatBuffer.clear()
        floatBuffer.position(0)
        for (i in 0 until length step 4) {
            for (j in 0 until 6) {
                floatBuffer.put(floatArray[i])
                floatBuffer.put(floatArray[i + 1])
                floatBuffer.put(floatArray[i + 2])
                floatBuffer.put(floatArray[i + 3])

                when (j) {
                    0 -> {
                        floatBuffer.put(-1f)
                        floatBuffer.put(1f)
                    }

                    1 -> {
                        floatBuffer.put(-1f)
                        floatBuffer.put(-1f)
                    }

                    2 -> {
                        floatBuffer.put(1f)
                        floatBuffer.put(1f)
                    }

                    3 -> {
                        floatBuffer.put(1f)
                        floatBuffer.put(1f)
                    }

                    4 -> {
                        floatBuffer.put(-1f)
                        floatBuffer.put(-1f)
                    }

                    5 -> {
                        floatBuffer.put(1f)
                        floatBuffer.put(-1f)
                    }
                }
            }
        }

        floatBuffer.position(0)
        GLES20.glVertexAttribPointer(ap0, 2, GLES20.GL_FLOAT, false, 6 * 4, floatBuffer)
        GLES20.glEnableVertexAttribArray(ap0)

        floatBuffer.position(2)
        GLES20.glVertexAttribPointer(ap1, 2, GLES20.GL_FLOAT, false, 6 * 4, floatBuffer)
        GLES20.glEnableVertexAttribArray(ap1)

        floatBuffer.position(4)
        GLES20.glVertexAttribPointer(auv, 2, GLES20.GL_FLOAT, false, 6 * 4, floatBuffer)
        GLES20.glEnableVertexAttribArray(auv)

        floatBuffer.position(0)

        trianglesNum = length / 2
    }

    override fun draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, trianglesNum * 3)
    }
}