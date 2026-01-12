package engine;

import engine.utils.Sphere;
import org.w3c.dom.ls.LSOutput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DrawEngine extends JPanel implements ActionListener {
    public boolean drawVertices = true;
    public boolean drawEdges = true;

    private double viewDistance = 1;
    private double angle = 0;

    private final Timer timer;
    private Graphics2D graphics;

    private final int POINT_SIZE = 5;
    private final float LINE_SIZE = 1.0f;

    private final int FPS = 60;
    private final double DELTA = 1.0 / FPS;

    private final Color BACKGROUND = new Color(30, 30, 30);
    private final Color FOREGROUND = new Color(0, 150, 0);

    private double aspectRatio = 1.0;


    private Sphere sphere1 = new Sphere(0.4,50, 50, 0, 0, 0);
    private Sphere sphere2 = new Sphere(0.2,25, 25, 0.3, 0, 0);
    private Sphere sphere3 = new Sphere(0.6,100, 100, 0.8, 0, 0);

    private double[][][] vertices = {
            sphere1.getSphereVertices(),
            sphere2.getSphereVertices(),
            sphere3.getSphereVertices(),
    };

    private int[][][] indices = {
            sphere1.getSphereIndices(),
            sphere2.getSphereIndices(),
            sphere3.getSphereIndices(),
    };

    // constructor
    public DrawEngine(Dimension dimensions) {
        setPreferredSize(dimensions);
        aspectRatio = dimensions.getWidth() / dimensions.getHeight();
        timer = new Timer(1000 / FPS, this);
        timer.start();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                handleResize(getSize());
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                System.out.println(e);
            }
        });

        addMouseWheelListener(new MouseAdapter() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                handleZoom(e.getPreciseWheelRotation());
            }
        });
    }

    double lastTime = 0;

    @Override
    public void actionPerformed(ActionEvent e) {
//            deltaZ += 1 * DELTA;
        angle += 0.5 * DELTA;
        System.out.println(1000 / timer.getDelay());
        repaint();
        Toolkit.getDefaultToolkit().sync();
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
                    double[] rotateXY = rotateXY(vertex, angle * (1 + i * 0.5));
                    double[] rotateXZ = rotateXZ(rotateXY, angle * (1 + i * 0.5));
                    double[] rotateYZ = rotateYZ(rotateXZ, angle * (1 + i * 0.5));
                    double[] translatedPoint = translateZ(rotateYZ, viewDistance);
                    double[] translateAspect = translateAspectRatio(translatedPoint);
                    drawPoint(translateToCanvas(project(translateAspect)), translateAspect[2]);
                }
            }
        }

        if (drawEdges) {
            for (int i = 0; i < indices.length; i++) {
                for (int[] edge : indices[i]) {
                    for (int j = 0; j < edge.length; j++) {
                        double[] a = vertices[i][edge[j]];
                        double[] b = vertices[i][edge[(j + 1)%edge.length]];
                        double[] rotateXYA = rotateXY(a, angle * (1 + i * 0.5));
                        double[] rotateXZA = rotateXZ(rotateXYA, angle * (1 + i * 0.5));
                        double[] rotateYZA = rotateYZ(rotateXZA, angle * (1 + i * 0.5));
                        double[] rotateXYB = rotateXY(b, angle * (1 + i * 0.5));
                        double[] rotateXZB = rotateXZ(rotateXYB, angle * (1 + i * 0.5));
                        double[] rotateYZB = rotateYZ(rotateXZB, angle * (1 + i * 0.5));
                        double[] translatedPointA = translateZ(rotateYZA, viewDistance);
                        double[] translatedPointB = translateZ(rotateYZB, viewDistance);
                        double[] translateAspectA = translateAspectRatio(translatedPointA);
                        double[] translateAspectB = translateAspectRatio(translatedPointB);
                        double thicknessModifier = (translateAspectA[2] + translateAspectB[2]) / 2;
                        drawLine(
                                translateToCanvas(project(translateAspectA)),
                                translateToCanvas(project(translateAspectB)),
                                thicknessModifier
                        );
                    }
                }
            }
        }
    }

    public void handleResize(Dimension dimensions) {
        aspectRatio = dimensions.getWidth() / dimensions.getHeight();
    }

    private void handleZoom(double zoom) {
        viewDistance += zoom * 0.1;
    }

    /**
     * Draws a filled oval to the canvas at the given coordinates.
     * @param vec2 int[]{x, y}
     * @param sizeModifier double, higher means smaller
     */
    private void drawPoint(int[] vec2, double sizeModifier) {
        int size = (int) (POINT_SIZE / sizeModifier);
        int offset = (int) (size * 0.5);
//        graphics.fillOval(vec2[0] - offset, vec2[1] - offset, size, size);
//        graphics.drawOval(vec2[0] - offset, vec2[1] - offset, size, size);
        graphics.fillRect(vec2[0] - offset, vec2[1] - offset, size, size);
    }

    /**
     * Draw a line between two points.
     * @param p1 int[]{x, y}
     * @param p2 int[]{x, y}
     */
    private void drawLine(int[] p1, int[] p2, double thicknessModifier) {
        BasicStroke stroke = new BasicStroke(Math.max(0.01f, (float)(LINE_SIZE / thicknessModifier)));
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
                (int) ((vec2[0] + 1) / 2 * getWidth()),
                (int) ((1 - (vec2[1] + 1) / 2) * getHeight()),
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

    private double[] translateAspectRatio(double[] vec3) {
        return new double[]{
                vec3[0] / aspectRatio,
                vec3[1],
                vec3[2]
        };
    }

    /**
     * Translate the Z axis by a given delta or distance
     * @param vec3 double[]{x, y, z}
     * @param delta double
     * @return A new double[]{x, y, z} with translated z.
     */
    private double[] translateZ(double[] vec3, double delta) {
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
    private double[] rotateYZ(double[] vec3, double angle) {
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
    private double[] rotateXZ(double[] vec3, double angle) {
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
    private double[] rotateXY(double[] vec3, double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);

        return new double[]{
                vec3[0] * c - vec3[1] * s,
                vec3[0] * s + vec3[1] * c,
                vec3[2],
        };
    }
}
