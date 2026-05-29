package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Utilities to apply the glass look to an existing frame.
 * Call UIUtils.applyGlassLook(frame) after your MainFrame has built components.
 *
 * This will:
 *  - wrap the current contentPane into a GlassPanel (keeping layout and children),
 *  - set some default fonts/colors,
 *  - replace JButton instances with GlassButton (non-destructively),
 *  - make other minor cosmetic changes.
 */
public class UIUtils {

    public static void applyGlassLook(JFrame frame) {
        // Wrap existing content in a GlassPanel
        Container old = frame.getContentPane();
        Component[] comps = old.getComponents();

        GlassPanel glass = new GlassPanel();
        glass.setLayout(new BorderLayout());
        // transfer components
        old.removeAll();
        for (Component c : comps) {
            glass.add(c);
        }
        frame.setContentPane(glass);

        // tweak root frame appearance
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        // set a light millennial-tinged background (soft pastel) to pair with the vibrant glass panels
        frame.getContentPane().setBackground(new Color(255, 245, 250));

        // set global font (optional) - prefer Frutiger, fall back to Segoe UI / SansSerif
        setGlobalFont(selectPreferredFont(new String[]{"Frutiger", "Segoe UI", "SansSerif"}, Font.PLAIN, 13));

        // walk components and apply default (pink) styles
        recurseStyle(frame.getContentPane(), 0);
        frame.revalidate();
        frame.repaint();
    }

    // Public helpers to switch between initial blue theme (pre-login) and pink theme (post-login)
    public static void applyBlueTheme(JFrame frame) {
        frame.getContentPane().setBackground(new Color(232, 244, 255));
        applyThemeToComponent(frame.getContentPane(), Theme.BLUE);
        frame.revalidate();
        frame.repaint();
    }

    public static void applyPinkTheme(JFrame frame) {
        // keep a soft neutral background for alternate theme
        frame.getContentPane().setBackground(new Color(248, 251, 255));
        applyThemeToComponent(frame.getContentPane(), Theme.PINK);
        frame.revalidate();
        frame.repaint();
    }

    private enum Theme { BLUE, PINK }

