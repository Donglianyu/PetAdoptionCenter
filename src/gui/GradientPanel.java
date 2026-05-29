package gui;

import javax.swing.*;
import java.awt.*;

/**
 * A simple panel that paints a vertical gradient background. Keeps normal JPanel API.
 */
public class GradientPanel extends JPanel {
    private Color top;
    private Color bottom;

    public GradientPanel(Color top, Color bottom, LayoutManager layout) {
        super(layout);
        this.top = top;
        this.bottom = bottom;
        setOpaque(true);
    }

    public GradientPanel(Color top, Color bottom) {
        this(top, bottom, new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (top == null || bottom == null) {
            super.paintComponent(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bottom);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
        super.paintComponent(g);
    }

    public void setTop(Color top) { this.top = top; repaint(); }
    public void setBottom(Color bottom) { this.bottom = bottom; repaint(); }
}

