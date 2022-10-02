package com.knubisoft;

import lombok.AllArgsConstructor;
import org.deeplearning4j.earlystopping.scorecalc.ScoreCalculator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

@AllArgsConstructor
public class AccuracyCalculator implements ScoreCalculator<MultiLayerNetwork> {

    private final DataSetIterator dataSetIterator;

    @Override
    public double calculateScore(MultiLayerNetwork network) {
        Evaluation evaluation = network.evaluate(dataSetIterator);
        System.out.println(evaluation.stats());
        System.out.println(evaluation.accuracy());
        return 1 - evaluation.accuracy();
    }

    @Override
    public boolean minimizeScore() {
        return false;
    }
}
