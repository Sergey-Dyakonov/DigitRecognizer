package com.knubisoft;

import com.knubisoft.ui.ProgressBar;
import com.knubisoft.ui.UI;
import freemarker.log.Logger;

import javax.swing.*;
import java.util.concurrent.Executors;

public class Main {

    private final static Logger LOG = Logger.getLogger(Main.class.getName());
    private static final JFrame mainFrame = new JFrame();

    public static void main(String[] args) {
        LOG.info("Application running");
        ProgressBar progressBar = new ProgressBar(mainFrame, true);
        progressBar.showProgressBar("Collecting data... This may take several seconds!");
        UI ui = new UI();
        Executors.newCachedThreadPool().submit(() -> {
            try {
                ui.initUI();
            } finally {
                progressBar.setVisible(false);
                mainFrame.dispose();
            }
        });
    }
}
