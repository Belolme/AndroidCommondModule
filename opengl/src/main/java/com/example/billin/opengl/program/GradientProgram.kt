package com.example.billin.opengl.program

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import com.example.billin.opengl.R
import com.example.billin.opengl.base.Program
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class GradientProgram(context: Context) :
        Program(context, R.raw.vertex_base_color_gradient, R.raw.fragment_base_color_gradient) {

    private val uMatrix = GLES20.glGetUniformLocation(programId, "uMatrix")

    private val uSize = GLES20.glGetUniformLocation(programId, "uSize")

    protected val ap = GLES20.glGetAttribLocation(programId, "ap")

    protected val aColor = GLES20.glGetAttribLocation(programId, "aColor")

    protected val uColor = GLES20.glGetUniformLocation(programId, "uColor")

    fun setSize(size: Int) {
        GLES20.glUniform1f(uSize, size.toFloat())
    }

    fun setMatrix(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0)
    }
}

class GradientTriangleProgram(context: Context) : GradientProgram(context) {

    private val aTriangle = FloatArray(6)

    private val triangleColor = IntArray(3)

    private var triangleVertexSize = 0

    /**
     * 支持绘制 300 个三角形
     */
    private val floatBuffer = ByteBuffer.allocateDirect(300 * 3 * 6 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    fun bindTriangle(
            x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float,
            colorV0: Int, colorV1: Int, colorV2: Int
    ) {
        aTriangle[0] = x0
        aTriangle[1] = y0
        aTriangle[2] = x1
        aTriangle[3] = y1
        aTriangle[4] = x2
        aTriangle[5] = y2
        triangleColor[0] = colorV0
        triangleColor[1] = colorV1
        triangleColor[2] = colorV2
        bindTriangles(aTriangle, triangleColor, aTriangle.size)
    }

    /**
     * [length] 代表 [triangles] 数组的有效数据。
     */
    fun bindTriangles(triangles: FloatArray, colors: IntArray, length: Int) {
        floatBuffer.clear()
        floatBuffer.put(triangles)
        floatBuffer.position(0)

        for (i in 0 until length step 2) {
            val fi = i / 2 * 6
            val ci = i / 2
            floatBuffer.put(fi, triangles[i])
            floatBuffer.put(fi + 1, triangles[i + 1])
            floatBuffer.put(fi + 2, Color.red(colors[ci]).toFloat() / 255f)
            floatBuffer.put(fi + 3, Color.green(colors[ci]).toFloat() / 255f)
            floatBuffer.put(fi + 4, Color.blue(colors[ci]).toFloat() / 255f)
            floatBuffer.put(fi + 5, Color.alpha(colors[ci]).toFloat() / 255f)
        }

        floatBuffer.position(0)
        GLES20.glVertexAttribPointer(ap, 2, GLES20.GL_FLOAT, false, 6 * 4, floatBuffer)
        GLES20.glEnableVertexAttribArray(ap)

        floatBuffer.position(2)
        GLES20.glVertexAttribPointer(aColor, 4, GLES20.GL_FLOAT, false, 6 * 4, floatBuffer)
        GLES20.glEnableVertexAttribArray(aColor)

        triangleVertexSize = length / 2
    }

    fun bindTriangles(triangles: FloatArray, colorV0: Int, colorV1: Int, colorV2: Int, length: Int) {
        floatBuffer.clear()
        floatBuffer.put(triangles)
        floatBuffer.position(0)

        for (i in 0 until length step 2) {
            val ci = i / 2
            floatBuffer.put(triangles[i])
            floatBuffer.put(triangles[i + 1])

            val color = when (ci % 3) {
                0 -> colorV0
                1 -> colorV1
                2 -> colorV2
                else -> throw RuntimeException("impossible exception")
            }

            floatBuffer.put(Color.red(color).toFloat() / 255f)
            floatBuffer.put(Color.green(color).toFloat() / 255f)
            floatBuffer.put(Color.blue(color).toFloat() / 255f)
            floatBuffer.put(Color.alpha(color).toFloat() / 255f)
        }

        floatBuffer.position(0)
        GLES20.glVertexAttribPointer(ap, 2, GLES20.GL_FLOAT, false, 6 * 4, floatBuffer)
        GLES20.glEnableVertexAttribArray(ap)

        floatBuffer.position(2)
        GLES20.glVertexAttribPointer(aColor, 4, GLES20.GL_FLOAT, false, 6 * 4, floatBuffer)
        GLES20.glEnableVertexAttribArray(aColor)

        triangleVertexSize = length / 2
    }

    override fun draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triangleVertexSize)
    }
}