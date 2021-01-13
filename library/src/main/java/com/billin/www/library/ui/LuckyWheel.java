package com.billin.www.library.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;

import com.billin.www.library.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by Billin on 2019/12/24
 */
public class LuckyWheel extends View {

    /* 大转盘固定的属性 */
    /**
     * 转盘中奖品的个数.
     */
    private static final int ITEM_SIZE = 8;

    /**
     * 绘制转盘的尺寸, 单位 px.
     */
    private static final int CONTENT_SIZE = 880;

    /**
     * 转盘 X 轴偏移量系数.
     * 该值的取值和 drawable 中的 turntable_pic_turntable 相关.
     */
    private static final float CONTENT_OFFSET_X_RATIO = 0.095f;

    /**
     * 转盘 Y 轴偏移量系数.
     * 该值的取值和 drawable 中的 turntable_pic_turntable 相关.
     */
    private static final float CONTENT_OFFSET_Y_RATIO = 0.095f;

    /**
     * 正方形 icon 的长度 / 转盘的半径.
     */
    private static final float ICON_RATIO = 0.73941368f;

    /**
     * 扇区颜色
     */
    private static final int COLOR_SECTION = Color.parseColor("#FFEEE0");

    /* Drawables */
    private BitmapDrawable point;
    private BitmapDrawable background;
    private BitmapDrawable content;
    private Matrix backgroundMatrix;

    /* Paints */
    private Paint paint;
    private TextPaint textPaint;

    /* Attributions */
    /**
     * 转盘旋转的角度.
     */
    @FloatRange(from = 0, to = 360)
    private float degree;

    /* Animation */
    private WheelStateMachine wheelStateMachine;

    public LuckyWheel(Context context) {
        this(context, null);
    }

    public LuckyWheel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LuckyWheel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        background = (BitmapDrawable) context.getResources().getDrawable(R.mipmap.lucky_wheel_bg);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        point = (BitmapDrawable) context.getResources().getDrawable(R.mipmap.lucky_wheel_point);
        int pointLeft = (int) ((background.getBounds().width() - point.getIntrinsicWidth()) / 2f);
        int pointTop = (int) ((background.getBounds().height() - point.getIntrinsicHeight()) / 2f);
        point.setBounds(
                pointLeft,
                pointTop,
                pointLeft + point.getIntrinsicWidth(),
                pointTop + point.getIntrinsicHeight()
        );

