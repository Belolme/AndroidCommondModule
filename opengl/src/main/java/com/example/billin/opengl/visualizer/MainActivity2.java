package com.example.billin.opengl.visualizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.billin.opengl.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo to show how to use VisualizerView
 */
public class MainActivity2 extends AppCompatActivity {
    private MediaPlayer mPlayer;
    private MediaPlayer mSilentPlayer;  /* to avoid tunnel player issue */
    private GLVisualizerView mVisualizerView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.main);

        mVisualizerView = findViewById(R.id.visualizerView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVisualizerView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        mVisualizerView.onResume();
    }

    @Override
    protected void onDestroy() {
        cleanUp();
        super.onDestroy();
    }

    private void init() {
        mPlayer = MediaPlayer.create(this, R.raw.japanese);

        mPlayer.setLooping(true);

        askPermission();
        mPlayer.start();

        // We need to link the visualizer view to the media player so that
        // it displays something

        addCircleRenderer();
        mVisualizerView.bindSessionId(mPlayer.getAudioSessionId());

        // Start with just line renderer
    }

    private boolean askPermission() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int RECORD_AUDIO = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            if (RECORD_AUDIO != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
            } else
                return false;
        } else
            return false;
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {

            boolean result = true;
            for (int i = 0; i < permissions.length; i++) {
                result = result && grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
            if (!result) {

                Toast.makeText(this, "授权结果（至少有一项没有授权），result=" + result, Toast.LENGTH_LONG).show();
                // askPermission();
            } else {
                //授权成功
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void cleanUp() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        if (mSilentPlayer != null) {
            mSilentPlayer.release();
            mSilentPlayer = null;
        }
    }


    /**
     * 迷幻水波
     */
    private void addRippleRenderers() {
    }

    private void addCircleLineRenderer() {
    }

    /**
     * 孤独星球
     */
    private void addCircleRenderer() {
        mVisualizerView.setRender(new LittleBallRender(this));
    }

    /**
     * 动感音阶
     */
    private void addCicleBarRenderer() {

    }

    // Actions for buttons defined in xml
    public void startPressed(View view) throws IllegalStateException, IOException {
        if (mPlayer.isPlaying()) {
            return;
        }
        mPlayer.prepare();
        mPlayer.start();
    }

    public void stopPressed(View view) {
        mPlayer.stop();
    }

    /**
     * 迷幻水波
     */
    public void ripple(View view) {
        addRippleRenderers();
    }

    public void circlePressed(View view) {
        addCircleRenderer();
    }

    /**
     * 孤独星球
     */
    public void circleLine(View view) {
        addCircleLineRenderer();
    }

    /**
     * 动感音阶
     */
    public void circleBar(View view) {
        addCicleBarRenderer();
    }

    public void clearPressed(View view) {
        mVisualizerView.setRender(null);
    }
}