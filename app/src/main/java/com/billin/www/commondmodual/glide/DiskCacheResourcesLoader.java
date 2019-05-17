package com.billin.www.commondmodual.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.HttpUrlFetcher;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.LruArrayPool;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create by Billin on 2019/5/17
 */
public class DiskCacheResourcesLoader implements ModelLoader<DiskCacheResources, InputStream> {

    private static final String TAG = "DiskCacheResources";

    private Map<String, DiskCache> diskLruCacheMap = new ConcurrentHashMap<>();

    private ArrayPool arrayPool = new LruArrayPool(4 * 1024 * 1024);

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull DiskCacheResources resources,
                                               int width, int height, @NonNull Options options) {
        return new LoadData<>(resources, new InternalDataFetcher(resources, diskLruCacheMap, arrayPool));
    }

    @Override
    public boolean handles(@NonNull DiskCacheResources diskCacheResources) {
        return true;
    }

    private static class InternalDataFetcher implements DataFetcher<InputStream> {

        /**
         * 默认每个缓存目录为 64 MB.
         */
        private static final int DEFAULT_SIZE = 64 * 1024 * 1024;

        /**
         * 默认从网络中获取图片超时时间
         */
        private static final int DEFAULT_TIMEOUT = 3500;

        private final Map<String, DiskCache> diskCacheMap;

        private final DiskCacheResources resources;

        private final ArrayPool arrayPool;

        private InputStream stream;

        InternalDataFetcher(DiskCacheResources resources,
                            Map<String, DiskCache> diskCacheMap,
                            ArrayPool arrayPool) {
            this.diskCacheMap = diskCacheMap;
            this.resources = resources;
            this.arrayPool = arrayPool;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull final DataCallback<? super InputStream> callback) {

            final DiskCache diskLruCache;
            if ((diskCacheMap.get(resources.getCacheFold())) == null) {
                File cacheFold = new File(resources.getCacheFold());

                if (!cacheFold.isDirectory()) {
                    if (cacheFold.mkdirs()) {
                        Log.e(TAG, "buildLoadData: cannot make cache dir!!!");
                        callback.onLoadFailed(new RuntimeException("buildLoadData: cannot make cache dir!!!"));
                        return;
                    }
                }

                // 保证只创建一个 DiskLruCacheWrapper
                synchronized (InternalDataFetcher.class) {
                    if (diskCacheMap.get(resources.getCacheFold()) == null) {
                        diskCacheMap.put(resources.getCacheFold(), DiskLruCacheWrapper.create(cacheFold, DEFAULT_SIZE));
                    }
                }
            }

            diskLruCache = diskCacheMap.get(resources.getCacheFold());

            File file = Objects.requireNonNull(diskLruCache).get(resources);
            if (file == null || !file.exists()) {
                final HttpUrlFetcher remoteUrlFetcher = new HttpUrlFetcher(new GlideUrl(resources.getOriginUrl()), DEFAULT_TIMEOUT);
                remoteUrlFetcher.loadData(Priority.NORMAL, new DataCallback<InputStream>() {
                    @Override
                    public void onDataReady(@Nullable final InputStream data) {
                        if (data == null) {
                            Exception e = new RuntimeException("data is null");
                            Log.e(TAG, "onDataReady: ", e);
                            callback.onLoadFailed(e);
                            return;
                        }

                        diskLruCache.put(resources, new StreamWriter(arrayPool, data));
                        File newFile = diskLruCache.get(resources);
                        try {
                            stream = new FileInputStream(newFile);
                            callback.onDataReady(stream);
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "onDataReady: ", e);
                            callback.onLoadFailed(e);
                        }
                    }

                    @Override
                    public void onLoadFailed(@NonNull Exception e) {
                        callback.onLoadFailed(e);
                        remoteUrlFetcher.cleanup();
                    }
                });
            } else {
                try {
                    stream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "loadData: ", e);
                    callback.onLoadFailed(e);
                    return;
                }

                callback.onDataReady(stream);
            }
        }

        @Override
        public void cleanup() {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }

        @Override
        public void cancel() {
            // nothing to do
        }

        @NonNull
        @Override
        public Class<InputStream> getDataClass() {
            return InputStream.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.REMOTE;
        }
    }

    public static class Factory implements ModelLoaderFactory<DiskCacheResources, InputStream> {

        @NonNull
        @Override
        public ModelLoader<DiskCacheResources, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new DiskCacheResourcesLoader();
        }

        @Override
        public void teardown() {
            // nothing to do
        }
    }
}
