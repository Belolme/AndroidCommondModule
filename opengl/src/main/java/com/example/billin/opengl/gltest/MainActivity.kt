package com.example.billin.opengl.gltest

import android.app.ActivityManager
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.billin.opengl.R
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

class MainActivity : AppCompatActivity() {

    private lateinit var glView: GLSurfaceView

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)

//        val animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f)
//        animator.repeatMode = ValueAnimator.REVERSE
//        animator.repeatCount = ValueAnimator.INFINITE
//        animator.duration = 2000L
//        animator.start()

        glView = findViewById(R.id.glView)
        glView.setZOrderOnTop(true)
        glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        glView.holder.setFormat(PixelFormat.RGBA_8888)

        // Check if the system supports OpenGL ES 2.0.
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager
                .deviceConfigurationInfo
        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        val supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000 || (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86"))

        val renderer = GlTestRenderer(this)

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glView.setEGLContextClientVersion(2)

            // Assign our renderer.
//            glView.setEGLConfigChooser(MultisampleConfigChooser())
            glView.setRenderer(renderer)
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
            Toast.makeText(
                    this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }
}

class MultisampleConfigChooser : GLSurfaceView.EGLConfigChooser {

    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig? {
        val attribs = intArrayOf(
                EGL10.EGL_LEVEL,
                0,
                EGL10.EGL_RENDERABLE_TYPE,
                4, // EGL_OPENGL_ES2_BIT
                EGL10.EGL_COLOR_BUFFER_TYPE,
                EGL10.EGL_RGB_BUFFER,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_RED_SIZE,
                8,
                EGL10.EGL_GREEN_SIZE,
                8,
                EGL10.EGL_BLUE_SIZE,
                8,
                EGL10.EGL_DEPTH_SIZE,
                16,
                EGL10.EGL_SAMPLE_BUFFERS,
                1,
                EGL10.EGL_SAMPLES,
                4, // This is for 4x MSAA.
                EGL10.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val configCounts = IntArray(1)
        egl.eglChooseConfig(display, attribs, configs, 1, configCounts)

        return if (configCounts[0] == 0) {
            // Failed! Error handling.
            null
        } else {
            configs[0]
        }
    }
}