package com.billin.www.commondmodual.glide;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.billin.www.commondmodual.R;

public class GlideTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_glide);

        ImageView imageView = findViewById(R.id.image);
        GlideApp.with(this)
                .load(new DiskCacheResources(
                        "https://pic.rmb.bdstatic.com/c86255e8028696139d3e3e4bb44c047b.png",
                        getDir("glide_resource_test", Context.MODE_PRIVATE).getPath()
                ))
//                .load("https://pic.rmb.bdstatic.com/c86255e8028696139d3e3e4bb44c047b.png")
                .into(imageView);
    }
}
