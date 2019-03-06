package com.example.billin.opengl.visualizer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Toast;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 使用 OpenGL 进行音频频谱图的渲染
 * <p>
 * Create by Billin on 2019/2/21
 */
public class GLVisualizerView extends GLSurfaceView implements GLSurfaceView.Renderer {

    /**
     * 音频捕捉频率，单位为 Hz/s.
     */
    private static final int CAPTURE_RATE = 10;

    /**
     * 音频捕捉最大值.
     */
    private static final int CAPTURE_SIZE = Visualizer.getCaptureSizeRange()[1];

    /**
     * 音频频率捕捉的间隔时间，单位为 ns.
     */
    private static final int CAPTURE_INTERVAL_TIME = 1000000000 / CAPTURE_RATE;
    private static final String TAG = "GLVisualizerView";
    private long mLastCaptureTime = Long.MIN_VALUE;
    private Visualizer mVisualizer;
    private byte mOriginFftData[] = new byte[CAPTURE_SIZE];
    private int mAnimValueLength;
    private float mAnimValue[] = new float[CAPTURE_SIZE];
    private float mAnimFrameValue[][] = new float[2][CAPTURE_SIZE];
    private VisualizerRender mVisualizerRender;
    private boolean mSurfaceCreated = false;
    private int mWidth;
    private int mHeight;

    public GLVisualizerView(Context context) {
        super(context);
        init();
    }

    public GLVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);

        // Check if the system supports OpenGL ES 2.0.
        ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000 || (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86"));

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            setEGLContextClientVersion(2);

            // Assign our renderer.
//            glView.setEGLConfigChooser(MultisampleConfigChooser())
            setRenderer(this);
        } else {
            /*
             * This is where you could create an OpenGL ES 1.x compatible
             * renderer if you wanted to support both ES 1 and ES 2. Since
             * we're not doing anything, the app will crash if the device
             * doesn't support OpenGL ES 2.0. If we publish on the market, we
             * should also add the following to AndroidManifest.xml:
             *
             * <uses-feature android:glEsVersion="0x00020000"
             * android:required="true" />
             *
             * This hides our app from those devices which don't support OpenGL
             * ES 2.0.
             */
            Toast.makeText(getContext(),
                    "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
        }

        mVisualizerRender = new LittleBallRender(getContext());
    }

    public void bindSessionId(final int sessionId) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                unBindSessionIdDirect();

                mVisualizer = new Visualizer(sessionId);
                Equalizer equalizer = new Equalizer(0, sessionId);
                equalizer.setEnabled(true);
                mVisualizer.setCaptureSize(CAPTURE_SIZE);

                mVisualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
                mVisualizer.setEnabled(true);
            }
        });
    }

    private void unBindSessionIdDirect() {
        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
            mVisualizer.setDataCaptureListener(null, 0, false, false);
            mVisualizer.release();
        }
    }

    public void unBindSessionId() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                unBindSessionIdDirect();
            }
        });
    }

    public void setRender(final VisualizerRender render) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                setVisualizerRenderer(render);
            }
        });
    }

    private void setVisualizerRenderer(VisualizerRender render) {
        mVisualizerRender = render;
        if (mVisualizerRender == null) return;
        if (mSurfaceCreated) callSurfaceCreated(mVisualizerRender);
        configRenderBound(render);
    }

    private void configRenderBound(final VisualizerRender render) {
        if (render != null) render.resize(mWidth, mHeight);

    }

    private void callSurfaceCreated(VisualizerRender render) {
        if (render != null) render.surfaceCreated();
    }

    private void captureOriginValue() {
        long currentTime = System.nanoTime();
        long intervalTime = currentTime - mLastCaptureTime;
        if (intervalTime > 0 && intervalTime < CAPTURE_INTERVAL_TIME) {
            return;
        }
        mLastCaptureTime = currentTime;

        mVisualizer.getFft(mOriginFftData);

        // 获取动画最终显示的数据
        // 没办法，搜了很久没有找到更好更高效率的方法
        for (int i = 0; i < mOriginFftData.length; i++) {
            mAnimFrameValue[1][i] = (float) mOriginFftData[i];
        }

        mAnimValueLength = mVisualizerRender.processData(mAnimFrameValue[1], mAnimFrameValue[1].length);

        // 设置动画起始数据
        System.arraycopy(mAnimValue, 0, mAnimFrameValue[0], 0, mAnimValueLength);
    }

    private void animValueEvaluate() {
        float fraction = (System.nanoTime() - mLastCaptureTime) * 1f / CAPTURE_INTERVAL_TIME;
        for (int i = 0; i < mAnimValueLength; i++) {
            float start = mAnimFrameValue[0][i];
            float end = mAnimFrameValue[1][i];
            mAnimValue[i] = (start + (fraction * (end - start)));
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSurfaceCreated = true;

        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        callSurfaceCreated(mVisualizerRender);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        GLES20.glViewport(0, 0, width, height);
        configRenderBound(mVisualizerRender);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        long startTime = System.currentTimeMillis();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (mVisualizerRender == null || mVisualizer == null) return;

        captureOriginValue();
        animValueEvaluate();
        mVisualizerRender.draw(mAnimValue);
//        Log.d(TAG, "onDrawFrame: " + Arrays.toString(mAnimValue));
//        long endTime = System.currentTimeMillis();

//        Log.d(TAG, "onDrawFrame: " + (endTime - startTime));
    }

//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        GLES20.glClearColor(0f, 0f, 0f, 0f);
//
//        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        mVisualizerRender.surfaceCreated();
//    }
//
//    @Override
//    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        GLES20.glViewport(0, 0, width, height);
//        mVisualizerRender.resize(width, height);
//    }
//
//    @Override
//    public void onDrawFrame(GL10 gl) {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//
//        mVisualizerRender.draw(Data._1);
//    }
}
