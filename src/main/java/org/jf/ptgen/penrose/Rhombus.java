/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jf.ptgen.penrose;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.math.Vector2D;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single rhombus at the intersection of 2 strips in a tiling.
 */
public class Rhombus {

    public static final int THIN = 0;
    public static final int THICK = 1;

    public final Strip strip1;
    public final Strip strip2;

    public final int[] latticeCoords;

    public final Polygon polygon;

    public Rhombus(Strip strip1, Strip strip2, int[] latticeCoords) {
        this.strip1 = strip1;
        this.strip2 = strip2;
        this.latticeCoords = latticeCoords;

        Coordinate[] coordinates = new Coordinate[5];
        System.arraycopy(getVertices(), 0, coordinates, 0, 4);
        coordinates[4] = coordinates[0];
        polygon = PenroseTiling.GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    public int getRhombusType() {
        switch (Math.abs(strip1.stripFamily.angle - strip2.stripFamily.angle)) {
            case 1:
            case 4:
                return THICK;
            case 2:
            case 3:
                return THIN;
            default:
                throw new IllegalArgumentException("Parallel lines cannot intersect");
        }
    }

    // This order produces the list of vertices in order around the rhombus
    private static int[][] offsets = new int[][]{
            {0, 0},
            {0, -1},
            {-1, -1},
            {-1, 0}
    };

    /**
     * Given a particular grid configuration, gets the bounding box in that grid that contains
     * this rhombus.
     *
     * <p>If the rhombus spans multiple bounding boxes, the bounding box that contains the most
     * of the rhombus is returned. In case of ties, bounding boxes with a lower x win. If same x,
     * bounding boxes with a lower y win.
     */
    public BoundingBox getContainingBoundingBox(Coordinate gridStart, Vector2D gridSize) {
        Set<Integer> possibleXs = new HashSet<>();
        Set<Integer> possibleYs = new HashSet<>();

        for (Coordinate vertex : getVertices()) {
            Coordinate normalized =
                    new Vector2D(vertex).subtract(new Vector2D(gridStart)).toCoordinate();
            possibleXs.add((int)Math.floor(normalized.x / gridSize.getX()));
            possibleYs.add((int)Math.floor(normalized.y / gridSize.getY()));
        }

        BoundingBox maxBoundingBox = null;
        double maxArea = 0;
        for (Integer possibleX : possibleXs) {
            for (Integer possibleY : possibleYs) {
                BoundingBox boundingBox =
                        new BoundingBox(gridStart, gridSize, possibleX, possibleY);

                double area = Math.abs(polygon.intersection(boundingBox.polygon).getArea());

                if (area > 0) {
                    if (area > maxArea ||
                            (area == maxArea && boundingBox.compareTo(maxBoundingBox) < 0)) {
                        maxArea = area;
                        maxBoundingBox = boundingBox;
                    }
                }
            }
        }

        assert maxBoundingBox != null;
        return maxBoundingBox;
    }

    /**
     * @return An array of Coordinates of the vertices of this rhombus.
     */
    public Coordinate[] getVertices() {
        Coordinate[] vertices = new Coordinate[4];
        int[] coords = new int[5];
        for (int i = 0; i < 4; i++) {
            int[] currentOffsets = offsets[i];

            System.arraycopy(latticeCoords, 0, coords, 0, 5);

            coords[strip1.stripFamily.angle] += currentOffsets[0];
            coords[strip2.stripFamily.angle] += currentOffsets[1];

            vertices[i] = getCoordinate(coords);
        }

        return vertices;
    }

    private Coordinate getCoordinate(int[] latticeCoords) {
        double x = 0;
        double y = 0;

        for (int i = 0; i < 5; i++) {
            x += latticeCoords[i] * PentAngle.PENTANGLES[i].cos;
            y -= latticeCoords[i] * PentAngle.PENTANGLES[i].sin;
        }

        Coordinate coordinate = new Coordinate(x, y);
        PenroseTiling.PRECISION_MODEL.makePrecise(coordinate);
        return coordinate;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rhombus rhombus = (Rhombus)o;

        if (!getLowerStrip().equals(rhombus.getLowerStrip())) return false;
        return getUpperStrip().equals(rhombus.getUpperStrip());
    }

    /**
     * @return The strip that this rhombus belongs to that has the lower strip family index.
     */
    public Strip getLowerStrip() {
        if (strip1.stripFamily.angle < strip2.stripFamily.angle) {
            return strip1;
        }
        return strip2;
    }

    /**
     * @return The strip that this rhombus belongs to that has the upper strip family index.
     */
    public Strip getUpperStrip() {
        if (strip1.stripFamily.angle < strip2.stripFamily.angle) {
            return strip2;
        }
        return strip1;
    }

    @Override public int hashCode() {
        int result = getLowerStrip().hashCode();
        return 31 * result + getUpperStrip().hashCode();
    }

    @Override public String toString() {
        return "Rhombus(" + strip1 + ", " + strip2 + ")";
    }
}