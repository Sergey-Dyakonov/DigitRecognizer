package com.knubisoft.cnn;

import java.io.Serializable;

/**
 * Represents an image converted to one dimensional array with one color chanel
 */
public record LabeledImage(double[] pixels) implements Serializable {
}
