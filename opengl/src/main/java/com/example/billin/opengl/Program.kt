package com.example.billin.opengl

import android.content.Context
import android.opengl.GLES20


abstract class Program(
        context: Context,
        vertexResourceId: Int,
        shaderResourceId: Int
) {

    protected val programId: Int = ShaderHelper.buildProgram(
            TextResourceReader.readTextFileFromResource(context, vertexResourceId),
            TextResourceReader.readTextFileFromResource(context, shaderResourceId)
    )

    fun useProgram() {
        GLES20.glUseProgram(programId)
    }

    abstract fun draw()
}

inline fun <T : Program> T.apply(block: T.() -> Unit): T {
    useProgram()
    block()
    return this
}

inline fun <T : Program> T.draw(block: T.() -> Unit) {
    apply(block)
    draw()
}