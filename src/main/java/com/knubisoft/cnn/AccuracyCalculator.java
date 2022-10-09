package com.knubisoft.cnn;

import freemarker.log.Logger;
import lombok.AllArgsConstructor;
import org.deeplearning4j.earlystopping.scorecalc.ScoreCalculator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

@AllArgsConstructor
public class AccuracyCalculator implements ScoreCalculator<MultiLayerNetwork> {

    private final static Logger LOG = Logger.getLogger(AccuracyCalculator.class.getName());
    private final DataSetIterator dataSetIterator;

    /**
     * Calculates accuracy of the passed model
     * @param network MultiLayerNetwork to evaluate
     * @return accuracy of the model
     */
    @Override
    public double calculateScore(MultiLayerNetwork network) {
        Evaluation evaluation = network.evaluate(dataSetIterator);
        LOG.info(evaluation.stats());
        LOG.info(String.valueOf(evaluation.accuracy()));
        return 1 - evaluation.accuracy();
    }

    @Override
    public boolean minimizeScore() {
        return false;
    }
}
