package com.billin.www.library.glide;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * 需要缓存到其他目录的图片资源
 * <p>
 * Create by Billin on 2019/5/17
 */
public class DiskCacheResources implements Key {

    private String originUrl;

    private String cacheFold;

    public DiskCacheResources(String originUrl, String cacheFold) {
        this.originUrl = originUrl;
        this.cacheFold = cacheFold;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public String getCacheFold() {
        return cacheFold;
    }

    public void setCacheFold(String cacheFold) {
        this.cacheFold = cacheFold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiskCacheResources that = (DiskCacheResources) o;
        return Objects.equals(originUrl, that.originUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originUrl);
    }

    @NotNull
    @Override
    public String toString() {
        return "DiskCacheResources{" +
                "originUrl=" + originUrl +
                ", cacheFold=" + cacheFold +
                '}';
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(originUrl.getBytes(Charset.forName("UTF-8")));
    }
}
