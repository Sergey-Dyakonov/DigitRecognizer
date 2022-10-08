package com.knubisoft.ui;

import com.knubisoft.ConvolutionalNeuralNetwork;
import com.knubisoft.ImageProcessorUtil;
import com.knubisoft.LabeledImage;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;

public class UI {
    private final static Logger LOG = LoggerFactory.getLogger(UI.class);
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
    private SpinnerNumberModel modelTestSize;
    private SpinnerNumberModel modelTrainSize;
    private JSpinner testField;
    private JPanel resultPanel;

    @SneakyThrows
    public UI() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, 18)));
        UIManager.put("ProgressBar.font", new FontUIResource(new Font("Dialog", Font.BOLD, 18)));
        convolutionalNeuralNetwork.init();
    }

    public void initUI() {
        mainFrame = createMainFrame();
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        addTopPanel();

        drawAndDigitPredictionPanel = new JPanel(new GridLayout());
        addActionPanel();
        addDrawAreaAndPredictionArea();
        mainPanel.add(drawAndDigitPredictionPanel, BorderLayout.CENTER);

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }

    private void addTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton train = new JButton("Train");
        train.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure, training reqiures >10GB memory and more than 1 hour?") == JOptionPane.OK_OPTION) {
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
        modelTrainSize = new SpinnerNumberModel(TRAIN_SIZE, 10_000, 60_000, 1000);
        trainField = new JSpinner(modelTrainSize);
        trainField.setFont(sansSerifBold);
        topPanel.add(trainField);

        JLabel testLbl = new JLabel("Test data");
        testLbl.setFont(sansSerifBold);
        topPanel.add(trainLbl);
        modelTestSize = new SpinnerNumberModel(TEST_SIZE, 1000, 10_000, 500);
        testField = new JSpinner(modelTestSize);
        testField.setFont(sansSerifBold);
        topPanel.add(testField);

        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

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

//        ImageIcon imageIcon = new ImageIcon("icon.png");
//        mainFrame.setIconImage(imageIcon.getImage());

        return mainFrame;
    }

    private void addDrawAreaAndPredictionArea() {
        drawArea = new DrawArea();
        drawAndDigitPredictionPanel.add(drawArea);
        resultPanel = new JPanel();
        resultPanel.setLayout(new GridLayout());
        drawAndDigitPredictionPanel.add(resultPanel);
    }

    private void addActionPanel() {
        JButton recognize = new JButton("Recognize digit");
        recognize.addActionListener(e -> {
            Image drawImage = drawArea.getImage();
            BufferedImage sbi = ImageProcessorUtil.toBufferedImage(drawImage);
            Image scaled = ImageProcessorUtil.scale(sbi);
            BufferedImage scaledBuffered = ImageProcessorUtil.toBufferedImage(scaled);
            double[] scaledPixels = ImageProcessorUtil.toVector(scaledBuffered);
            LabeledImage labeledImage = new LabeledImage(0, scaledPixels);

            convolutionalNeuralNetwork.init();
            int predict = convolutionalNeuralNetwork.predict(labeledImage);

            JLabel prediction = new JLabel(String.valueOf(predict));
            prediction.setForeground(Color.red);
            prediction.setFont(new Font("SansSerif", Font.BOLD, 128));
            resultPanel.removeAll();
            resultPanel.add(prediction);
            resultPanel.updateUI();
        });
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            drawArea.setImage(null);
            drawArea.repaint();
            drawAndDigitPredictionPanel.updateUI();
        });
        JPanel actionPanel = new JPanel(new GridLayout(8, 1));
        actionPanel.add(recognize);
        actionPanel.add(clear);
        drawAndDigitPredictionPanel.add(actionPanel);
    }
}
