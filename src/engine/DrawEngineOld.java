package engine;

import engine.utils.Sphere;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DrawEngineOld extends JFrame {
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 800;

    private DrawCanvas canvas;

    // constructor
    public DrawEngineOld() {
        canvas = new DrawCanvas();    // Construct the drawing canvas
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        // Set the Drawing JPanel as the JFrame's content-pane
        Container container = getContentPane();
        container.add(canvas);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        new DrawEngineOld();
    }

    // inner JPanel class
    private static class DrawCanvas extends JPanel implements ActionListener {
        private final Timer timer;
        private Graphics2D graphics;

        private static final int POINT_SIZE = 5;
        private static final float LINE_SIZE = 1.0f;

        private static final int FPS = 60;
        private static final double DELTA = 1.0 / FPS;

        private static final Color BACKGROUND = new Color(30, 30, 30);
        private static final Color FOREGROUND = new Color(0, 150, 0);

        private double viewDistance = 1;
        private double angle = 0;

        public boolean drawVertices = false;
        public boolean drawEdges = true;

        private Sphere sphere1 = new Sphere(0.15,15, 15, 0, 0, 0);
        private Sphere sphere2 = new Sphere(0.1,10, 10, 0.3, 0, 0);
        private Sphere sphere3 = new Sphere(0.2,20, 20, 0.8, 0, 0);

        private double[][][] vertices = {
            sphere1.getSphereVertices(),
            sphere2.getSphereVertices(),
            sphere3.getSphereVertices(),
        };
//        private double[][] vertices = {
//                new double[]{0.25, 0.25, 0.25},
//                new double[]{-0.25, 0.25, 0.25},
//                new double[]{-0.25, -0.25, 0.25},
//                new double[]{0.25, -0.25, 0.25},
//
//                new double[]{0.25, 0.25, -0.25},
//                new double[]{-0.25, 0.25, -0.25},
//                new double[]{-0.25, -0.25, -0.25},
//                new double[]{0.25, -0.25, -0.25},
//        };

        private int[][][] indices = {
            sphere1.getSphereIndices(),
            sphere2.getSphereIndices(),
            sphere3.getSphereIndices(),
        };
//        private int[][] indices = {
//                {0, 1, 2, 3},
//                {4, 5, 6, 7},
//                {0, 4},
//                {1, 5},
//                {2, 6},
//                {3, 7},
//        };

        // constructor
        public DrawCanvas() {
            timer = new Timer(1000 / FPS, this);
            timer.start();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            deltaZ += 1 * DELTA;
            angle += 0.5 * DELTA;
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            graphics = g2d;
            setBackground(BACKGROUND);

            g2d.setColor(FOREGROUND);

            if (drawVertices) {
                for (int i = 0; i < vertices.length; i++) {
                    for (double[] vertex : vertices[i]) {
                        double[] translatedPoint = translate_z(rotate_xy(rotate_xz(vertex, angle * (1 + i * 0.5)), angle * (1 + i * 1.5)), viewDistance);
                        drawPoint(translateToCanvas(project(translatedPoint)), translatedPoint[2]);
                    }
                }
            }

            if (drawEdges) {
                for (int i = 0; i < indices.length; i++) {
                    for (int[] edge : indices[i]) {
                        for (int j = 0; j < edge.length; j++) {
                            double[] a = vertices[i][edge[j]];
                            double[] b = vertices[i][edge[(j + 1)%edge.length]];
                            double[] translatedPointA = translate_z(rotate_xy(rotate_xz(a, angle * (1 + i * 0.5)), angle * (1 + i * 1.5)), viewDistance);
                            double[] translatedPointB = translate_z(rotate_xy(rotate_xz(b, angle * (1 + i * 0.5)), angle * (1 + i * 1.5)), viewDistance);
                            double thicknessModifier = (translatedPointA[2] + translatedPointB[2]) / 2;
                            drawLine(
                                    translateToCanvas(project(translatedPointA)),
                                    translateToCanvas(project(translatedPointB)),
                                    thicknessModifier
                            );
                        }
                    }
                }
            }
        }

        /**
         * Draws a filled oval to the canvas at the given coordinates.
         * @param vec2 int[]{x, y}
         * @param sizeModifier double, higher means smaller
         */
        private void drawPoint(int[] vec2, double sizeModifier) {
            int size = (int) (POINT_SIZE / sizeModifier);
            int offset = size / 2;
            graphics.fillOval(vec2[0] - offset, vec2[1] - offset, size, size);
        }

        /**
         * Draw a line between two points.
         * @param p1 int[]{x, y}
         * @param p2 int[]{x, y}
         */
        private void drawLine(int[] p1, int[] p2, double thicknessModifier) {
            BasicStroke stroke = new BasicStroke((float)(LINE_SIZE / thicknessModifier));
            graphics.setStroke(stroke);
            graphics.drawLine(p1[0], p1[1], p2[0], p2[1]);
        }

        /**
         * Translate normalized coordinates (-1..1) to canvas coordinates.
         * @param vec2 double[]{x, y}
         * @return A new int[]{x, y} with the normalized coordinates translated to the canvas coordinates.
         */
        private int[] translateToCanvas(double[] vec2) {
            // input -1..1
            // +1     0..2
            // /2     0..1
            // *size  0..size
            return new int[]{
                    (int) ((vec2[0] + 1) / 2 * CANVAS_WIDTH),
                    (int) ((1 - (vec2[1] + 1) / 2) * CANVAS_HEIGHT),
            };
        }

        /**
         * Project a normalized 3D vector (-1..1) to a normalized 2D vector.
         * @param vec3 double[]{x, y, z}
         * @return A new int[]{x, y} with normalized coordinates.
         */
        private double[] project(double[] vec3) {
            return new double[]{
                    vec3[0] / vec3[2],
                    vec3[1] / vec3[2]
            };
        }

        /**
         * Translate the Z axis by a given delta or distance
         * @param vec3 double[]{x, y, z}
         * @param delta double
         * @return A new double[]{x, y, z} with translated z.
         */
        private double[] translate_z(double[] vec3, double delta) {
            return new double[]{
                    vec3[0],
                    vec3[1],
                    vec3[2] + delta
            };
        }

        /**
         * Rotate yz, rotation along the x axis.
         * @param vec3 double[]{x, y, z}
         * @param angle double, in radians
         * @return A new double[]{x, y, z} with rotated yz.
         */
        private double[] rotate_yz(double[] vec3, double angle) {
            double c = Math.cos(angle);
            double s = Math.sin(angle);

            return new double[]{
                    vec3[0],
                    vec3[1] * c - vec3[2] * s,
                    vec3[1] * s + vec3[2] * c
            };
        }

        /**
         * Rotate xz, rotation along the y axis.
         * @param vec3 double[]{x, y, z}
         * @param angle double, in radians
         * @return A new double[]{x, y, z} with rotated xz.
         */
        private double[] rotate_xz(double[] vec3, double angle) {
            double c = Math.cos(angle);
            double s = Math.sin(angle);

            return new double[]{
                    vec3[0] * c - vec3[2] * s,
                    vec3[1],
                    vec3[0] * s + vec3[2] * c
            };
        }

        /**
         * Rotate xy, rotation along the z axis.
         * @param vec3 double[]{x, y, z}
         * @param angle double, in radians
         * @return A new double[]{x, y, z} with rotated xy.
         */
        private double[] rotate_xy(double[] vec3, double angle) {
            double c = Math.cos(angle);
            double s = Math.sin(angle);

            return new double[]{
                    vec3[0] * c - vec3[1] * s,
                    vec3[0] * s + vec3[1] * c,
                    vec3[2],
            };
        }
    }
}
