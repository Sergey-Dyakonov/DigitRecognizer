package com.knubisoft;

import lombok.SneakyThrows;
import org.apache.commons.lang.ArrayUtils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class IdxReader {
    private final static String INP_IMG_PATH = "resources/train-images.idx3-ubyte";
    private final static String INP_LBL_PATH = "resources/train-labels.idx1-ubyte";
    private final static String INP_IMG_TEST_PATH = "resources/t10k-images.idx3-ubyte";
    private final static String INP_LBL_TEST_PATH = "resources/t10k-labels.idx1-ubyte";
    private final static int VECTOR_DIMENSION = 784; // square of an image (28*28)

    public static List<LabeledImage> loadData(int size){
        return getLabeledImages(INP_IMG_PATH, INP_LBL_PATH, size);
    }

    public static List<LabeledImage> loadTestData(int size){
        return getLabeledImages(INP_IMG_TEST_PATH, INP_LBL_TEST_PATH, size);
    }

    @SneakyThrows
    private static List<LabeledImage> getLabeledImages(String inpImgPath, String inpLblPath, int size) {
        List<LabeledImage> labeledImages = new ArrayList<>(size);
        try (FileReader readerImg = new FileReader(inpImgPath);
             FileReader readerLbl = new FileReader(inpImgPath)) {

            //skipping redundant data (see data description)
            readerImg.skip(16);
            readerLbl.skip(8);

            List<Double> imgPixels = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                imgPixels = new ArrayList<>();
                for (int j = 0; j < VECTOR_DIMENSION; j++) {
                    //converting image matrix to vector
                    imgPixels.add((double) readerImg.read());
                }
                int label = readerLbl.read();

                //creating object for image (array of ints) and its label
                labeledImages.add(new LabeledImage(label,
                        ArrayUtils.toPrimitive(imgPixels.toArray(new Double[0]))));
            }
        }
        return labeledImages;
    }
}
