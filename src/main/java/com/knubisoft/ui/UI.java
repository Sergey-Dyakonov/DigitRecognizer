package com.knubisoft.ui;

import com.knubisoft.cnn.ConvolutionalNeuralNetwork;
import com.knubisoft.utils.ImageProcessorUtil;
import com.knubisoft.cnn.LabeledImage;
import freemarker.log.Logger;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;

/**
 * Class for creating and providing GUI
 */
public class UI {
    private final static Logger LOG = Logger.getLogger(UI.class.getName());
    private final static int WIDTH = 1200;
    private final static int HEIGHT = 600;
    private final static int TRAIN_SIZE = 30_000;
    private final static int TEST_SIZE = 10_000;
    private final ConvolutionalNeuralNetwork convolutionalNeuralNetwork = new ConvolutionalNeuralNetwork();
    private DrawArea drawArea;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JPanel drawAndDigitPredictionPanel;
    private JSpinner trainField;
    private final Font sansSerifBold = new Font("SansSerif", Font.BOLD, 18);
    private JSpinner testField;
    private JPanel resultPanel;

    @SneakyThrows
    public UI() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, 18)));
        UIManager.put("ProgressBar.font", new FontUIResource(new Font("Dialog", Font.BOLD, 18)));
        convolutionalNeuralNetwork.init();
    }

    /**
     * Initiate GUI and adds all necessary elements
     */
    public void initUI() {
        mainFrame = createMainFrame();
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        addTrainPanel();

        drawAndDigitPredictionPanel = new JPanel(new GridLayout());
        addActionPanel();
        addDrawAreaAndPredictionArea();
        mainPanel.add(drawAndDigitPredictionPanel, BorderLayout.CENTER);

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }

    /**
     * Creates and adds to main frame panel with spinners to specify amount of data for model training
     */
    private void addTrainPanel() {
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton train = new JButton("Train");
        train.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure, training may take a long time?") == JOptionPane.OK_OPTION) {
                ProgressBar bar = new ProgressBar(mainFrame);
                SwingUtilities.invokeLater(() -> bar.showProgressBar("Training may take a while..."));
                Executors.newCachedThreadPool().submit(() -> {
                    LOG.info("Start CNN training");
                    convolutionalNeuralNetwork.train((Integer) trainField.getValue(), (Integer) testField.getValue());
                    LOG.info("End CNN training");
                    bar.setVisible(false);
                });
            }
        });
        topPanel.add(train);

        JLabel trainLbl = new JLabel("Training data");
        trainLbl.setFont(sansSerifBold);
        topPanel.add(trainLbl);
        SpinnerNumberModel modelTrainSize = new SpinnerNumberModel(TRAIN_SIZE, 10_000, 60_000, 1000);
        trainField = new JSpinner(modelTrainSize);
        trainField.setFont(sansSerifBold);
        topPanel.add(trainField);

        JLabel testLbl = new JLabel("Testing data");
        testLbl.setFont(sansSerifBold);
        topPanel.add(trainLbl);
        SpinnerNumberModel modelTestSize = new SpinnerNumberModel(TEST_SIZE, 1000, 10_000, 500);
        testField = new JSpinner(modelTestSize);
        testField.setFont(sansSerifBold);
        topPanel.add(testField);

        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    /**
     * Creates and establishes main frame of the app
     *
     * @return JFrame main frame
     */
    private JFrame createMainFrame() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Digit Recognizer");
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setSize(WIDTH, HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });

        ImageIcon imageIcon = new ImageIcon("src/main/resources/icon.png");
        mainFrame.setIconImage(imageIcon.getImage());

        return mainFrame;
    }

    /**
     * Adds panel with canvas for drawing and panel for predicted value output
     */
    private void addDrawAreaAndPredictionArea() {
        drawArea = new DrawArea();
        drawAndDigitPredictionPanel.add(drawArea);

        resultPanel = new JPanel();
        resultPanel.setLayout(new GridLayout());
        drawAndDigitPredictionPanel.add(resultPanel);
    }

    /**
     * Creates and adds to the main frame panel with buttons for interacting with CNN
     */
    private void addActionPanel() {
        JButton recognize = new JButton("Recognize digit");
        recognize.addActionListener(e -> {
            Image drawImage = drawArea.getImage();
            BufferedImage sbi = ImageProcessorUtil.toBufferedImage(drawImage);
            Image scaled = ImageProcessorUtil.scale(sbi);
            BufferedImage scaledBuffered = ImageProcessorUtil.toBufferedImage(scaled);
            double[] scaledPixels = ImageProcessorUtil.toVector(scaledBuffered);
            LabeledImage labeledImage = new LabeledImage(scaledPixels);

            int predict = convolutionalNeuralNetwork.predict(labeledImage);
            JLabel prediction = new JLabel(String.valueOf(predict));
            prediction.setForeground(Color.red);
            prediction.setFont(new Font("SansSerif", Font.BOLD, 256));

            resultPanel.removeAll();
            resultPanel.add(prediction);
            resultPanel.updateUI();
        });

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            drawArea.setImage(null);
            drawArea.repaint();
            drawAndDigitPredictionPanel.updateUI();

            resultPanel.removeAll();
            resultPanel.updateUI();
        });

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        buttonsPanel.add(recognize);
        buttonsPanel.add(clear);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
    }
}
