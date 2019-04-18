package com.example.billin.opengl.program

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import com.example.billin.opengl.R
import com.example.billin.opengl.base.Program
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class BaseProgram(context: Context) : Program(context, R.raw.vertex_base, R.raw.fragment_base) {

    protected val uMatrix = GLES20.glGetUniformLocation(programId, "uMatrix")

    protected val uSize = GLES20.glGetUniformLocation(programId, "uSize")

    protected val ap = GLES20.glGetAttribLocation(programId, "ap")

    protected val uColor = GLES20.glGetUniformLocation(programId, "uColor")

    fun setColor(color: Int) {
        GLES20.glUniform4f(
                uColor,
                Color.red(color).toFloat() / 255f,
                Color.green(color).toFloat() / 255f,
                Color.blue(color).toFloat() / 255f,
                Color.alpha(color).toFloat() / 255f
        )
    }

    fun setSize(size: Int) {
        GLES20.glUniform1f(uSize, size.toFloat())
    }

    fun setMatrix(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0)
    }
}

class BaseTriangleProgram(context: Context) : BaseProgram(context) {

    private val aTriangle = FloatArray(6)

    private var triangleVertex = 0

    /**
     * 支持绘制 300 个三角形
     */
    protected val floatBuffer = ByteBuffer.allocateDirect(300 * 3 * 2 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    fun bindTriangle(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float) {
        aTriangle[0] = x0
        aTriangle[1] = y0
        aTriangle[2] = x1
        aTriangle[3] = y1
        aTriangle[4] = x2
        aTriangle[5] = y2
        bindTriangles(aTriangle, aTriangle.size)
    }

    fun bindTriangles(triangles: FloatArray, length: Int) {
        floatBuffer.clear()
        floatBuffer.put(triangles)
        floatBuffer.position(0)
        GLES20.glVertexAttribPointer(ap, 2, GLES20.GL_FLOAT, false, 2 * 4, floatBuffer)
        GLES20.glEnableVertexAttribArray(ap)

        triangleVertex = length / 2
    }

    override fun draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triangleVertex)
    }
}

