package com.example.billin.opengl.visualizer;

/**
 * 映射 Y 值的公式
 */
public abstract class MappingFormula {

    protected float inputMaxValue;

    protected float outputMaxValue;

    public MappingFormula(float inputMaxValue, float outputMaxValue) {
        this.inputMaxValue = inputMaxValue;
        this.outputMaxValue = outputMaxValue;
    }

    public abstract float map(float value);
}
