package com.knubisoft.ui;

import javax.swing.*;
import java.awt.*;

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

    public void setVisible(boolean visible){
        progressBar.setVisible(visible);
    }

    private JProgressBar createProgressBar(JFrame mainFrame) {
        JProgressBar bar = new JProgressBar(JProgressBar.HORIZONTAL);
        bar.setVisible(false);
        mainFrame.add(bar, BorderLayout.NORTH);
        return bar;
    }
}
