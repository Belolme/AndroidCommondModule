package com.billin.www.commondmodual.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;

/**
 * 一些关于 bitmap 的操作
 * <p/>
 * Created by Billin on 2018/2/1.
 */
public class BitmapUtil {

    /**
     * 把图片添加 gradient 效果
     */
    public Bitmap getGradientBitmap(Bitmap src, int x0, int y0, int x1, int y1, int startColor,
                                    int endColor, float startPos, float endPos) {

        Bitmap maskBm = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        paint.setShader(new LinearGradient(x0, y0, x1, y1,
                new int[]{Color.TRANSPARENT, startColor, endColor, Color.TRANSPARENT},
                new float[]{startPos, startPos, endPos, endPos}, Shader.TileMode.CLAMP));

        Canvas canvas = new Canvas(maskBm);
        canvas.drawRect(x0, y0, x1, y1, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(src, 0, 0, paint);

        return maskBm;
    }
}
