package com.example.billin.opengl.visualizer;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 处理 visualizer 原始数据的工具
 * <p>
 * Create by Billin on 2019/1/23
 */
public class VisualizerCalculateUtil {

    private static final Interpolator sValueInterpolator = new AccelerateDecelerateInterpolator();

    /**
     * 处理原始 fft 数组数据并转换成频率强度数据，长度将会变成传入长度的一半。
     */
    public static int processFftMagnitudeData(float[] fft, int length) {
        for (int i = 0; i < length; i += 2) {
            float magnitude = (float) Math.hypot(fft[i], fft[i + 1]);

            fft[i / 2] = magnitude;
        }

        return length / 2;
    }

    /**
     * 裁剪前面一段和后面一段数据
     *
     * @throws IllegalArgumentException 如果剪切的长度大于数据的长度会抛出这个异常
     */
    public static int cropOriginFftData(float[] fft, int cropStartLength, int cropEndLength, int length) {
        if (cropEndLength + cropStartLength >= length) {
            throw new IllegalArgumentException("crop length is grater than data length!");
        }

        System.arraycopy(fft, cropStartLength, fft, 0,
                length - cropEndLength - cropStartLength);

        return length - cropStartLength - cropEndLength;
    }

    /**
     * 把小于 filterMinValue 的值设置为 0
     */
    public static int filterMinValueData(float[] fft, float filterMinValue, int length) {
        for (int i = 0; i < length; i++) {
            if (fft[i] < filterMinValue) fft[i] = 0;
        }

        return length;
    }

    /**
     * @see #flatData(float[], MappingFormula, int, int)
     */
    public static int flatData(float[] originData, MappingFormula mappingFormula, int length) {
        return flatData(originData, mappingFormula, 0, length);
    }

    /**
     * 把数据的 Y 值映射根据 mappingFormula 映射到相应范围中去.
     */
    public static int flatData(float[] originData, MappingFormula mappingFormula, int startI, int length) {
        int endI = length + startI;
        for (int i = startI; i < endI; i++) {
            originData[i] = mappingFormula.map(originData[i]);
        }

        return length;
    }

    /**
     * 把 fft 杂乱的数据做平滑处理，处理过程如下：
     * 1. 使用中值滤波函数根据传入的 rangeCount 分组过滤数据;
     * 2. 得到每一组的中值后，通过 sin 函数对每一组的数据做平滑过渡.
     * <p>
     * 注意：如果 length % rangeCount != 0, 那么最后的几个数据会被直接舍弃.
     * 该方法最大复杂度为 O(N^2)
     *
     * @param groupCount 平滑数据的组数
     * @param round      数据是否首尾相接。如果是，那么将会对第一组数据和最后一组数据做平滑处理，
     *                   否则，直接平滑到 0.
     * @throws IllegalArgumentException 如果传入的数据不足以分 rangeCount 组，将会抛出这个异常
     */
    public static int smoothData(float[] fft, int groupCount, boolean round, int length) {
        return smoothData(fft, groupCount, round, 0, length);
    }

    public static int smoothData(float[] fft, int groupCount, boolean round, int startI, int length) {
        int groupLength = length / groupCount;
        if (groupLength == 0) {
            throw new IllegalArgumentException("rangeCount grater than data length!");
        }

        // 滤波
//        boolean isEven = groupLength % 2 == 0;
//        for (int i = 0; i < groupCount; i++) {
//            int mid = getMidValues(fft, groupLength * i + startI,
//                    groupLength * i + groupLength + startI);
//
//            // 如果是复数，把前一个数也变成中位数
////            if (isEven) {
////                fft[mid - 1] = fft[mid];
////            }
//        }

        // 平滑
        for (int i = 0; i < groupCount - 1; i++) {
            int startIndex = (i * groupLength) + groupLength / 2 + startI;
            int endIndex = (i + 1) * groupLength + groupLength / 2 + startI;
            for (int j = startIndex; j < endIndex; j++) {
                fft[j] = getSinValue(fft[startIndex], fft[endIndex],
                        ((float) (j - startIndex)) / groupLength);
            }
        }

        int resultLength = length - (length % groupCount);
        if (round) {
            int startIndex = (groupCount - 1) * groupLength + groupLength / 2 + startI;
            int endIndex = groupLength / 2 + startI;
            for (int i = startIndex, j = 0; j < groupLength; j++, i++) {
                int index = (i - startI) % resultLength + startI;
                fft[index] = getSinValue(fft[startIndex], fft[endIndex],
                        ((float) j) / groupLength);
            }
        }

        return resultLength;
    }

    private static float getSinValue(float start, float end, float factor) {
        return (end - start) * sValueInterpolator.getInterpolation(factor) + start;
    }

    /**
     * 获取数组某段中的中位数，数组中的位置标识为 [startIndex, endIndex).
     * 该算法的最大复杂度为 O(N^2)，平均复杂度为 O(N*logN).
     * <p>
     * 注意：调用该方法会改变数据的数据顺序.
     *
     * @return 该中位数的位置
     */
    private static int getMidValues(float[] array, int startIndex, int endIndex) {
        int mid = (endIndex - startIndex) / 2 + startIndex;
        int leftP = startIndex;
        int rightP = endIndex;
        while (true) {
            int midP = moveToMiddle(array, leftP, rightP);
            if (midP == mid) {
                break;
            } else if (midP > mid) {
                rightP = midP;
            } else {
                leftP = midP + 1;
            }
        }

        return mid;
    }

    /**
     * 把传入数组的第一个数据移动到左边的数据都比它小，右边的数据都比它大的地方。
     *
     * @return 第一个数据最后的位置
     */
    private static int moveToMiddle(float[] a, int start, int end) {
        float v = a[start];

        int leftP = start + 1;
        int rightP = end - 1;
        while (leftP < rightP) {
            while (a[rightP] > v && leftP < rightP) rightP--;
            while (a[leftP] <= v && leftP < rightP) leftP++;

            float tmp = a[rightP];
            a[rightP] = a[leftP];
            a[leftP] = tmp;
        }

        if (leftP < end && a[leftP] < v) {
            a[start] = a[leftP];
            a[leftP] = v;
            return leftP;
        }
        return start;
    }

}
