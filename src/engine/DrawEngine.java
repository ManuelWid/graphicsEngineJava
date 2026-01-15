package engine;

import engine.utils.ModelInterface;
import engine.utils.Sphere;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class DrawEngine extends JPanel implements ActionListener {
    public boolean drawVertices = false;
    public boolean drawEdges = false;
    public boolean drawFaces = false;

    private double viewDistance = 1;
    private double angle = 0;

    private final Timer timer;

    private int pointSize = 4;
    private float lineSize = 1.0f;

    public int FPS = 60;
    private double DELTA = 1.0 / FPS;

    public Color backgroundColor = new Color(30, 30, 30);
    public Color pointColor = new Color(0, 180, 0, 255);
    public Color lineColor = new Color(0, 110, 0, 255);
    public Color faceColor = new Color(0, 150, 0, 255);

    private double aspectRatio = 1.0;

    private final ArrayList<double[][]> vertices = new ArrayList<>();
    private final ArrayList<int[][]> indices = new ArrayList<>();

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
        angle += 0.5 * DELTA;
//        System.out.println(1000 / timer.getDelay());
        repaint();
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        setBackground(backgroundColor);

        if (this.vertices.isEmpty() && this.indices.isEmpty()) {
            return;
        }

        if (drawVertices) {
            for (int i = 0; i < vertices.size(); i++) {
                for (double[] vertex : vertices.get(i)) {
                    double[] rotateXY = rotateXY(vertex, angle * (1 + i * 0.5));
                    double[] rotateXZ = rotateXZ(rotateXY, angle * (1 + i * 0.5));
                    double[] rotateYZ = rotateYZ(rotateXZ, angle * (1 + i * 0.5));
                    double[] translatedPoint = translateZ(rotateYZ, viewDistance);
                    double[] translateAspect = translateAspectRatio(translatedPoint);
                    drawPoint(g2d, translateToCanvas(project(translateAspect)), translateAspect[2]);
                }
            }
        }

        if (drawFaces) {
            for (int i = 0; i < indices.size(); i++) {
                for (int[] edge : indices.get(i)) {
                    int[] xPositions = new int[3];
                    int[] yPositions = new int[3];
                    double colorModifier = 0;

                    for (int j = 0; j < edge.length; j++) {
                        double[] vertex = vertices.get(i)[edge[j]];
//                        if (vertex[2] > viewDistance) {
//                            break;
//                        }
                        double[] rotateXY = rotateXY(vertex, this.angle * (1 + i * 0.5));
                        double[] rotateXZ = rotateXZ(rotateXY, this.angle * (1 + i * 0.5));
                        double[] rotateYZ = rotateYZ(rotateXZ, this.angle * (1 + i * 0.5));
                        double[] translatedPoint = translateZ(rotateYZ, this.viewDistance);
                        double[] translateAspect = translateAspectRatio(translatedPoint);
                        int[] canvasCoords = translateToCanvas(project(translateAspect));
                        xPositions[j] = canvasCoords[0];
                        yPositions[j] = canvasCoords[1];
                        if (colorModifier == 0) {
                            colorModifier = translateAspect[2];
                        }
                    }

                    drawPolygon(g2d, xPositions, yPositions, colorModifier);
                }
            }
        }

        if (drawEdges) {
            for (int i = 0; i < indices.size(); i++) {
                for (int[] edge : indices.get(i)) {
                    for (int j = 0; j < edge.length; j++) {
                        double[] a = vertices.get(i)[edge[j]];
                        double[] b = vertices.get(i)[edge[(j + 1)%edge.length]];
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
                                g2d,
                                translateToCanvas(project(translateAspectA)),
                                translateToCanvas(project(translateAspectB)),
                                thicknessModifier
                        );
                    }
                }
            }
        }
    }

    /**
     * Main method to add models to the engine.
     * @param model The model to add, this must implement the ModelInterface
     */
    public void addModel(ModelInterface model) {
        this.vertices.add(model.getVertices());
        this.indices.add(model.getIndices());
    }

    /**
     * Called on a canvas resize to calculate the new aspect ratio
     * @param dimensions Dimensions of the new canvas size, passed by componentResize
     */
    private void handleResize(Dimension dimensions) {
        aspectRatio = dimensions.getWidth() / dimensions.getHeight();
    }

    /**
     * Zooms the canvas in or out
     * @param zoom double received from mouseWheelEvent, positive zooms in
     */
    private void handleZoom(double zoom) {
        viewDistance = Math.clamp(viewDistance - zoom * 0.1, 0.5, 10);
    }

    /**
     * Draws a filled oval to the canvas at the given coordinates.
     * @param graphics Graphics2D object
     * @param vec2 int[]{x, y}
     * @param sizeModifier double, higher means smaller
     */
    private void drawPoint(Graphics2D graphics, int[] vec2, double sizeModifier) {
        graphics.setColor(pointColor);
        int size = (int) (pointSize / sizeModifier);
        int offset = (int) (size * 0.5);
//        graphics.fillOval(vec2[0] - offset, vec2[1] - offset, size, size);
        graphics.fillRect(vec2[0] - offset, vec2[1] - offset, size, size);
    }

    /**
     * Draw a line between two points.
     * @param graphics Graphics2D object
     * @param p1 int[]{x, y}
     * @param p2 int[]{x, y}
     * @param thicknessModifier any double, higher means thinner line, z axis preferred
     */
    private void drawLine(Graphics2D graphics, int[] p1, int[] p2, double thicknessModifier) {
        graphics.setColor(lineColor);
        BasicStroke stroke = new BasicStroke(Math.max(0.01f, (float)(lineSize / thicknessModifier)));
        graphics.setStroke(stroke);
        graphics.drawLine(p1[0], p1[1], p2[0], p2[1]);
    }

    /**
     * Draw a 3 point polygon
     * @param graphics Graphics2D object
     * @param xArray int[3] with x points
     * @param yArray int[3] with y points
     * @param colorModifier any double, used to calculate new face color
     */
    private void drawPolygon(Graphics2D graphics, int[] xArray, int[] yArray, double colorModifier) {
        int red = Math.clamp((int)(faceColor.getRed() * colorModifier), 0, 255);
        int green = Math.clamp((int)(faceColor.getGreen() * colorModifier), 0, 255);
        int blue = Math.clamp((int)(faceColor.getBlue() * colorModifier), 0, 255);
        int alpha = Math.clamp((int)(faceColor.getAlpha() - colorModifier * 150), 0, 255);
        Color fillColor = new Color(red, green, blue, alpha);
        graphics.setColor(fillColor);
        graphics.fillPolygon(xArray, yArray, 3);
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

    /**
     *
     * @param vec3 double[]{x, y, z}
     * @return A new vec3 double[]{x, y, z} with corrected x in case the viewport is not square
     */
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
