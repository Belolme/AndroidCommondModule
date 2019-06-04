package com.billin.www.commondmodual.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.billin.www.commondmodual.R
import kotlinx.android.synthetic.main.activity_ui.*

private const val TAG = "UITestActivity"

/**
 * Create by Billin on 2019/6/3
 */
class UITestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui)

        colorPicker.apply {
            hue = 180f
//            ringAndRadiusRatio = 0.2f
        }.setColorChangeListener {
            Log.d(TAG, "onCreate: $it")
        }

        testButton.setOnClickListener { colorPicker.ringAndRadiusRatio += 0.1f }
    }

}