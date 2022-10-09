package com.knubisoft.utils;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Provides operations for image processing for CNN
 */
public class ImageProcessorUtil {
    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img Image to be converted into BufferedImage
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bimage.createGraphics();
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();
        return bimage;
    }

    /**
     * Scales image to appropriate size <strong>(28x28)</strong> for compatibility with neural network trained on MNIST dataset
     *
     * @param img BufferedImage to be scaled
     * @return scaled Image to <strong>28x28</strong> size which is suitable to trained neural network
     * @see com.mortennobel.imagescaling.ResampleOp
     */
    public static Image scale(BufferedImage img) {
        ResampleOp resize = new ResampleOp(28, 28);
        resize.setFilter(ResampleFilters.getLanczos3Filter());
        return resize.filter(img, null);
    }

    /**
     * Converts given BufferedImage to one dimensional array of doubles.
     * This array represents gray (one channel for color) image.
     * <p>
     * Each pixel of provided image is converted to Color,
     * then its values of red, green and blue channels (RGB model)
     * are used to compute value for one channel color (which is gray).
     * To compute this value arithmetical mean is used.
     * <p>
     * Values in returned array are in range [0, 255]
     * because it's the <strong>max</strong> and <strong>min</strong> values for color channel in RGB model.
     *
     * @param img BufferedImage to be converted into a <i>vector</i> (one dimensional array)
     * @return one dimensional array of doubles which represents converted gray image
     */
    public static double[] toVector(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        double[] imgGray = new double[width * height];
        int index = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color color = new Color(img.getRGB(j, i), true);
                imgGray[index++] = 255 - (color.getRed() + color.getGreen() + color.getBlue()) / 3d;
            }
        }
        return imgGray;
    }
}
