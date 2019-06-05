package com.billin.www.commondmodual.ui.colorpicker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

/**
 * 一个可以绘制圆边和填充圆形的图形. 用于 {@link CircleColorPicker} 中的 thumb 绘制.
 * <p>
 * Create by Billin on 2019/6/5
 */
public class OutlineOvalShape extends RectShape {

    private int outlineColor, contentColor;

    @FloatRange(from = 0f, to = 1f)
    private float outlineRatio;

    private Path outline;

    private Path content;

    private RectF innerOval;

    /**
     * @param outlineColor 圆边的颜色
     * @param contentColor 填充颜色
     * @param outlineRatio 圆边边宽占图形的百分比
     */
    public OutlineOvalShape(int outlineColor, int contentColor, @FloatRange(from = 0f, to = 1f) float outlineRatio) {
        this.outlineColor = outlineColor;
        this.contentColor = contentColor;
        this.outlineRatio = outlineRatio;

        outline = new Path();
        content = new Path();
        innerOval = new RectF();
    }

    public int getOutlineColor() {
        return outlineColor;
    }

    public void setOutlineColor(int outlineColor) {
        this.outlineColor = outlineColor;
    }

    public int getContentColor() {
        return contentColor;
    }

    public void setContentColor(int contentColor) {
        this.contentColor = contentColor;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        int originColor = paint.getColor();

        paint.setShadowLayer(2, 1, 1, Color.DKGRAY);
        paint.setColor(outlineColor);
        canvas.drawPath(outline, paint);

        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        paint.setColor(contentColor);
        canvas.drawPath(content, paint);

        paint.setColor(originColor);
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final RectF rect = rect();
            outline.setOval((int) Math.ceil(rect.left), (int) Math.ceil(rect.top),
                    (int) Math.floor(rect.right), (int) Math.floor(rect.bottom));
        }
    }

    @Override
    protected void onResize(float width, float height) {
        super.onResize(width, height);

        RectF rect = rect();

        outline.reset();
        content.reset();

        if (outlineRatio == 0) {
            content.addOval(rect, Path.Direction.CW);
            return;
        }

        outline.addOval(rect, Path.Direction.CW);

        float innerWidthOffset = width * outlineRatio / 2f;
        float innerHeightOffset = height * outlineRatio / 2f;
        innerOval.set(
                rect.left + innerWidthOffset,
                rect.top + innerHeightOffset,
                rect.right - innerWidthOffset,
                rect.bottom - innerHeightOffset
        );
        outline.addOval(innerOval, Path.Direction.CCW);

        content.addOval(innerOval, Path.Direction.CW);
    }
}
