package engine.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sphere implements ModelInterface {
    private final double radius;
    private final int segments;
    private final int rings;
    private final double x_pos;
    private final double y_pos;
    private final double z_pos;
    private final double[][] vertices;
    private final double[][] verticesNormalized;
    private final int[][] indices;

    public Sphere(double radius, int segments, int rings, double x, double y, double z) {
        this.radius = radius;
        this.segments = segments;
        this.rings = rings;
        this.x_pos = x;
        this.y_pos = y;
        this.z_pos = z;

        this.vertices = calculateVertices();
        this.verticesNormalized = calculateVerticesNormalized();
        this.indices = calculateIndices();
    }

    public double[][] getVertices() {
        return this.vertices;
    }

    public double[][] getVerticesNormalized() {
        return this.verticesNormalized;
    }

    public int[][] getIndices() {
        return this.indices;
    }

    private double[][] calculateVertices() {
        ArrayList<double[]> vertices = new ArrayList<>();

        for (int y = 0; y <= this.rings; y++) {
            for (int x = 0; x <= this.segments; x++) {
                double theta = ((double) y / this.rings) * Math.PI;
                double phi = ((double) x / this.segments) * 2 * Math.PI;

                double vx = this.x_pos + this.radius * Math.sin(theta) * Math.cos(phi);
                double vy = this.y_pos + this.radius * Math.cos(theta);
                double vz = this.z_pos + this.radius * Math.sin(theta) * Math.sin(phi);

                vertices.add(new double[]{
                        Math.round(vx * 1000) / 1000.0,
                        Math.round(vy * 1000) / 1000.0,
                        Math.round(vz * 1000) / 1000.0
                });
            }
        }

        return vertices.toArray(new double[0][]);
    }

    private double[][] calculateVerticesNormalized() {
        ArrayList<double[]> vertices = new ArrayList<>();

        for (int y = 0; y <= this.rings; y++) {
            for (int x = 0; x <= this.segments; x++) {
                double theta = ((double) y / this.rings) * Math.PI;
                double phi = ((double) x / this.segments) * 2 * Math.PI;

                double vx = this.radius * Math.sin(theta) * Math.cos(phi);
                double vy = this.radius * Math.cos(theta);
                double vz = this.radius * Math.sin(theta) * Math.sin(phi);

                vertices.add(new double[]{
                        Math.round(vx / this.radius * 1000) / 1000.0,
                        Math.round(vy / this.radius * 1000) / 1000.0,
                        Math.round(vz / this.radius * 1000) / 1000.0
                });
            }
        }

        return vertices.toArray(new double[0][]);
    }

    private int[][] calculateIndices() {
        ArrayList<int[]> indices = new ArrayList<>();

        for (int y = 0; y <= this.rings; y++) {
            for (int x = 0; x <= this.segments; x++) {
                if (x < this.segments && y < this.rings) {
                    int first = (y * (this.segments + 1)) + x;
                    int second = first + this.segments + 1;
                    indices.add(new int[]{first, second, first + 1});
                    indices.add(new int[]{second, second + 1, first + 1});
                }
            }
        }

        return indices.toArray(new int[0][]);
    }
}
