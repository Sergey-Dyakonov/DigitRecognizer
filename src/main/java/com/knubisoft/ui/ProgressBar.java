package com.knubisoft.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Progress bar to show internal processes running during app execution
 */
public class ProgressBar {
    private final JFrame mainFrame;
    private JProgressBar progressBar;
    private boolean undecorated = false;

    public ProgressBar(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        progressBar = createProgressBar(mainFrame);
    }

    public ProgressBar(JFrame mainFrame, boolean undecorated) {
        this.mainFrame = mainFrame;
        progressBar = createProgressBar(mainFrame);
        this.undecorated = undecorated;
    }


    /**
     * Shows progress bar with passed message
     *
     * @param msg String to show in progress bar
     */
    public void showProgressBar(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (undecorated) {
                mainFrame.setLocationRelativeTo(null);
                mainFrame.setUndecorated(true);
            }
            progressBar = createProgressBar(mainFrame);
            progressBar.setString(msg);
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            mainFrame.add(progressBar, BorderLayout.NORTH);
            if (undecorated) {
                mainFrame.pack();
                mainFrame.setVisible(true);
            }
            mainFrame.repaint();
        });
    }

    /**
     * Changes ProgressBar visibility according to passed value
     * @param isVisible Boolean value to set visibility (true - visible, false - not visible)
     */
    public void setVisible(boolean isVisible){
        progressBar.setVisible(isVisible);
    }

    private JProgressBar createProgressBar(JFrame mainFrame) {
        JProgressBar bar = new JProgressBar(JProgressBar.HORIZONTAL);
        bar.setVisible(false);
        mainFrame.add(bar, BorderLayout.NORTH);
        return bar;
    }
}
