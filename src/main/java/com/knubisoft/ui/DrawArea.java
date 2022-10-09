package com.knubisoft.ui;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Canvas for getting mouse pointer input
 */
public class DrawArea extends JComponent {
    @Getter
    @Setter
    private Image image;
    @Setter
    @Getter
    private int strokeWidth = 10;
    private Graphics2D g2;
    private int curX, curY, oldX, oldY;

    /**
     * Creates area for drawing (canvas) with border and tip
     */
    public DrawArea() {
        setDoubleBuffered(false);

        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Please, draw a digit",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 18),
                Color.blue));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                oldX = e.getX();
                oldY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                curX = e.getX();
                curY = e.getY();

                if (g2 != null) {
                    g2.setStroke(new BasicStroke(strokeWidth));
                    g2.drawLine(oldX, oldY, curX, curY);
                    repaint();
                    oldX = curX;
                    oldY = curY;
                }
            }
        });
    }

    /**
     * Converts graphical data from the canvas to Image
     *
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (image == null) {
            image = createImage(getSize().width, getSize().height);
            g2 = (Graphics2D) image.getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            clear();
        }
        g.drawImage(image, 0, 0, null);
    }

    /**
     * Clears canvas
     */
    private void clear() {
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, getSize().width, getSize().height);
        g2.setPaint(Color.black);
        repaint();
    }
}
