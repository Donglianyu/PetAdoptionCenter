package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A translucent, flat button styled to match the glass panel.
 * Keeps normal JButton APIs so replacing existing JButtons is straightforward.
 */
public class GlassButton extends JButton {
    // Aero-style button colors (soft blue accents)
    private Color base = new Color(0, 120, 215, 220);   // aero blue
    private Color hover = new Color(0, 142, 255, 235);  // lighter blue on hover
    private Color pressed = new Color(0, 90, 180, 220); // deeper blue when pressed

    public GlassButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setFont(getFont().deriveFont(Font.BOLD, 13f));
        setMargin(new Insets(6,12,6,12));
        initListeners();
    }

    private void initListeners() {
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { repaint(); }
            @Override public void mouseExited(MouseEvent e) { repaint(); }
            @Override public void mousePressed(MouseEvent e) { repaint(); }
            @Override public void mouseReleased(MouseEvent e) { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth(), h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = base;
        if (getModel().isPressed()) fill = pressed;
        else if (getModel().isRollover()) fill = hover;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
        g2.setPaint(new GradientPaint(0,0, fill, 0,h, new Color(255,255,255,28)));
        g2.fillRoundRect(0, 0, w, h, 14, 14);

        // soft border
        g2.setColor(new Color(255,255,255,140));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w-1, h-1, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}

