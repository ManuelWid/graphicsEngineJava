import engine.DrawEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    private static final int CANVAS_WIDTH = 600;
    private static final int CANVAS_HEIGHT = 800;

    public Main() {
        initializeJFrame();
    }

    public static void main(String[] args) {
        new Main();
    }

    private static void initializeJFrame() {
        JFrame frame = new JFrame();
        frame.setTitle("Graphics Engine");
        Container container = frame.getContentPane();
        engine.DrawEngine engine = initializeGraphicsEngine();
        container.add(engine);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static engine.DrawEngine initializeGraphicsEngine() {
        return new DrawEngine(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));    // Construct the drawing canvas
    }
}
