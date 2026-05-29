package gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

/**
 * A small lightweight chart panel that can draw a simple bar chart or pie chart
 * from a Map<String, Long> or Map<LocalDate, Long>. This avoids external
 * dependencies and provides a visual analytics widget out of the box.
 */
public class BasicChartPanel extends JPanel {
    public enum Mode { BAR, PIE }

    private Mode mode = Mode.BAR;
    private Map<String, Long> stringData = new LinkedHashMap<>();
    private Map<LocalDate, Long> dateData = new LinkedHashMap<>();
    private DateTimeFormatter df = DateTimeFormatter.ofPattern("MM-dd");
    private String title = "";

    public BasicChartPanel() {
        setPreferredSize(new Dimension(400, 300));
    }

    public void setMode(Mode m) {
        this.mode = m;
        repaint();
    }

    /**
     * Return the current display mode (BAR or PIE).
     * This getter is used by callers that need to toggle chart mode.
     */
    public Mode getMode() {
        return this.mode;
    }

    public void setStringData(Map<String, Long> data) {
        this.stringData = new LinkedHashMap<>(data);
        this.dateData.clear();
        repaint();
    }

    public void setDateData(Map<LocalDate, Long> data) {
        this.dateData = new LinkedHashMap<>(data);
        // convert to stringData for PIE mode if needed
        this.stringData.clear();
        for (Map.Entry<LocalDate, Long> e : data.entrySet()) {
            this.stringData.put(e.getKey().format(df), e.getValue());
        }
        repaint();
    }

    public void setTitle(String t) {
        this.title = t == null ? "" : t;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Insets in = getInsets();
        int w = getWidth() - in.left - in.right;
        int h = getHeight() - in.top - in.bottom;

        if (mode == Mode.BAR) {
            drawBarChart(g2, in.left, in.top, w, h);
        } else {
            drawPieChart(g2, in.left, in.top, w, h);
        }
    }

    private void drawBarChart(Graphics2D g2, int x, int y, int w, int h) {
        Map<String, Long> data = dateData.isEmpty() ? stringData : dateDataToStringMap();
        if (data.isEmpty()) {
            drawEmpty(g2, x, y, w, h);
            return;
        }
        // draw title if present
        if (!title.isEmpty()) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (w - fm.stringWidth(title)) / 2;
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(title, tx, y + 18);
            y += 24; // move chart area down
            h -= 24;
        }
        long max = data.values().stream().mapToLong(Long::longValue).max().orElse(1);
        int margin = 40;
        int chartW = w - margin * 2;
        int chartH = h - margin * 2;
        int barWidth = Math.max(6, chartW / data.size() - 10);

        int i = 0;
        for (Map.Entry<String, Long> e : data.entrySet()) {
            int bx = x + margin + i * (barWidth + 10);
            int bh = (int) ((double) e.getValue() / max * chartH);
            int by = y + margin + (chartH - bh);
            g2.setColor(new Color(100, 150, 240));
            g2.fillRect(bx, by, barWidth, bh);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(bx, by, barWidth, bh);
            // draw label centered under the bar
            FontMetrics fm = g2.getFontMetrics();
            int labelX = bx + (barWidth - fm.stringWidth(e.getKey())) / 2;
            g2.drawString(e.getKey(), labelX, y + margin + chartH + 15);
            // draw value above the bar
            String valStr = String.valueOf(e.getValue());
            int valW = fm.stringWidth(valStr);
            g2.drawString(valStr, bx + (barWidth - valW) / 2, by - 4);
            i++;
        }
    }

    private Map<String, Long> dateDataToStringMap() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, Long> e : dateData.entrySet()) {
            m.put(e.getKey().format(df), e.getValue());
        }
        return m;
    }

    private void drawPieChart(Graphics2D g2, int x, int y, int w, int h) {
        if (stringData.isEmpty()) {
            drawEmpty(g2, x, y, w, h);
            return;
        }
        // draw title if present
        if (!title.isEmpty()) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (w - fm.stringWidth(title)) / 2;
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(title, tx, y + 18);
            y += 24;
            h -= 24;
        }
        int size = Math.min(w, h) - 40;
        int cx = x + (w - size) / 2;
        int cy = y + (h - size) / 2;
        long total = stringData.values().stream().mapToLong(Long::longValue).sum();
        int start = 0;
        int i = 0;
        Color[] palette = new Color[]{new Color(120, 180, 240), new Color(240, 120, 120), new Color(120, 240, 160), new Color(240, 200, 120)};
        for (Map.Entry<String, Long> e : stringData.entrySet()) {
            int angle = (int) Math.round(360.0 * e.getValue() / Math.max(1, total));
            g2.setColor(palette[i % palette.length]);
            g2.fillArc(cx, cy, size, size, start, angle);
            start += angle;
            i++;
        }
        // draw legend
        int lx = cx + size + 10;
        int ly = cy;
        i = 0;
        for (Map.Entry<String, Long> e : stringData.entrySet()) {
            g2.setColor(palette[i % palette.length]);
            g2.fillRect(lx, ly + i * 18, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            // draw key and percentage to make species clear
            double pct = total == 0 ? 0.0 : (100.0 * e.getValue() / total);
            String label = e.getKey() + " (" + e.getValue() + ") " + String.format("%.1f%%", pct);
            g2.drawString(label, lx + 16, ly + 12 + i * 18);
            i++;
        }
    }

    private void drawEmpty(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(Color.GRAY);
        g2.drawString("No data to display", x + w / 2 - 40, y + h / 2);
    }
}

