package com.knubisoft.cnn;

import freemarker.log.Logger;
import lombok.SneakyThrows;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ConvolutionalNeuralNetwork {
    private final static Logger LOG = Logger.getLogger(ConvolutionalNeuralNetwork.class.getName());
    private static final String MODEL_PATH = "resources/model.bin";
    private static final String OUT_DIR = "resources";
    private static final double LEARNING_RATE = 0.01;
    private static final int BATCH_SIZE = 16;
    private static final int MAX_WAIT_AMOUNT = 15;
    private static final int EPOCHS = 10;
    private static final int HEIGHT = 28;
    private static final int WIDTH = 28;
    private static final int DEPTH = 1;
    private static final int SEED = 123;
    private static final int OUT = 10;
    private static final int INP_CHANNELS = 1;

    private MultiLayerNetwork trainedModel;

    /**
     * Initiate a convolutional neural network model
     * (loads data if model has been already trained and saved to file and this file exists)
     */
    @SneakyThrows
    public void init() {
        File modelFile = new File(MODEL_PATH);
        if (trainedModel == null && modelFile.exists()) {
            trainedModel = ModelSerializer.restoreMultiLayerNetwork(modelFile);
        }
    }

    /**
     * Uses already trained CNN model to predict to which class corresponds passed LabeledImage
     *
     * @param img LabeledImage containing vector (one dimensional array) which represents image with one color channel
     * @return The predicted class index which represents passed LabeledImage
     */
    public int predict(LabeledImage img) {
        double[] pixels = img.pixels();
        Arrays.stream(pixels).forEach(pixel -> pixel /= 255); //normalizes value if it is greater than max value (255)
        INDArray indArray = Nd4j.create(pixels).reshape(1, pixels.length);
        return trainedModel.predict(indArray)[0];
    }

    /**
     * Trains {@link ConvolutionalNeuralNetwork#buildCNNConf() CNN model} using
     * <a href="http://yann.lecun.com/exdb/mnist/">MNIST</a> dataset with passed train and test amount
     * and saves trained model to {@link ConvolutionalNeuralNetwork#OUT_DIR the directory}
     * with name defined in {@link ConvolutionalNeuralNetwork#MODEL_PATH <i>MODEL_PATH</i>}
     * when one of the {@link ConvolutionalNeuralNetwork#buildEarlyStopConf(DataSetIterator) early stopping} condition happens.
     * After model training method logs confusion matrix of the model.
     *
     * @param trainDataSize Number of train samples to use during CNN model training
     * @param testDataSize  Number of test samples to use during CNN model testing
     */
    @SneakyThrows
    public void train(int trainDataSize, int testDataSize) {
        LOG.info("Data loading...");
        DataSetIterator mnistTrain = new MnistDataSetIterator(BATCH_SIZE, trainDataSize, false, true, true, SEED);
        DataSetIterator mnistTest = new MnistDataSetIterator(BATCH_SIZE, testDataSize, false, false, true, SEED);

        LOG.info("Model building...");
        MultiLayerConfiguration conf = buildCNNConf();

        LOG.info("Stopping configuration building...");
        EarlyStoppingConfiguration<MultiLayerNetwork> esConf = buildEarlyStopConf(mnistTest);
        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, conf, mnistTrain);

        LOG.info("Training started...");
        EarlyStoppingResult<MultiLayerNetwork> res = trainer.fit();

        LOG.info("Training finished. Saving model...");
        trainedModel = res.getBestModel();
        trainedModel.save(new File(MODEL_PATH), true);

        LOG.info("Model saved");
        LOG.info(String.valueOf(res.getTerminationReason()));
        LOG.info(res.getTerminationDetails());
        LOG.info(String.valueOf(res.getBestModelEpoch()));
        LOG.info(String.valueOf(res.getBestModelScore()));
    }

    /**
     * Builds early stopping configuration for a {@link ConvolutionalNeuralNetwork#buildCNNConf() CNN model}
     * according to which model training finishes in case of reaching one of the following conditions:
     * <ul>
     *     <li>number of epochs reaches {@link ConvolutionalNeuralNetwork#EPOCHS max epochs amount}</li>
     *     <li>iteration for epoch training takes more then {@link ConvolutionalNeuralNetwork#MAX_WAIT_AMOUNT max waiting amount} minutes</li>
     * </ul>
     * @param mnistTest DataSetIterator which consists samples for testing
     * @return EarlyStoppingConfiguration for CNN model
     */
    private EarlyStoppingConfiguration<MultiLayerNetwork> buildEarlyStopConf(DataSetIterator mnistTest) {
        return new EarlyStoppingConfiguration.
                Builder<MultiLayerNetwork>().
                epochTerminationConditions(new MaxEpochsTerminationCondition(EPOCHS)).
                iterationTerminationConditions(new MaxTimeIterationTerminationCondition(MAX_WAIT_AMOUNT, TimeUnit.MINUTES)).
                scoreCalculator(new AccuracyCalculator(mnistTest)).
                evaluateEveryNEpochs(1).
                modelSaver(new LocalFileModelSaver(OUT_DIR)).
                build();
    }

    /**
     * Builds CNN with the following model:
     * <ol>
     *     <li><strong>Convolutional layer</strong>, kernel: [5, 5], activation: identity (linear activation), in: 1, out: 20</li>
     *     <li><strong>Max pooling layer</strong>, kernel: [2, 2]</li>
     *     <li><strong>Convolutional layer</strong>, kernel: [5, 5], activation: identity (linear activation), in: 20, out: 50</li>
     *     <li><strong>Max pooling layer</strong>, kernel: [2, 2]</li>
     *     <li><strong>Dense layer (fully connected layer)</strong>, activation: relu, in: 800, out: 128</li>
     *     <li><strong>Dense layer (fully connected layer)</strong>, activation: relu, in: 128, out: 64</li>
     *     <li><strong>Softmax layer</strong>, in: 64, out: 10</li>
     * </ol>
     *
     * @return MultiLayerConfiguration representing model mentioned above
     */

    private MultiLayerConfiguration buildCNNConf() {
        return new NeuralNetConfiguration.Builder().
                seed(SEED).
                activation(Activation.RELU).
                weightInit(WeightInit.XAVIER).
                updater(new Nesterovs(LEARNING_RATE, 0.9)).
                optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).
                list().
                layer(0, new ConvolutionLayer.Builder(5, 5).
                        nIn(INP_CHANNELS).
                        stride(1, 1).
                        nOut(20).
                        activation(Activation.IDENTITY).
                        build()).
                layer(1, new SubsamplingLayer.Builder(PoolingType.MAX).
                        kernelSize(2, 2).
                        stride(2, 2).
                        build()).
                layer(2, new ConvolutionLayer.Builder(5, 5).
                        nIn(20).
                        stride(1, 1).
                        nOut(50).
                        activation(Activation.IDENTITY).
                        build()).
                layer(3, new SubsamplingLayer.Builder().
                        kernelSize(2, 2).
                        stride(2, 2).
                        build()).
                layer(4, new DenseLayer.Builder().
                        activation(Activation.RELU).
                        nIn(800).
                        nOut(128).
                        build()).
                layer(5, new DenseLayer.Builder().
                        activation(Activation.RELU).
                        nIn(128).
                        nOut(64).
                        build()).
                layer(6, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).
                        nOut(OUT).
                        activation(Activation.SOFTMAX).
                        build()).
                setInputType(InputType.convolutionalFlat(HEIGHT, WIDTH, DEPTH)).
                backpropType(BackpropType.Standard).
                build();
    }
}
