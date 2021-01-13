package com.example.billin.opengl.visualizer;


import androidx.annotation.Nullable;

/**
 * 整理波形数据并绘制波形图的实际类
 * <p>
 * Create by Billin on 2019/1/23
 */
public abstract class VisualizerRender {

    private float mWidth;
    private float mHeight;

    /**
     * Returns the width of the Shape.
     */
    public final float getWidth() {
        return mWidth;
    }

    /**
     * Returns the height of the Shape.
     */
    public final float getHeight() {
        return mHeight;
    }

    /**
     * Resizes the dimensions of this shape.
     * <p>
     * Must be called before {@link #draw(float[])}.
     *
     * @param width  the width of the shape (in pixels)
     * @param height the height of the shape (in pixels)
     */
    public final void resize(float width, float height) {
        if (width < 0) {
            width = 0;
        }
        if (height < 0) {
            height = 0;
        }
        if (mWidth != width || mHeight != height) {
            mWidth = width;
            mHeight = height;
            onResize(width, height);
        }
    }

    /**
     * Callback method called when {@link #resize(float, float)} is executed.
     *
     * @param width  the new width of the Shape
     * @param height the new height of the Shape
     */
    protected void onResize(float width, float height) {
    }

    /**
     * 数据预处理, 这里的数据来源于 {@link android.media.audiofx.Visualizer} 中的 fft 频谱源数据。
     *
     * @param length 传入数据的有效长度, 这个长度值应当在每一次的调用都是固定的
     * @return 返回处理后数据的有效长度
     */
    public abstract int processData(float[] rawData, int length);

    /**
     * 经过 {@link #processData(float[], int)} 方法处理过的数据会再经过属性动画插值器处理产生每一帧动画的数据,
     * 最终生成每一帧数据的其中一帧会传入这个方法进行绘制。
     *
     * @param processedData 这个参数的数据有效长度和 {@link #processData(float[], int)} 返回的长度一致
     */
    public abstract void draw(@Nullable float[] processedData);

    public abstract void surfaceCreated();
}
