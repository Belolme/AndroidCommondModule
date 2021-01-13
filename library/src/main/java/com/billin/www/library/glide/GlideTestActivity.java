package com.billin.www.library.glide;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.billin.www.library.R;

public class GlideTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_glide);

        ImageView imageView = findViewById(R.id.image);
        GlideApp.with(this)
                .load(new DiskCacheResources(
                        "https://dgit.com/wp-content/uploads/2017/05/HDR-Sample.jpg",
                        getDir("glide_resource_test", Context.MODE_PRIVATE).getPath()
                ))
//                .load("https://pic.rmb.bdstatic.com/c86255e8028696139d3e3e4bb44c047b.png")
                .into(imageView);
    }
}
