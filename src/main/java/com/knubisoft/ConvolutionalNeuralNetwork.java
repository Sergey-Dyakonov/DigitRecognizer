package com.knubisoft;

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
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ConvolutionalNeuralNetwork {

    private static final String MODEL_PATH = "resources/model.bin";
    private static final String OUT_DIR = "resources";
    private static final double LEARNING_RATE = 0.01;
    private static final int BATCH_SIZE = 16;
    private static final int EPOCHES = 5;
    private static final int HEIGHT = 28;
    private static final int WIDTH = 28;
    private static final int SEED = 123;
    private static final int OUT = 10;
    private static final int INP_CHANNELS = 1;

    private MultiLayerNetwork trainedModel;

    @SneakyThrows
    public void init() {
        File modelFile = new File(MODEL_PATH);
        if (trainedModel == null && modelFile.exists()) {
            trainedModel = ModelSerializer.restoreMultiLayerNetwork(modelFile);
        }
    }

    public int predict(LabeledImage img) {
        double[] pixels = img.getPixels();
        Arrays.stream(pixels).forEach(pixel -> pixel /= 255);
        return trainedModel.predict(Nd4j.create(pixels))[0];
    }

    @SneakyThrows
    public void train(int trainDataSize, int testDataSize) {
        System.out.println("Data loading...");
//        DataSetIterator mnistTrain = new MnistDataSetIterator(BATCH_SIZE, trainDataSize, false, true, true, SEED);
        DataSetIterator mnistTrain = new MnistDataSetIterator(BATCH_SIZE, true, SEED);
        DataSetIterator mnistTest = new MnistDataSetIterator(BATCH_SIZE, false, SEED);

        System.out.println("Model building...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().
                seed(SEED).
                activation(Activation.RELU).
                weightInit(WeightInit.XAVIER).
                updater(new Nesterovs(LEARNING_RATE, 0.9)).
                optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).
//                l2(LEARNING_RATE).
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
                setInputType(InputType.convolutionalFlat(28, 28, 1)).
                backpropType(BackpropType.Standard).
                build();

        EarlyStoppingConfiguration<MultiLayerNetwork> esConf = new EarlyStoppingConfiguration.
                Builder<MultiLayerNetwork>().
                epochTerminationConditions(new MaxEpochsTerminationCondition(EPOCHES)).
                iterationTerminationConditions(new MaxTimeIterationTerminationCondition(75, TimeUnit.MINUTES)).
                scoreCalculator(new AccuracyCalculator(
                        new MnistDataSetIterator(testDataSize, testDataSize, false, false, true, SEED))).
                evaluateEveryNEpochs(1).
                modelSaver(new LocalFileModelSaver(OUT_DIR)).
                build();
        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, conf, mnistTrain);
        EarlyStoppingResult<MultiLayerNetwork> res = trainer.fit();

        System.out.println(res.getTerminationReason());
        System.out.println(res.getTerminationDetails());
        System.out.println(res.getBestModelEpoch());
        System.out.println(res.getBestModelScore());
//        AccuracyCalculator accuracyCalculator = new AccuracyCalculator(mnistTest);
//        accuracyCalculator.calculateScore(conf);
    }

    public static void main(String[] args) {
        new ConvolutionalNeuralNetwork().train(30000, 1000);
    }
}