    private static void applyThemeToComponent(Component c, Theme theme) {
        // Aero-ish soft blues and teals
        Color panelBg = (theme == Theme.BLUE) ? new Color(232,244,255) : new Color(248,251,255);
        Color headerAccent = (theme == Theme.BLUE) ? new Color(0,120,215) : new Color(0,150,200);

        if (c instanceof JLabel) {
            ((JLabel) c).setForeground(new Color(20,20,25));
        } else if (c instanceof JTable) {
            JTable t = (JTable) c;
            t.setBackground(Color.WHITE);
            t.setForeground(new Color(20,20,25));
            t.setSelectionBackground(new Color(0,120,215));
            t.setSelectionForeground(Color.WHITE);
                if (t.getTableHeader() != null) {
                    t.getTableHeader().setBackground(headerAccent);
                    t.getTableHeader().setForeground(new Color(20,20,25));
                }
        } else if (c instanceof JTextField || c instanceof JPasswordField) {
            ((JTextField) c).setBackground(new Color(255,255,255));
            ((JTextField) c).setForeground(new Color(20,20,25));
        } else if (c instanceof JTextArea) {
            ((JTextArea) c).setBackground(new Color(255,255,255));
            ((JTextArea) c).setForeground(new Color(20,20,25));
        } else if (c instanceof JScrollPane) {
            JViewport vp = ((JScrollPane) c).getViewport();
            if (vp != null) vp.setBackground(panelBg);
        } else if (c instanceof JComboBox) {
            @SuppressWarnings("rawtypes")
            JComboBox cb = (JComboBox) c;
            cb.setBackground(new Color(255,255,255));
            cb.setForeground(new Color(20,20,25));
        } else if (c instanceof JPanel) {
            // do not override GlassPanel or GradientPanel (login) so their custom painting remains
            if (!(c instanceof gui.GlassPanel) && !(c instanceof gui.GradientPanel)) {
                JPanel p = (JPanel) c;
                p.setBackground(panelBg);
                p.setOpaque(true);
            }
        } else if (c instanceof JTabbedPane) {
            JTabbedPane tp = (JTabbedPane) c;
            tp.setBackground(panelBg);
            tp.setForeground(new Color(20,20,25));
        } else if (c instanceof JSplitPane) {
            ((JSplitPane) c).setBackground(panelBg);
        }

        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                applyThemeToComponent(child, theme);
            }
        }
    }

    // soft Aero-ish tints for subtle panel accents
    private static final Color[] PALETTE = new Color[] {
            new Color(232,244,255,200), // very light blue
            new Color(220,238,255,200),
            new Color(210,235,245,200)
    };

    private static void recurseStyle(Component c, int depth) {
        if (c instanceof JButton && !(c instanceof GlassButton)) {
            // replace with GlassButton: create new button with same text and listeners
            JButton oldBtn = (JButton) c;
            Container parent = oldBtn.getParent();
            if (parent != null) {
                int idx = findIndex(parent, oldBtn);
                GlassButton gb = new GlassButton(oldBtn.getText());
                // copy model properties that don't affect action listeners
                gb.setEnabled(oldBtn.isEnabled());
                // transfer ActionListeners
                for (java.awt.event.ActionListener al : oldBtn.getActionListeners()) {
                    gb.addActionListener(al);
                }
                // transfer tooltip
                gb.setToolTipText(oldBtn.getToolTipText());
                parent.remove(idx);
                parent.add(gb, idx);
            }
        } else if (c instanceof JLabel) {
            // keep labels dark for readability on light background
            ((JLabel) c).setForeground(new Color(20, 20, 25));
        } else if (c instanceof JTable) {
            JTable t = (JTable) c;
            t.setShowGrid(false);
            t.setRowHeight(24);
            t.setBackground(Color.WHITE);
            t.setForeground(new Color(20,20,25));
            t.setSelectionBackground(new Color(0,120,215)); // aero blue accent for selection
            t.setSelectionForeground(Color.WHITE);
                if (t.getTableHeader() != null) {
                    t.getTableHeader().setBackground(new Color(0,120,215));
                    t.getTableHeader().setForeground(new Color(20,20,25));
                }
        } else if (c instanceof JTextField || c instanceof JPasswordField) {
            JTextField tf = (JTextField) c;
            tf.setBackground(new Color(255,255,255));
            tf.setForeground(new Color(20,20,25));
            try { tf.setCaretColor(new Color(20,20,25)); } catch (Throwable ignored) {}
        } else if (c instanceof JTextArea) {
            JTextArea ta = (JTextArea) c;
            ta.setBackground(new Color(255,255,255));
            ta.setForeground(new Color(20,20,25));
        } else if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            JViewport vp = sp.getViewport();
            if (vp != null) vp.setBackground(new Color(248,251,255));
        } else if (c instanceof JComboBox) {
            @SuppressWarnings("rawtypes")
            JComboBox cb = (JComboBox) c;
            cb.setBackground(new Color(255,255,255));
            cb.setForeground(new Color(20,20,25));
        } else if (c instanceof JPanel) {
            JPanel p = (JPanel) c;
            // Avoid recoloring the top-level GlassPanel which already paints a gradient
            if (!(p instanceof gui.GlassPanel)) {
                // Make panels use a soft Aero-like neutral background
                p.setBackground(new Color(248, 251, 255));
                p.setOpaque(true);
            }
        } else if (c instanceof JTabbedPane) {
            JTabbedPane tp = (JTabbedPane) c;
            tp.setBackground(new Color(248,251,255));
            tp.setForeground(new Color(20,20,25));
        } else if (c instanceof JSplitPane) {
            JSplitPane sp = (JSplitPane) c;
            sp.setBackground(new Color(248,251,255));
        }

        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                recurseStyle(child, depth + 1);
            }
        }
    }

    private static int findIndex(Container parent, Component c) {
        Component[] children = parent.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] == c) return i;
        }
        return -1;
    }

    private static void setGlobalFont(Font f) {
        UIManager.put("Label.font", f);
        UIManager.put("Button.font", f);
        UIManager.put("ToggleButton.font", f);
        UIManager.put("RadioButton.font", f);
        UIManager.put("ComboBox.font", f);
        UIManager.put("Menu.font", f);
        UIManager.put("Table.font", f);
        UIManager.put("TextField.font", f);
    }

    private static Font selectPreferredFont(String[] names, int style, int size) {
        for (String n : names) {
            try {
                Font f = new Font(n, style, size);
                // If the requested family is available, getFamily will match closely
                if (f != null && f.getFamily() != null && !"Dialog".equals(f.getFamily())) {
                    return f;
                }
            } catch (Throwable ignored) {}
        }
        return new Font("SansSerif", style, size);
    }

    // optional helper: try to set Nimbus Look and Feel (call from Main before creating frame)
    public static void trySetNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {}
    }
}

