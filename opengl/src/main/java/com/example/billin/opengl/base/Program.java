package com.example.billin.opengl.base;

import android.content.Context;
import android.opengl.GLES20;

public abstract class Program {

    protected int programId;

    public Program(Context context, int vertexResId, int shaderResId) {
        programId = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(context, vertexResId),
                TextResourceReader.readTextFileFromResource(context, shaderResId)
        );
    }

    public void useProgram() {
        GLES20.glUseProgram(programId);
    }

    public abstract void draw();
}