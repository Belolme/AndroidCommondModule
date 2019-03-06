package com.example.billin.opengl.visualizer;

/**
 * 两段线性函数实现
 */
public class PiecewiseFormula extends MappingFormula {

    private float firstK;

    private float secondK;

    private float secondA;

    private float turnPoint;

    /**
     * @param firstLineK 第一段线性函数的 k 值，第二段线性函数的 k 值会根据这个值来计算
     * @param piecePoint 分段函数的转折点
     */
    public PiecewiseFormula(float inputMaxValue, float outputMaxValue, float firstLineK, float piecePoint) {
        super(inputMaxValue, outputMaxValue);
        if (piecePoint * firstLineK > outputMaxValue) {
            throw new IllegalArgumentException("illegal k value(piecePoint * firstLineK > outputMaxValue).");
        }

        firstK = firstLineK;
        turnPoint = piecePoint;
        secondK = (outputMaxValue - piecePoint * firstLineK) / (inputMaxValue - piecePoint);
        secondA = piecePoint * firstLineK - piecePoint * secondK;
    }

    @Override
    public float map(float value) {
        return value < turnPoint ? firstK * value : secondK * value + secondA;
    }
}
