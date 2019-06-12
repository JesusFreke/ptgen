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

package org.jf.ptgen;

import org.jf.ptgen.penrose.BoundingBox;
import org.jf.ptgen.penrose.PenroseTiling;
import org.jf.ptgen.penrose.PenroseTiling.RhombusVisitor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.util.Random;

/**
 * PTGen = Penrose-Tiling-Generator
 */
public class PTGen {
    private final long seed;
    public final double minX;
    public final double minY;
    public final double width;
    public final double height;
    public final int countX;
    public final int countY;

    /**
     * Construct a tiling generator.
     *
     * @param seed The random seed used to generate the tiling.
     * @param minX The minimum x coordinate of the pentagrid space to include in the tiling.
     * @param minY The minimum x coordinate of the pentagrid space to include in the tiling.
     * @param width The width of a single grid box, in pentagrid space coordinates.
     * @param height The height of a single grid box, in pentagrid space coordinates.
     * @param countX How many grid boxes to include in the x dimension.
     * @param countY How many grid boxes to include in the y dimension.
     */
    public PTGen(long seed, double minX, double minY, double width, double height,
                 int countX, int countY) {
        this.seed = seed;
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;
        this.countX = countX;
        this.countY = countY;
    }

    /**
     * An interface for receiving callbacks about the generated tiling.
     */
    public interface RhombusOutput extends RhombusVisitor {
        /**
         * Called once at the beginning of tiling generation.
         */
        void start(PTGen ptgen);

        /**
         * Called once at the start of every grid box that is generated.
         * @param boundingBox The bounding box being visited.
         */
        void startBox(BoundingBox boundingBox);

        /**
         * Called once at the end of every grid box that is generated.
         * @param boundingBox The bounding box being left.
         */
        void endBox(BoundingBox boundingBox);

        /**
         * Called once at the end of tiling generation.
         */
        void end();
    }

    /**
     * Generates the tiling, calling the appropriate visitor methods as generating proceeds.
     */
    public void visitRhombii(RhombusOutput visitor) {
        Random random = new Random(seed);
        PenroseTiling tiling = new PenroseTiling(random);

        visitor.start(this);

        Coordinate gridOrigin = new Coordinate(minX, minY);
        PenroseTiling.PRECISION_MODEL.makePrecise(gridOrigin);
        Coordinate gridSizeCoordinate = new Coordinate(width, height);
        PenroseTiling.PRECISION_MODEL.makePrecise(gridSizeCoordinate);
        Vector2D gridSize = new Vector2D(gridSizeCoordinate);

        for (int x = 0; x < countX; x++) {
            for (int y = 0; y < countY; y++) {
                BoundingBox boundingBox = new BoundingBox(gridOrigin, gridSize, x, y);
                visitor.startBox(boundingBox);
                tiling.visitRhombii(boundingBox, visitor);
                visitor.endBox(boundingBox);
            }
        }

        visitor.end();
    }

}
