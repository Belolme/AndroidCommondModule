package com.billin.www.commondmodual.glide;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.cache.DiskCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 以下代码来源于 {@link com.bumptech.glide.load.model.StreamEncoder}
 * <p>
 * Create by Billin on 2019/5/17
 */
public class StreamWriter implements DiskCache.Writer {

    private static final String TAG = "StreamWriter";

    private final ArrayPool byteArrayPool;

    private final InputStream data;

    public StreamWriter(ArrayPool byteArrayPool, InputStream inputStream) {
        this.byteArrayPool = byteArrayPool;
        this.data = inputStream;
    }

    @Override
    public boolean write(@NonNull File file) {
        byte[] buffer = byteArrayPool.get(ArrayPool.STANDARD_BUFFER_SIZE_BYTES, byte[].class);
        boolean success = false;
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int read;
            while ((read = data.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.close();
            success = true;
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Failed to encode data onto the OutputStream", e);
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // Do nothing.
                }
            }
            byteArrayPool.put(buffer);
        }
        return success;
    }
}
