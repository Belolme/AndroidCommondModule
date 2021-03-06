package com.billin.www.library.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

/**
 * Create by Billin on 2019/5/17
 */
@GlideModule
public class GlideAppModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(DiskCacheResources.class, InputStream.class, new DiskCacheResourcesLoader.Factory());
    }
}
