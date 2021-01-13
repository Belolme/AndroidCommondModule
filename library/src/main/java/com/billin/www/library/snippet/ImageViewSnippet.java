package com.billin.www.library.snippet;

import android.graphics.Matrix;
import android.widget.ImageView;

/**
 * 和 image view 相关的代码片段
 * <p>
 * Created by Billin on 2018/2/12.
 */
public class ImageViewSnippet {

    /**
     * 自定义 scale type.
     * imageView width 必须确定，background 和 src 必须已经指定
     */
    private void scaleImageSrc(ImageView imageView, float scale) {
        scale = (float) imageView.getLayoutParams().width / imageView.getDrawable().getIntrinsicWidth() * scale;

        float dx = (imageView.getLayoutParams().width - imageView.getDrawable().getIntrinsicWidth() * scale) * 0.5f;

        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dx);

        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        imageView.setImageMatrix(matrix);
    }
}
