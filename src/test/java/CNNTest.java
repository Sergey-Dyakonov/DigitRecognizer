import com.knubisoft.cnn.ConvolutionalNeuralNetwork;
import com.knubisoft.cnn.LabeledImage;
import com.knubisoft.utils.ImageProcessorUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CNNTest {
    private static final ConvolutionalNeuralNetwork cnn = new ConvolutionalNeuralNetwork();

    @BeforeAll
    public static void initCNN() {
        cnn.init();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/resources/1.png",
            "src/test/resources/2.png",
            "src/test/resources/3.png",
            "src/test/resources/4.png",
            "src/test/resources/5.png",
            "src/test/resources/6.png",
            "src/test/resources/7.png",
            "src/test/resources/8.png",
            "src/test/resources/9.png",
            "src/test/resources/0.png",})
    public void correctPredictionsCNN(String filePath) {
        File imageFile = new File(filePath);
        LabeledImage labeledImage = prepareLabeledImage(imageFile);
        int predicted = cnn.predict(labeledImage);
        int expected = Integer.parseInt(imageFile.getName().split(".png")[0]);
        assertEquals(expected, predicted, "Failed to recognize digit from test images!");
    }

    @SneakyThrows
    private LabeledImage prepareLabeledImage(File imageFile) {
        BufferedImage image = ImageIO.read(imageFile);
        Image scaled = ImageProcessorUtil.scale(image);
        BufferedImage scaledBuffered = ImageProcessorUtil.toBufferedImage(scaled);
        double[] scaledPixels = ImageProcessorUtil.toVector(scaledBuffered);
        return new LabeledImage(scaledPixels);
    }
}