        backgroundMatrix = new Matrix();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#963707"));
//        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 11,
                getResources().getDisplayMetrics()
        ));

        wheelStateMachine = new WheelStateMachine();
    }

    public void initResourceAwards(List<Pair<Integer, String>> iconResAndTitles) {
        if (iconResAndTitles == null || iconResAndTitles.size() != ITEM_SIZE) {
            throw new IllegalArgumentException("Awards size must is " + ITEM_SIZE);
        }

        List<Pair<Bitmap, String>> iconAndTitles = new ArrayList<>(ITEM_SIZE);
        for (Pair<Integer, String> iconResAndTitle : iconResAndTitles) {
            if (iconResAndTitle.first == null || iconResAndTitle.second == null) {
                throw new IllegalArgumentException("icon and title must not null");
            }

            Pair<Bitmap, String> iconAndTitle = Pair.create(
                    ((BitmapDrawable) getResources().getDrawable(iconResAndTitle.first)).getBitmap(),
                    iconResAndTitle.second
            );
            iconAndTitles.add(iconAndTitle);
        }

        initAwards(iconAndTitles);
    }

    public void initAwards(List<Pair<Bitmap, String>> iconAndTitles) {
        if (iconAndTitles == null || iconAndTitles.size() != ITEM_SIZE) {
            throw new IllegalArgumentException("Awards size must is " + ITEM_SIZE);
        }

        Bitmap contentBitmap = content == null ?
                Bitmap.createBitmap(CONTENT_SIZE, CONTENT_SIZE, Bitmap.Config.ARGB_8888) :
                content.getBitmap();
        Canvas contentCanvas = new Canvas(contentBitmap);

        // 转盘半径
        final int radius = CONTENT_SIZE / 2;
        // 转盘区域
        final RectF contentRectF = new RectF(0, 0, CONTENT_SIZE, CONTENT_SIZE);
        // 扇区角度
        final float sectionAngle = 360f / ITEM_SIZE;

        // 奖品的位置计算. 计算在一个等腰三角形中最大正方形的区域.
        // \--------/
        //  \ |__| /
        //   \    /
        //    \  /
        final double alpha = Math.PI / ITEM_SIZE;
        final float height = radius * ICON_RATIO;
        final float x = (float) (height * height * Math.tan(alpha) /
                (height + 2 * height * Math.tan(alpha)));
        final float iconTop = radius - height;
        final float iconLeft = -x + radius;
        final float iconRight = +x + radius;
        final float iconBottom = iconTop + 2 * x;

        // 文字的起始位置和长度
        final float textWidth = (float) (height * Math.tan(alpha)) * 2f;
        final float textTop = (float) (radius - Math.cos(alpha) * radius) * 2f;
        final float textLeft = radius - textWidth / 2f;

        // draw white circle
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        contentCanvas.drawCircle(radius, radius, radius, paint);

        RectF iconLocate = new RectF();
        for (int i = 0; i < ITEM_SIZE; i++) {
            // draw arc
            if (i % 2 == 0) {
                paint.setColor(COLOR_SECTION);
                contentCanvas.drawArc(
                        contentRectF,
                        -sectionAngle / 2f - 90,
                        sectionAngle,
                        true,
                        paint
                );
            }

            // draw icon
            Bitmap icon = iconAndTitles.get(i).first;
            if (icon != null) {
                if (icon.getWidth() / icon.getHeight() > 1) {
                    iconLocate.set(
                            iconLeft,
                            iconTop,
                            iconRight,
                            (iconRight - iconLeft) / (1f * icon.getWidth() / icon.getHeight())
                    );
                } else {
                    float interval = (iconRight - iconLeft - (iconBottom - iconTop) * icon.getWidth() / icon.getHeight()) / 2f;
                    iconLocate.set(
                            iconLeft + interval,
                            iconTop,
                            iconRight - interval,
                            iconBottom
                    );
                }
                contentCanvas.drawBitmap(icon, null, iconLocate, null);
            }

            // draw text
            String title = iconAndTitles.get(i).second;
            if (title != null && !title.isEmpty()) {
                contentCanvas.translate(textLeft, textTop);
                StaticLayout layout;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    layout = StaticLayout.Builder
                            .obtain(title, 0, title.length(), textPaint, (int) textWidth)
                            .setMaxLines(2)
                            .setEllipsize(TextUtils.TruncateAt.END)
                            .setAlignment(Layout.Alignment.ALIGN_CENTER)
                            .setLineSpacing(0, 1f)
                            .build();
                } else {
                    layout = new StaticLayout(
                            title, 0, title.length(), textPaint, (int) textWidth,
                            Layout.Alignment.ALIGN_CENTER,
                            1f, 0, false,
                            TextUtils.TruncateAt.END, 300
                    );
                }

                layout.draw(contentCanvas);
                contentCanvas.translate(-textLeft, -textTop);
            }

            contentCanvas.rotate(sectionAngle, radius, radius);
        }

        if (content == null) {
            content = new BitmapDrawable(getResources(), contentBitmap);
            int offsetX = (int) (CONTENT_OFFSET_X_RATIO * background.getBounds().width());
            int offsetY = (int) (CONTENT_OFFSET_Y_RATIO * background.getBounds().height());
            content.setBounds(
                    offsetX, offsetY,
                    background.getIntrinsicWidth() - offsetX,
                    background.getIntrinsicHeight() - offsetY
            );
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public boolean isInitAwards() {
        return content != null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = background.getIntrinsicWidth();
        int height = background.getIntrinsicHeight();
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0),
                resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float min = Math.min(w, h);
        float scaleW = min / background.getIntrinsicWidth();
        float scaleH = min / background.getIntrinsicHeight();

        float tx = (w / scaleW - background.getBounds().width()) / 2f;
        float ty = (h / scaleH - background.getBounds().height()) / 2f;
        backgroundMatrix.setScale(scaleW, scaleH);
        backgroundMatrix.preTranslate(tx, ty);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw background
        canvas.concat(backgroundMatrix);
        background.draw(canvas);

        // draw wheel content
        if (content != null) {
            int count = canvas.save();
            Rect contentBound = content.getBounds();
            canvas.rotate(
                    -degree,
                    contentBound.left + contentBound.width() / 2f,
                    contentBound.top + contentBound.height() / 2f
            );
            content.draw(canvas);
            canvas.restoreToCount(count);
        }

        // draw point
        point.draw(canvas);
    }

    private float getDegree() {
        return degree;
    }

    private void setDegree(float degree) {
        this.degree = degree % 360;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 获取当前选中的 item 位置
     */
    public int getSelected() {
        // 选中 i 求转动角度的反函数.
        return ((int) ((degree * ITEM_SIZE + 180) / 360)) % ITEM_SIZE;
    }

    /**
     * 开始转动转盘.
     */
    public void startLoop() {
        wheelStateMachine.currentState().start();
    }

    /**
     * 结束转动转盘, 转动转盘到指定的位置.
     * 指定的 i 必须符合 [0, {@link #ITEM_SIZE}) 范围,
     * 且先前通过 {@link #initAwards(List)} (List)}
     * 或者 {@link #initResourceAwards(List)} (List)} 方法设置了对应的 icon 和 title 才有效。
     * 在动画没有结束的时候调用这个方法会取消先前的动画，并且设置动画重新开始。
     *
     * @param i      转动到的第几个 item. 从 0 开始计数, 顺时针旋转.
     * @param offset 转动偏移值，范围为 (0, 1)。当此值设为 0.5 的时候，会偏移到格子的中间，
     *               设置为 0.1 的时候，会偏移到格子左边界往右边界角度的 10%。
     */
    public void stopLoop(int i, float offset, Animator.AnimatorListener animatorEndListener) {
        int stopAngle = (int) ((360 * offset + 360 * i - 180) / ITEM_SIZE);
        wheelStateMachine.currentState().stop(stopAngle + 360f * 3, animatorEndListener);
    }

    public boolean isLoop() {
        return wheelStateMachine.isLooping();
    }

    /**
     * 转盘状态状态机.
     * <p>
     * prepareState -> runningState -> endState;
     * endState -> prepareState;
     */
    private class WheelStateMachine {

        private final WheelState prepareState = new PrepareState();
        private final WheelState runningState = new RunningState();
        private final WheelState endState = new EndState();

        private WheelState wheelState = endState;

        /* Animation */
        private ObjectAnimator prepareAnimator;
        private Animator.AnimatorListener prepareEndListener;
        private ObjectAnimator runningAnimator;
        private ObjectAnimator endingAnimator;

        WheelState currentState() {
            return wheelState;
        }

        boolean isLooping() {
            return !(currentState() == endState && (endingAnimator == null || !endingAnimator.isRunning()));
        }

        private void switchTo(WheelState from, WheelState to) {
            if (from == to) return;
            wheelState = to;
            wheelState.running();
        }


        private void startEndAnimator(float stopDegree, Animator.AnimatorListener animatorEndListener) {
            if (endingAnimator == null) {
                endingAnimator = ObjectAnimator.ofFloat(
                        LuckyWheel.this,
                        "degree",
                        getDegree(),
                        stopDegree
                );
                endingAnimator.setInterpolator(new DecelerateInterpolator());
                endingAnimator.setDuration(3000L);
            }

            if (endingAnimator.isRunning()) {
                endingAnimator.cancel();
            }
            endingAnimator.removeAllListeners();
            endingAnimator.addListener(animatorEndListener);
            endingAnimator.setFloatValues(getDegree(), stopDegree);
            endingAnimator.start();
        }

        /**
         * 转盘开始转动的准备阶段. 显示加速转动的动画.
         */
        private class PrepareState implements WheelState {

            @Override
            public void start() {
                // Nothing to do
            }

            @Override
            public void stop(float stopDegree, Animator.AnimatorListener animatorEndListener) {
                prepareAnimator.removeAllListeners();
                startEndAnimator(stopDegree, animatorEndListener);
                switchTo(this, endState);
            }

            @Override
            public void running() {
                if (prepareEndListener == null) {
                    prepareEndListener = new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (currentState() == prepareState) {
                                switchTo(currentState(), runningState);
                            }
                        }
                    };
                }
                prepareAnimator.removeAllListeners();
                prepareAnimator.addListener(prepareEndListener);
            }
        }

        private class RunningState implements WheelState {

            @Override
            public void start() {
                // Nothing to do
            }

            @Override
            public void stop(float stopDegree, Animator.AnimatorListener animatorEndListener) {
                runningAnimator.cancel();
                startEndAnimator(stopDegree, animatorEndListener);
                switchTo(this, endState);
            }

            @Override
            public void running() {
                if (runningAnimator == null) {
                    runningAnimator = ObjectAnimator.ofFloat(
                            LuckyWheel.this,
                            "degree",
                            getDegree(),
                            getDegree() + 6 * 360f
                    );
                    runningAnimator.setInterpolator(new LinearInterpolator());
                    runningAnimator.setDuration(2000L);
                    runningAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    runningAnimator.setRepeatMode(ValueAnimator.RESTART);
                }

                runningAnimator.cancel();
                runningAnimator.setFloatValues(getDegree(), getDegree() + 5 * 360f);
                runningAnimator.start();
            }
        }

        private class EndState implements WheelState {

            @Override
            public void start() {
                if (prepareAnimator == null) {
                    prepareAnimator = ObjectAnimator.ofFloat(
                            LuckyWheel.this,
                            "degree",
                            getDegree(),
                            getDegree() + 3 * 360f
                    );
                    prepareAnimator.setDuration(2000L);
                    prepareAnimator.setInterpolator(new AccelerateInterpolator());
                }

                prepareAnimator.cancel();
                if (endingAnimator != null && endingAnimator.isRunning()) {
                    endingAnimator.cancel();
                }
                prepareAnimator.setFloatValues(getDegree(), getDegree() + 3 * 360f);
                prepareAnimator.start();
                switchTo(this, prepareState);
            }

            @Override
            public void stop(float stopDegree, Animator.AnimatorListener animatorEndListener) {
                // 不做任何动画, 直接变成需要设置的结果.
                if (endingAnimator != null && endingAnimator.isRunning()) {
                    endingAnimator.removeAllListeners();
                    endingAnimator.end();
                }
                setDegree(stopDegree);
                animatorEndListener.onAnimationEnd(null);
            }

            @Override
            public void running() {

            }
        }
    }

    /**
     * 转盘状态.
     */
    private interface WheelState {

        /**
         * 开始转动转盘.
         */
        void start();

        /**
         * 结束转动转盘.
         *
         * @see LuckyWheel#stopLoop(int, float, Animator.AnimatorListener)
         */
        void stop(float stopDegree, Animator.AnimatorListener animatorEndListener);

        void running();
    }
}
