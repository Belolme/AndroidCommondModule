package com.example.billin.opengl

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class GlTestRenderer(val context: Context) : GLSurfaceView.Renderer {

    private lateinit var roundPointProgram: RoundPointProgram

    private lateinit var roundLineProgram: RoundCapLineProgram

    private lateinit var baseTriangleProgram: BaseTriangleProgram

    private lateinit var gradientTriangleProgram: GradientTriangleProgram

    private val projectMatrix: FloatArray = FloatArray(16)

    private val viewMatrix: FloatArray = FloatArray(16)

    private val viewProjectMatrix: FloatArray = FloatArray(16)


    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        roundPointProgram.draw {
            bindPosition(100f, 100f)
        }

//        roundLineProgram.draw {
//            bindLines(floatArrayOf(500f, 500f, 1000f, 500f, 1000f, 500f, 1000f, 1000f), 8)
//        }
//
//        baseTriangleProgram.draw {
//            bindTriangle(500f, 500f, 1000f, 500f, 1000f, 1000f)
//        }
//
//        gradientTriangleProgram.draw {
//            bindTriangle(500f, 500f, 1000f, 500f, 1000f, 1000f,
//                Color.RED, Color.GREEN, Color.BLUE)
//        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        Matrix.orthoM(
                projectMatrix, 0, 0f, width.toFloat(),
                height.toFloat(), 0f, -1f, 1f
        )
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.multiplyMM(viewProjectMatrix, 0, projectMatrix, 0, viewMatrix, 0)

        roundPointProgram.apply {
            setMatrix(viewProjectMatrix)
            setColor(Color.parseColor("#ff000000"))
            setScreenSize(width, height)
            setSize(100)
        }

        roundLineProgram.apply {
            setMatrix(viewProjectMatrix)
            setAntialias(2f)
            setColor(Color.parseColor("#99ff0000"))
            setSize(50)
        }

        baseTriangleProgram.apply {
            setMatrix(viewProjectMatrix)
            setColor(Color.BLUE)
        }

        gradientTriangleProgram.apply {
            setMatrix(viewProjectMatrix)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 0f)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

//        glEnable(GL_MULTISAMPLE)

        roundPointProgram = RoundPointProgram(context)
        roundLineProgram = RoundCapLineProgram(context)
        baseTriangleProgram = BaseTriangleProgram(context)
        gradientTriangleProgram = GradientTriangleProgram(context)
    }

    private companion object {
        private const val TAG = "GlTestRenderer"
    }
}

