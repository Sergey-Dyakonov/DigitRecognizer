package com.knubisoft;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@ToString
public class LabeledImage implements Serializable {
    private final Double[] meanNormalizedPixel;
    private final double[] pixels;
    private final Vector features;
    @Setter
    private double label;

    public LabeledImage(int label, double[] pixels) {
        this.pixels = pixels;
        this.label = label;
        meanNormalizedPixel = meanNormalizedFeatures(pixels);
        features = Vectors.dense(ArrayUtils.toPrimitive(meanNormalizedPixel));
    }

    private Double[] meanNormalizedFeatures(double[] pixels) {
        double min = Arrays.stream(pixels).min().orElse(0);
        double max = Arrays.stream(pixels).max().orElse(0);
        double sum = Arrays.stream(pixels).sum();
        double mean = sum / pixels.length;
        List<Double> norms = new ArrayList<>();
        for (double pixel : pixels) {
            norms.add((pixel - mean) / (max - min));
        }
        return norms.toArray(new Double[0]);
    }
}
