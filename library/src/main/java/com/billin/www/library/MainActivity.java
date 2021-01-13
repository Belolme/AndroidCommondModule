package com.billin.www.library;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.save_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View root = findViewById(android.R.id.content);
                Bitmap bitmap = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                root.draw(canvas);

                ImageView preview = findViewById(R.id.preview);
                preview.setImageBitmap(bitmap);
            }
        });
    }
}
