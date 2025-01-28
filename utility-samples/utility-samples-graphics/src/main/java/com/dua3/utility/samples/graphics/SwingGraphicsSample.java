package com.dua3.utility.samples.graphics;

import com.dua3.utility.swing.SwingGraphics;
import com.dua3.utility.swing.SwingUtil;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Supplier;

public class SwingGraphicsSample extends JFrame implements IGraphicsSample<JComponent> {

    public static final String APP_NAME = "SwingGraphicsSample";

    public static void main(String[] args) {
        SwingGraphicsSample sample = new SwingGraphicsSample();

        SwingUtil.setNativeLookAndFeel(APP_NAME);
        sample.setVisible(true);
    }

    SwingGraphicsSample() {
        super(APP_NAME);

        setSize(TILE_WIDTH, TILE_HEIGHT);

        int w = getWidth();
        int h = getHeight();

        JTabbedPane tabbedPane = new JTabbedPane();
        createSlides(w, h).forEach(tabbedPane::add);
        setContentPane(tabbedPane);
    }

    @Override
    public JComponent createSlide(Supplier<Slide> factory, float w, float h) {
        Slide slide = factory.get();

        JPanel panel = new JPanel(false) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // Clear panel before rendering
                Graphics2D g2d = (Graphics2D) g;
                SwingUtil.setRenderingQuality(g2d);
                SwingGraphics swingGraphics = new SwingGraphics(g2d, g.getClipBounds());
                slide.draw(swingGraphics);
            }
        };
        panel.setName(slide.title());
        panel.setSize((int) w, (int) h);

        return panel;
    }
}
