import engine.DrawEngine;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 800;

    public Main() {
        initializeJFrame();
    }

    public static void main(String[] args) {
        new Main();
    }

    private static void initializeJFrame() {
        JFrame frame = new JFrame();
        Container container = frame.getContentPane();
        engine.DrawEngine engine = initializeGraphicsEngine();
        container.add(engine);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static engine.DrawEngine initializeGraphicsEngine() {
        engine.DrawEngine panel = new DrawEngine();    // Construct the drawing canvas
        panel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        return panel;
    }
}
