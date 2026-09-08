import engine.DrawEngine;
import engine.utils.Sphere;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    private static final int CANVAS_WIDTH = 600;
    private static final int CANVAS_HEIGHT = 800;
    private static DrawEngine engine;

    public static void main(String[] args) {
        initializeJFrame();
        engine.drawEdges = true;
        engine.drawVertices = false;

        Sphere sphere1 = new Sphere(0.4,40, 40, 0, 0, 0);
        Sphere sphere2 = new Sphere(0.2,20, 20, 0.3, 0, 0);
        Sphere sphere3 = new Sphere(0.6,70, 70, -0.5, 0, 0);
        engine.addModel(sphere1);
//        engine.addModel(sphere2);
//        engine.addModel(sphere3);
    }

    private static void initializeJFrame() {
        JFrame frame = new JFrame();
        frame.setTitle("Graphics Engine");
        Container container = frame.getContentPane();
        engine = new DrawEngine(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        container.add(engine);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
