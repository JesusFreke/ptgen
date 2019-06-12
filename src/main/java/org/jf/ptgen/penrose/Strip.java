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
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Represents a single string of rhombii in de Bruijn's method.
 */
public class Strip {
    public final StripFamily stripFamily;
    public final int multiple;

    public Strip(StripFamily stripFamily, int multiple) {
        this.stripFamily = stripFamily;
        this.multiple = multiple;
    }

    public Iterable<Rhombus> getRhombii(double target, boolean forward) {
        return getRhombii(null, target, forward);
    }

    public Iterable<Rhombus> getRhombii(Strip start, boolean forward) {
        return getRhombii(start, getIntersectionDistanceFromPoint(start), forward);
    }

    public Rhombus getRhombus(double target) {
        return getRhombii(null, target, true).iterator().next();
    }

    private Iterable<Rhombus> getRhombii(@Nullable Strip start, double target, boolean forward) {
        final double[] intersections = new double[5];
        final int[] intersectionMultiples = new int[5];

        for (int i = 0; i < 5; i++) {
            if (i == stripFamily.angle) {
                continue;
            }

            if (start != null && i == start.stripFamily.angle) {
                intersections[i] = target;
                intersectionMultiples[i] = start.multiple;
                continue;
            }

            StripFamily other = stripFamily.tiling.getStripFamily(i);

            double initialIntersection = getIntersectionDistanceFromPoint(other.getStrip(0));
            double delta = initialIntersection - target;
            double sin = PentAngle.PENTANGLES[i].sin(PentAngle.PENTANGLES[stripFamily.angle]);
            double interval = 1 / sin;

            if (forward) {
                if (interval < 0) {
                    intersectionMultiples[i] = (int)-Math.ceil(delta / interval);
                } else {
                    intersectionMultiples[i] = (int)-Math.floor(delta / interval);
                }
            } else {
                if (interval < 0) {
                    intersectionMultiples[i] = (int)-Math.floor(delta / interval);
                } else {
                    intersectionMultiples[i] = (int)-Math.ceil(delta/ interval);
                }
            }

            intersections[i] = getIntersectionDistanceFromPoint(other.getStrip(intersectionMultiples[i]));
        }

        return new Iterable<Rhombus>() {
            @Override @Nonnull public Iterator<Rhombus> iterator() {
                return new Iterator<Rhombus>() {
                    public double[] inter = intersections;
                    public int[] interMultiples = intersectionMultiples;

                    @Override public boolean hasNext() {
                        return true;
                    }

                    @Override public Rhombus next() {
                        int closest = -1;
                        double closestValue =
                                forward ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;

                        for (int i=0; i<5; i++) {
                            if (i == stripFamily.angle) {
                                interMultiples[i] = multiple;
                                continue;
                            }

                            if ((forward && inter[i] < closestValue) ||
                                    (!forward && inter[i] > closestValue)) {
                                closestValue = inter[i];
                                closest = i;
                            }
                        }

                        int[] latticeCoords = new int[5];

                        for (int i = 0; i < 5; i++) {
                            if (i == stripFamily.angle) {
                                latticeCoords[i] = multiple;
                            } else if (i == closest) {
                                latticeCoords[i] = interMultiples[i];
                            } else {
                                latticeCoords[i] = interMultiples[i];

                                double sin = stripFamily.pentangle().sin(PentAngle.PENTANGLES[i]);
                                if ((forward && sin < 0) || (!forward && sin > 0)) {
                                    latticeCoords[i]--;
                                }
                            }
                        }

                        int multiple = interMultiples[closest];
                        double sin = PentAngle.PENTANGLES[closest]
                                .sin(PentAngle.PENTANGLES[stripFamily.angle]);

                        if ((forward && sin < 0) || (!forward && sin > 0)) {
                            interMultiples[closest]--;
                        } else {
                            interMultiples[closest]++;
                        }

                        inter[closest] = getIntersectionDistanceFromPoint(
                                stripFamily.tiling.getStripFamily(closest).getStrip
                                        (interMultiples[closest]));

                        return new Rhombus(Strip.this,
                                stripFamily.tiling.getStripFamily(closest)
                                        .getStrip(multiple), latticeCoords);
                    }

                    @Override public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public Iterable<Rhombus> getRhombii(boolean forward) {
        return getRhombii(null, 0, forward);
    }

    /**
     * Gets an arbitrary point on the strip.
     */
    public Coordinate getPoint() {
        return stripFamily
                .getOffsetDirection()
                .multiply(stripFamily.offset + multiple)
                .toCoordinate();
    }

    /**
     * Gets an arbitrary LineSegment that is collinear with the line.
     */
    public LineSegment getLine() {
        Coordinate point = getPoint();

        return new LineSegment(point, new Vector2D(point).add(
                stripFamily.getDirection()).toCoordinate());
    }

    /**
     * Gets the coordinate of the intersection between this strip and the given strip.
     * @return the coordinate of the intersection between this strip and the given strip, or null
     * if the strips are parallel.
     */
    public Coordinate getIntersectionPoint(Strip other) {
        return getLine().lineIntersection(other.getLine());
    }

    /**
     * Gets the distance from the getPoint() point to the intersection with the given strip
     */
    public double getIntersectionDistanceFromPoint(Strip other) {
        Coordinate intersectionCoordinate = getIntersectionPoint(other);
        if (intersectionCoordinate == null) {
            throw new IllegalArgumentException("The strips don't intersect");
        }
        if (new Vector2D(intersectionCoordinate).length() == 0) {
            return 0;
        }
        return divide(new Vector2D(intersectionCoordinate).subtract(new Vector2D(getPoint())),
                stripFamily.getDirection());
    }

    private double divide(Vector2D vector1, Vector2D vector2) {
        return (vector1.dot(vector2) / (vector1.length() * vector2.length())) *
                (vector1.length() / vector2.length());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Strip strip = (Strip)o;

        if (multiple != strip.multiple) return false;
        return stripFamily.equals(strip.stripFamily);
    }

    @Override public int hashCode() {
        int result = stripFamily.hashCode();
        result = 31 * result + multiple;
        return result;
    }

    @Override public String toString() {
        return "Strip(" + stripFamily.angle +":" + multiple + ")";
    }
}
