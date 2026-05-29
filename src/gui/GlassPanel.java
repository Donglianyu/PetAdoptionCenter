package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A lightweight "glass" panel providing:
 *  - rounded corners,
 *  - soft drop shadow,
 *  - translucent gradient fill,
 *  - glossy highlight overlay.
 *
 * This panel is opaque=false so it can be placed on top of arbitrary backgrounds.
 */
public class GlassPanel extends JPanel {
    private int arc = 12;
    // Aero-like subtle glass: soft blue top -> slightly deeper blue bottom
    private Color color1 = new Color(232, 244, 255, 220); // top (very light blue)
    private Color color2 = new Color(210, 230, 250, 200); // bottom (soft blue)
    private Color borderColor = new Color(255, 255, 255, 140);

    public GlassPanel() {
        setOpaque(false);
        // keep a faint translucent background that blends with the dark frame backdrop
        setBackground(new Color(255,255,255,30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw soft shadow (subtle)
        g2.setColor(new Color(0,0,0,36));
        for (int i = 0; i < 4; i++) {
            RoundRectangle2D rr = new RoundRectangle2D.Float(4 - i, 4 - i, w - (8 - 2*i), h - (8 - 2*i), arc, arc);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f * (1 + i)));
            g2.fill(rr);
        }

        // main rounded panel gradient
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
        RoundRectangle2D mainRect = new RoundRectangle2D.Float(0, 0, w-1, h-1, arc, arc);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
        g2.setPaint(gp);
        g2.fill(mainRect);

        // subtle border (slightly bluish white)
        g2.setPaint(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(mainRect);

        // gentle glossy top highlight
        Shape clip = g2.getClip();
        g2.clip(mainRect);
        GradientPaint gloss = new GradientPaint(0, 0, new Color(255,255,255,160), 0, h/3, new Color(255,255,255,30));
        RoundRectangle2D glossRect = new RoundRectangle2D.Float(2, 2, w-4, h/3, arc/2, arc/2);
        g2.setPaint(gloss);
        g2.fill(glossRect);
        g2.setClip(clip);

        g2.dispose();
        super.paintComponent(g);
    }

    // Optional: allow changing corner radius
    public void setArc(int arc) { this.arc = arc; repaint(); }
}

