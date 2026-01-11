package engine.utils;

import java.util.ArrayList;

public class Sphere {
    public double radius;
    public int segments;
    public int rings;
    public double x_pos;
    public double y_pos;
    public double z_pos;

    public Sphere(double radius, int segments, int rings, double x, double y, double z) {
        this.radius = radius;
        this.segments = segments;
        this.rings = rings;
        this.x_pos = x;
        this.y_pos = y;
        this.z_pos = z;
    }

    public double[][] getSphereVertices() {
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

    public double[][] getSphereVerticesNormalized() {
        ArrayList<double[]> vertices = new ArrayList<>();

        for (int y = 0; y <= rings; y++) {
            for (int x = 0; x <= segments; x++) {
                double theta = ((double) y / rings) * Math.PI;
                double phi = ((double) x / segments) * 2 * Math.PI;

                double vx = radius * Math.sin(theta) * Math.cos(phi);
                double vy = radius * Math.cos(theta);
                double vz = radius * Math.sin(theta) * Math.sin(phi);

                vertices.add(new double[]{
                        Math.round(vx / radius * 1000) / 1000.0,
                        Math.round(vy / radius * 1000) / 1000.0,
                        Math.round(vz / radius * 1000) / 1000.0
                });
            }
        }

        return vertices.toArray(new double[0][]);
    }

    public int[][] getSphereIndices() {
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
