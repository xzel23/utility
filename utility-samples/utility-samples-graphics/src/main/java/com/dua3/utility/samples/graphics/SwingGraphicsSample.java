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

/**
 * The SwingGraphicsSample class provides a Swing-based implementation of the IGraphicsSample interface
 * for rendering graphical slides. This class sets up a user interface using JFrame and JTabbedPane,
 * where each tab represents a graphical slide.
 * <p>
 * This implementation demonstrates how graphical content can be dynamically generated and
 * rendered in a Swing application. The slides are created using factories that provide graphical
 * content, and they are rendered with high-quality settings.
 * <p>
 * The class includes several components:
 * - It defines application-level properties such as the application name.
 * - It initializes the graphical user interface using a tabbed pane to display multiple slides.
 * - It provides an implementation of the createSlide method to generate graphical panels using
 *   supplied factories.
 * <p>
 * Usage:
 * - This class sets a native look and feel for the Swing application.
 * - The main method serves as the entry point for launching the application.
 * - The createSlide method renders a graphical slide on a JPanel for any given slide factory.
 */
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
                SwingUtil.setRenderingQualityHigh(g2d);
                SwingGraphics swingGraphics = new SwingGraphics(g2d, g.getClipBounds());
                slide.draw(swingGraphics);
            }
        };
        panel.setName(slide.title());
        panel.setSize((int) w, (int) h);

        return panel;
    }
}
