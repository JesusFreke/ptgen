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

/**
 * A bounding box.
 */
public class BoundingBox implements Comparable<BoundingBox> {
    public final Coordinate gridOrigin;
    public final Vector2D gridSize;

    public final int xMultiple;
    public final int yMultiple;

    public final Coordinate origin;
    public final Coordinate extent;

    public final Polygon polygon;

    /**
     * Given an origin point and grid size, construct a bounding box that is the (xth, yth)
     * box in the grid.
     *
     * @param gridOrigin The lower left point of the (0, 0) box of this grid.
     * @param gridSize A vector representing the x and y size of each grid box
     * @param xMultiple Create a bounding box for the box at this x multiple.
     * @param yMultiple Create a bounding box for the box at this y multiple.
     */
    public BoundingBox(Coordinate gridOrigin, Vector2D gridSize, int xMultiple, int yMultiple) {
        this.gridOrigin = gridOrigin;
        this.gridSize = gridSize;
        this.xMultiple = xMultiple;
        this.yMultiple = yMultiple;

        this.origin = new Vector2D(gridOrigin).add(new Vector2D(
                gridSize.getX() * xMultiple, gridSize.getY() * yMultiple)).toCoordinate();

        this.extent = new Vector2D(origin).add(gridSize).toCoordinate();

        polygon = PenroseTiling.GEOMETRY_FACTORY.createPolygon(new Coordinate[] {
                origin,
                new Coordinate(origin.x, extent.y),
                new Coordinate(extent.x, extent.y),
                new Coordinate(extent.x, origin.y),
                origin
        });
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoundingBox that = (BoundingBox)o;

        if (xMultiple != that.xMultiple) return false;
        if (yMultiple != that.yMultiple) return false;

        if (!gridOrigin.equals(that.gridOrigin)) return false;
        if (!gridSize.equals(that.gridSize)) return false;
        return true;
    }

    @Override public int compareTo(BoundingBox o) {
        int comparison = Double.compare(origin.x, o.origin.x);
        if (comparison != 0) {
            return comparison;
        }
        return Double.compare(origin.y, o.origin.y);
    }

    @Override public int hashCode() {
        int result = gridOrigin.hashCode();
        result = 31 * result + gridSize.hashCode();
        result = 31 * result + xMultiple;
        result = 31 * result + yMultiple;
        return result;
    }
}


